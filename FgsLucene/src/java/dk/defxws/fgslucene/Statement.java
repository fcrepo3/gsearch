//$Id$
/*
 * <p><b>License and Copyright: </b>The contents of this file will be subject to the
 * same open source license as the Fedora Repository System at www.fedora.info
 * It is expected to be released with Fedora version 2.2.
 *
 * <p>The entire file consists of original code.  
 * Copyright &copy; 2006 by The Technical University of Denmark.
 * All rights reserved.</p>
 *
 */
package dk.defxws.fgslucene;

import java.io.IOException;
import java.io.StringReader;
import java.net.URLEncoder;
import java.util.Enumeration;
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
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;

import dk.defxws.fedoragsearch.server.errors.GenericSearchException;

/**
 * queries the Lucene index 
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
            String indexName)
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
            Hits hits = searcher.search(query);
            int start = Integer.parseInt(Integer.toString(startRecord));
            int end = Math.min(hits.length(), start + maxResults - 1);
            StringBuffer resultXml = new StringBuffer();
            resultXml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            resultXml.append("<lucenesearch "+
                    "   xmlns:dc=\"http://purl.org/dc/elements/1.1/"+
                    "\" query=\""+URLEncoder.encode(queryString, "UTF-8")+
                    "\" indexName=\""+indexName+
                    "\" hitPageStart=\""+startRecord+
                    "\" hitPageSize=\""+maxResults+
                    "\" hitTotal=\""+hits.length()+"\">");
            for (int i = start; i <= end; i++)
            {
                Document doc = hits.doc(i-1);
                resultXml.append("<hit no=\""+i+ "\" score=\""+hits.score(i-1)+"\">");
                for (Enumeration e = doc.fields(); e.hasMoreElements(); ) {
                    Field f = (Field)e.nextElement();
                    resultXml.append("<field name=\""+f.name()+"\"");
                    String snippets = null;
                    if (snippetsMax > 0) {
                        SimpleHTMLFormatter formatter = new SimpleHTMLFormatter("<span class=\"highlight\">", "</span>");
                        QueryScorer scorer = new QueryScorer(query, f.name());
                        Highlighter highlighter = new Highlighter(formatter, scorer);
                        Fragmenter fragmenter = new SimpleFragmenter(fieldMaxLength);
                        highlighter.setTextFragmenter(fragmenter);
                        TokenStream tokenStream = analyzer.tokenStream( f.name(), new StringReader(f.stringValue()));
                        snippets = highlighter.getBestFragments(tokenStream, f.stringValue(), snippetsMax, " ... ");
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
                            else
                                resultXml.append(">"+snippet+" ... ");
                        } else
                            resultXml.append(">"+f.stringValue());
                    resultXml.append("</field>");
                }
                resultXml.append("</hit>");
            }
            resultXml.append("</lucenesearch>");
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
    
    void close() throws GenericSearchException {
    }
}
