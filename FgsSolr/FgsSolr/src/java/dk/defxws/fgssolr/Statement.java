//$Id: Statement.java 6565 2008-02-07 14:53:30Z gertsp $
/*
 * <p><b>License and Copyright: </b>The contents of this file is subject to the
 * same open source license as the Fedora Repository System at www.fedora-commons.org
 * Copyright &copy; 2006, 2007, 2008 by The Technical University of Denmark.
 * All rights reserved.</p>
 */
package dk.defxws.fgssolr;

import java.io.IOException;
import java.io.StringReader;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.ListIterator;
import java.util.Locale;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;

import dk.defxws.fedoragsearch.server.errors.GenericSearchException;
import fedora.server.utilities.StreamUtility;

/**
 * queries the Solr index 
 * 
 * @author  gsp@dtv.dk
 * @version 
 */
public class Statement {
    
    private static final Logger logger = Logger.getLogger(Statement.class);
    
    ResultSet executeQuery(
            String queryString, 
            int startRecord, 
            int maxResults,
            int snippetsMax,
            int fieldMaxLength,
            Analyzer analyzer, 
            String defaultQueryFields, 
            String indexPath, 
            String indexName, 
            String snippetBegin,
            String snippetEnd, 
            String sortFields)
    throws GenericSearchException {
        ResultSet rs = null;
        IndexSearcher searcher = null;
        try {
            StringTokenizer defaultFieldNames = new StringTokenizer(defaultQueryFields);
            int countFields = defaultFieldNames.countTokens();
            String[] defaultFields = new String[countFields];
            for (int i=0; i<countFields; i++) {
                defaultFields[i] = defaultFieldNames.nextToken();
            }
            searcher = new IndexSearcher(indexPath);
            Query query = null;
            if (defaultFields.length == 1) {
                query = (new QueryParser(defaultFields[0], analyzer)).parse(queryString);
            }
            else {
                query = (new MultiFieldQueryParser(defaultFields, analyzer)).parse(queryString);
            }
            query.rewrite(IndexReader.open(indexPath));
//            Hits hits = searcher.search(query);
            Hits hits;
			try {
				hits = getHits(searcher, query, sortFields);
			} catch (RuntimeException e) {
	            throw new GenericSearchException("sortFields must be UN_TOKENIZED : "+e.toString());
			}
            int start = Integer.parseInt(Integer.toString(startRecord));
            int end = Math.min(hits.length(), start + maxResults - 1);
            StringBuffer resultXml = new StringBuffer();
            resultXml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            resultXml.append("<solrsearch "+
                    "   xmlns:dc=\"http://purl.org/dc/elements/1.1/"+
                    "\" query=\""+URLEncoder.encode(queryString, "UTF-8")+
                    "\" indexName=\""+indexName+
                    "\" sortFields=\""+sortFields+
                    "\" hitPageStart=\""+startRecord+
                    "\" hitPageSize=\""+maxResults+
                    "\" hitTotal=\""+hits.length()+"\">");
            for (int i = start; i <= end; i++)
            {
                Document doc = hits.doc(i-1);
                resultXml.append("<hit no=\""+i+ "\" score=\""+hits.score(i-1)+"\">");
                for (ListIterator li = doc.getFields().listIterator(); li.hasNext(); ) {
                    Field f = (Field)li.next();
                    resultXml.append("<field name=\""+f.name()+"\"");
                    String snippets = null;
                    if (snippetsMax > 0) {
//                        SimpleHTMLFormatter formatter = new SimpleHTMLFormatter("<span class=\"highlight\">", "</span>");
                        SimpleHTMLFormatter formatter = new SimpleHTMLFormatter("!!!SNIPPETBEGIN", "!!!SNIPPETEND");
                        QueryScorer scorer = new QueryScorer(query, f.name());
                        Highlighter highlighter = new Highlighter(formatter, scorer);
                        Fragmenter fragmenter = new SimpleFragmenter(fieldMaxLength);
                        highlighter.setTextFragmenter(fragmenter);
                        TokenStream tokenStream = analyzer.tokenStream( f.name(), new StringReader(f.stringValue()));
                        snippets = highlighter.getBestFragments(tokenStream, f.stringValue(), snippetsMax, " ... ");
                        //snippets = snippets.replace('&', '#');
                        snippets = checkTruncatedWords(snippets, " ... ");
                        snippets = StreamUtility.enc(snippets);
                        snippets = snippets.replaceAll("!!!SNIPPETBEGIN", snippetBegin);
                        snippets = snippets.replaceAll("!!!SNIPPETEND", snippetEnd);
                        if (snippets!=null && !snippets.equals("")) {
                            resultXml.append(" snippet=\"yes\">"+snippets);
                        }
                    }
                    if (snippets==null || snippets.equals(""))
                        if (fieldMaxLength > 0 && f.stringValue().length() > fieldMaxLength) {
                            String snippet = f.stringValue().substring(0, fieldMaxLength);
                            int iamp = snippet.lastIndexOf("&");
                            if (iamp>-1 && iamp>fieldMaxLength-8)
                                snippet = snippet.substring(0, iamp);
                            resultXml.append(">"+StreamUtility.enc(snippet)+" ... ");
                        } else
                            resultXml.append(">"+StreamUtility.enc(f.stringValue()));
                    resultXml.append("</field>");
                }
                resultXml.append("</hit>");
            }
            resultXml.append("</solrsearch>");
            rs = new ResultSet(resultXml);
        } catch (GenericSearchException e) {
            throw new GenericSearchException(e.toString());
        } catch (IOException e) {
            throw new GenericSearchException(e.toString());
        } catch (ParseException e) {
            throw new GenericSearchException(e.toString());
        } finally {
            if (searcher!=null)
                try {
                    searcher.close();
                } catch (IOException e) {
                }
        }
        return rs;
    }
    
//    sortFields ::= [sortField[';'sortField]*]
//    sortField  ::= sortFieldName[','(sortType | locale)[','reverse]]]]
//    sortFieldName ::= #the name of an index field, which is UN_TOKENIZED and contains a single term per document
//    sortType   ::= 'AUTO' (default) | 'DOC' | 'SCORE' | 'INT' | 'FLOAT' | 'STRING'
//    locale     ::= language['-'country['-'variant]]
//    reverse    ::= 'false' (default) | 'true'
    private Hits getHits(IndexSearcher searcher, Query query, String sortFields) throws GenericSearchException {
    	Hits hits = null;
    	IndexReader ireader = searcher.getIndexReader();
    	Collection fieldNames = ireader.getFieldNames(IndexReader.FieldOption.ALL);
    	String sortFieldsString = sortFields;
    	if (sortFields == null) sortFieldsString = "";
    	StringTokenizer st = new StringTokenizer(sortFieldsString, ";");
    	SortField[] sortFieldArray = new SortField[st.countTokens()];
    	int i = 0;
    	while (st.hasMoreTokens()) {
        	SortField sortField = null;
    		String sortFieldString = st.nextToken().trim();
    		if (sortFieldString.length()==0)
	            throw new GenericSearchException("sortFields : empty sortField string in '" + sortFields + "'");
    		StringTokenizer stf = new StringTokenizer(sortFieldString, ",");
    		if (!stf.hasMoreTokens())
	            throw new GenericSearchException("sortFields : empty sortFieldName string in '" + sortFieldString + "'");
    		String sortFieldName = stf.nextToken().trim();
    		if (sortFieldName.length()==0)
	            throw new GenericSearchException("sortFields : empty sortFieldName string in '" + sortFieldString + "'");
    		if (!fieldNames.contains(sortFieldName))
	            throw new GenericSearchException("sortFields : sortFieldName '" + sortFieldName + "' not found as index field name");
    		if (!stf.hasMoreTokens()) {
            	sortField = new SortField(sortFieldName);
    		} else {
    			String sortTypeOrLocaleString = stf.nextToken().trim();
        		if (sortTypeOrLocaleString.length()==0)
    	            throw new GenericSearchException("sortFields : empty sortType or locale string in '" + sortFieldString + "'");
    			int sortType = -1;
    			Locale locale = null;
    			if ("AUTO".equals(sortTypeOrLocaleString)) sortType = SortField.AUTO;
    			else if ("DOC".equals(sortTypeOrLocaleString)) sortType = SortField.DOC;
    			else if ("SCORE".equals(sortTypeOrLocaleString)) sortType = SortField.SCORE;
    			else if ("INT".equals(sortTypeOrLocaleString)) sortType = SortField.INT;
    			else if ("FLOAT".equals(sortTypeOrLocaleString)) sortType = SortField.FLOAT;
    			else if ("STRING".equals(sortTypeOrLocaleString)) sortType = SortField.STRING;
    			else if (((sortTypeOrLocaleString.substring(0, 1)).compareTo("A") >= 0) && ((sortTypeOrLocaleString.substring(0, 1)).compareTo("Z") <= 0)) {
    	            throw new GenericSearchException("sortFields : unknown sortType string '" + sortTypeOrLocaleString + "' in '" + sortFieldString + "'");
    			}
    			else {
            		StringTokenizer stfl = new StringTokenizer(sortTypeOrLocaleString, "-");
            		if (stfl.countTokens()>3)
        	            throw new GenericSearchException("sortFields : unknown locale string '" + sortTypeOrLocaleString + "' in '" + sortFieldString + "'");
            		String language = stfl.nextToken().trim();
            		if (language.length()==0)
        	            throw new GenericSearchException("sortFields : empty language string in '" + sortFieldString + "'");
            		if (language.length()>2)
        	            throw new GenericSearchException("sortFields : unknown language string '" + language + "' in '" + sortFieldString + "'");
            		if (!stfl.hasMoreTokens()) {
                    	locale = new Locale(language);
            		} else {
            			String country = stfl.nextToken().trim();
                		if (country.length()==0)
            	            throw new GenericSearchException("sortFields : empty country string in '" + sortFieldString + "'");
                		if (country.length()>3)
            	            throw new GenericSearchException("sortFields : unknown country string '" + country + "' in '" + sortFieldString + "'");
                		if (!stfl.hasMoreTokens()) {
                        	locale = new Locale(language, country);
                		} else {
                			String variant = stfl.nextToken().trim();
                    		if (variant.length()==0)
                	            throw new GenericSearchException("sortFields : empty variant string in '" + sortFieldString + "'");
                        	locale = new Locale(language, country, variant);
                		}
            		}
    			}
        		if (!stf.hasMoreTokens()) {
        			if (sortType >= 0)
        				sortField = new SortField(sortFieldName, sortType);
        			else
        				sortField = new SortField(sortFieldName, locale);
        		} else {
        			String reverseString = stf.nextToken().trim();
            		if (reverseString.length()==0)
        	            throw new GenericSearchException("sortFields : empty reverse string in '" + sortFieldString + "'");
        			boolean reverse = false;
        			if ("true".equalsIgnoreCase(reverseString)) reverse = true;
        			else if ("false".equalsIgnoreCase(reverseString)) reverse = false;
        			else
        	            throw new GenericSearchException("sortFields : unknown reverse string '" + reverseString + "' in '" + sortFieldString + "'");
        			if (sortType >= 0)
        				sortField = new SortField(sortFieldName, sortType, reverse);
        			else
        				sortField = new SortField(sortFieldName, locale, reverse);
        		}
    		}
    		sortFieldArray[i++] = sortField;
    	}
    	Sort sort = new Sort(sortFieldArray);
    	try {
    		if (sortFieldArray.length == 0) {
    			hits = searcher.search(query);
    		} else {
    			hits = searcher.search(query, sort);
    		}
		} catch (IOException e) {
            throw new GenericSearchException("sortFields :" + e.toString());
		}
    	return hits;
    }

    //	contributed by Leire Urcelay
    private String checkTruncatedWords(String snippets, String separator) {
    	String transformedSnippets = "";

    	if (snippets!=null && !snippets.equals("")) {
    		int separatorIndex = snippets.indexOf(separator);
    		while (separatorIndex > -1 ) {
    			transformedSnippets = transformedSnippets.concat(removeLastWordIfNeeded(snippets.substring(0, separatorIndex)));
    			transformedSnippets = transformedSnippets.concat(separator);
    			snippets = snippets.substring(separatorIndex + separator.length());    			
    			separatorIndex = snippets.indexOf(separator);
    		}
    		//add last node
    		snippets = removeLastWordIfNeeded(snippets.substring(0, snippets.length()));
    		transformedSnippets = transformedSnippets.concat(snippets);
    	}
    	else {
    		transformedSnippets = snippets;
    	}
    	return transformedSnippets;
    }

    private String removeLastWordIfNeeded(String snippetsFragment) {
    	int lastWordIndex = snippetsFragment.lastIndexOf(" ");
    	if ((lastWordIndex > -1) && (lastWordIndex + 1  <= snippetsFragment.length())) {
    		String lastWord = snippetsFragment.substring(lastWordIndex + 1, snippetsFragment.length());
    		if ((lastWord.startsWith("&")) && (!lastWord.endsWith(";"))) {
    			snippetsFragment = snippetsFragment.substring(0, lastWordIndex);    			
    		}	
    	}
    	return snippetsFragment;
    }
    
    void close() throws GenericSearchException {
    }
}
