//$Id$
/*
 * <p><b>License and Copyright: </b>The contents of this file is subject to the
 * same open source license as the Fedora Repository System at www.fedora-commons.org
 * Copyright &copy; 2006, 2007, 2008, 2009, 2010, 2011 by The Technical University of Denmark.
 * All rights reserved.</p>
 */
package dk.defxws.fgslucene;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.LogDocMergePolicy;
import org.apache.lucene.index.StaleReaderException;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

import dk.defxws.fedoragsearch.server.GTransformer;
import dk.defxws.fedoragsearch.server.GenericOperationsImpl;
import dk.defxws.fedoragsearch.server.errors.ConfigException;
import dk.defxws.fedoragsearch.server.errors.GenericSearchException;

import org.fcrepo.server.utilities.StreamUtility;

/**
 * performs the Lucene specific parts of the operations
 * 
 * @author  gsp@dtv.dk
 * @version 
 */
public class OperationsImpl extends GenericOperationsImpl {
    
    private static final Logger logger = Logger.getLogger(OperationsImpl.class);
    
    private IndexWriter iw = null;
    private IndexReader ir = null;
    
    public String gfindObjects(
            String query,
            int hitPageStart,
            int hitPageSize,
            int snippetsMax,
            int fieldMaxLength,
            String indexName,
            String sortFields,
            String resultPageXslt)
    throws java.rmi.RemoteException {
        super.gfindObjects(query, hitPageStart, hitPageSize, snippetsMax, fieldMaxLength, indexName, sortFields, resultPageXslt);
        if ("".equals(usingQuery)) {
        	return embeddedResult.toString();
        }
        String usingIndexName = config.getIndexName(indexName);
        if (srf != null && config.isSearchResultFilteringActive("presearch")) {
        	usingIndexName = srf.selectIndexNameForPresearch(fgsUserName, usingIndexName, fgsUserAttributes, config);
            if (logger.isDebugEnabled())
                logger.debug("gfindObjects presearch" +
                        " fgsUserName="+fgsUserName+
                        " usingIndexName="+usingIndexName);
        }
        if (srf != null && config.isSearchResultFilteringActive("insearch")) {
        	usingQuery = srf.rewriteQueryForInsearch(fgsUserName, usingIndexName, usingQuery, fgsUserAttributes, config);
            if (logger.isDebugEnabled())
                logger.debug("gfindObjects insearch" +
                        " fgsUserName="+fgsUserName+
                        " usingQuery="+usingQuery);
        }
        ResultSet resultSet = null;
		try {
			resultSet = (new Connection()).createStatement().executeQuery(
					usingQuery,
			        hitPageStart,
			        hitPageSize,
			        snippetsMax,
			        fieldMaxLength,
			        getQueryAnalyzer(usingIndexName),
			        config.getDefaultQueryFields(usingIndexName),
			        config.getAllowLeadingWildcard(usingIndexName),
			        config.getLowercaseExpandedTerms(usingIndexName),
			        config.getIndexDir(usingIndexName),
			        usingIndexName,
			        config.getSnippetBegin(usingIndexName),
			        config.getSnippetEnd(usingIndexName),
			        config.getSortFields(usingIndexName, sortFields));
		} catch (Exception e) {
            throw new GenericSearchException("gfindObjects executeQuery error:\n" + e.toString());
		}
        params[12] = "RESULTPAGEXSLT";
        params[13] = resultPageXslt;
        String xsltPath = null;
		try {
			xsltPath = config.getConfigName()+"/index/"+usingIndexName+"/"+config.getGfindObjectsResultXslt(usingIndexName, resultPageXslt);
		} catch (Exception e) {
            throw new GenericSearchException("gfindObjects xsltPath error:\n" + e.toString());
		}
        if (logger.isDebugEnabled())
            logger.debug("gfindObjects xsltPath=\n"+xsltPath+" resultSet="+resultSet);
        StringBuffer resultXml = null;
		try {
			resultXml = (new GTransformer()).transform(
					xsltPath,
					resultSet.getResultXml(),
			        params);
		} catch (Exception e) {
            throw new GenericSearchException("gfindObjects transform error:\n" + e.toString());
		}
        if (srf != null && config.isSearchResultFilteringActive("postsearch")) {
        	resultXml = srf.filterResultsetForPostsearch(fgsUserName, resultXml, fgsUserAttributes, config);
            if (logger.isDebugEnabled())
                logger.debug("gfindObjects postsearch" +
                        " fgsUserName="+fgsUserName+
                        " resultXml=\n"+resultXml);
        }
        return resultXml.toString();
    }
    
    public String browseIndex(
            String startTerm,
            int termPageSize,
            String fieldName,
            String indexName,
            String resultPageXslt)
    throws java.rmi.RemoteException {
        super.browseIndex(startTerm, termPageSize, fieldName, indexName, resultPageXslt);
        StringBuffer resultXml = new StringBuffer("<fields>");
        try {
            getIndexWriter(indexName);
			optimize(indexName, resultXml);
        } finally {
            closeIndexWriter(indexName);
        }
        int termNo = 0;
        try {
            getIndexReader(indexName);
            Iterator fieldNames = (new TreeSet(ir.getFieldNames(IndexReader.FieldOption.INDEXED))).iterator();
            while (fieldNames.hasNext()) {
                resultXml.append("<field>"+fieldNames.next()+"</field>");
            }
            resultXml.append("</fields>");
            resultXml.append("<terms>");
            int pageSize = 0;
            Term beginTerm = new Term(fieldName, "");
            TermEnum terms;
            try {
                terms = ir.terms(beginTerm);
            } catch (IOException e) {
                throw new GenericSearchException("IndexReader terms error:\n" + e.toString());
            }
            try {
                while (terms.term()!=null && terms.term().field().equals(fieldName)
                        && !"".equals(terms.term().text().trim())) {
                    termNo++;
                    if (startTerm.compareTo(terms.term().text()) <= 0 && pageSize < termPageSize) {
                        pageSize++;
                        resultXml.append("<term no=\""+termNo+"\""
                                +" fieldtermhittotal=\""+terms.docFreq()
                                +"\">"+StreamUtility.enc(terms.term().text())+"</term>");
                    }
                    terms.next();
                }
            } catch (IOException e) {
                throw new GenericSearchException("IndexReader terms.next error:\n" + e.toString());
            }
            try {
                terms.close();
            } catch (IOException e) {
                throw new GenericSearchException("IndexReader terms close error:\n" + e.toString());
            }
        } catch (IOException e) {
            throw new GenericSearchException("IndexReader open error:\n" + e.toString());
        } finally {
            closeIndexReader(indexName);
        }
        resultXml.append("</terms>");
        resultXml.insert(0, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<lucenebrowseindex "+
                "   xmlns:dc=\"http://purl.org/dc/elements/1.1/"+
                "\" startTerm=\""+StreamUtility.enc(startTerm)+
                "\" termPageSize=\""+termPageSize+
                "\" fieldName=\""+fieldName+
                "\" indexName=\""+indexName+
                "\" termTotal=\""+termNo+"\">");
        resultXml.append("</lucenebrowseindex>");
        if (logger.isDebugEnabled())
            logger.debug("resultXml="+resultXml);
        params[10] = "RESULTPAGEXSLT";
        params[11] = resultPageXslt;
        String xsltPath = config.getConfigName()+"/index/"+config.getIndexName(indexName)+"/"+config.getBrowseIndexResultXslt(indexName, resultPageXslt);
        StringBuffer sb = (new GTransformer()).transform(
        		xsltPath,
                resultXml,
                params);
        return sb.toString();
    }
    
    public String getIndexInfo(
            String indexName,
            String resultPageXslt)
    throws java.rmi.RemoteException {
        super.getIndexInfo(indexName, resultPageXslt);
        InputStream infoStream =  null;
        String indexInfoPath = "/"+config.getConfigName()+"/index/"+config.getIndexName(indexName)+"/indexInfo.xml";
        try {
            infoStream =  OperationsImpl.class.getResourceAsStream(indexInfoPath);
            if (infoStream == null) {
                throw new GenericSearchException("Error "+indexInfoPath+" not found in classpath");
            }
        } catch (IOException e) {
            throw new GenericSearchException("Error "+indexInfoPath+" not found in classpath", e);
        }
        String xsltPath = config.getConfigName()+"/index/"+config.getIndexName(indexName)+"/"+config.getIndexInfoResultXslt(indexName, resultPageXslt);
        StringBuffer sb = (new GTransformer()).transform(
        		xsltPath,
                new StreamSource(infoStream),
                new String[] {});
        return sb.toString();
    }
    
    public String updateIndex(
            String action,
            String value,
            String repositoryName,
            String indexName,
            String indexDocXslt,
            String resultPageXslt)
    throws java.rmi.RemoteException {
        insertTotal = 0;
        updateTotal = 0;
        deleteTotal = 0;
        int initDocCount = 0;
        StringBuffer resultXml = new StringBuffer(); 
        resultXml.append("<luceneUpdateIndex");
        resultXml.append(" indexName=\""+indexName+"\"");
        resultXml.append(">\n");
        try {
    		if ("deletePid".equals(action)) {
    			deletePid(value, indexName, resultXml);
    		}
        	else if (action != null && action.length()>0) {
                getIndexWriter(indexName);
        		initDocCount = docCount;
            	if ("createEmpty".equals(action)) 
            		createEmpty(indexName, resultXml);
        		else {
        			if ("fromPid".equals(action)) 
						fromPid(value, repositoryName, indexName, resultXml, indexDocXslt);
        			else {
        				if ("fromFoxmlFiles".equals(action)) 
        					fromFoxmlFiles(value, repositoryName, indexName, resultXml, indexDocXslt);
        				else
        					if ("optimize".equals(action)) 
                				optimize(indexName, resultXml);
        			}
        		}
        	}
        } finally {
            closeIndexWriter(indexName);
        	getIndexReader(indexName);
        	closeIndexReader(indexName);
        	if (updateTotal > 0) {
        		int diff = docCount - initDocCount;
        		insertTotal = diff;
        		updateTotal -= diff;
        	}
        }
        logger.info("updateIndex "+action+" indexName="+indexName
        		+" indexDirSpace="+indexDirSpace(new File(config.getIndexDir(indexName)))
        		+" docCount="+docCount);
        resultXml.append("<counts");
        resultXml.append(" insertTotal=\""+insertTotal+"\"");
        resultXml.append(" updateTotal=\""+updateTotal+"\"");
        resultXml.append(" deleteTotal=\""+deleteTotal+"\"");
        resultXml.append(" docCount=\""+docCount+"\"");
        resultXml.append(" warnCount=\""+warnCount+"\"");
        resultXml.append("/>\n");
        resultXml.append("</luceneUpdateIndex>\n");
        params = new String[12];
        params[0] = "OPERATION";
        params[1] = "updateIndex";
        params[2] = "ACTION";
        params[3] = action;
        params[4] = "VALUE";
        params[5] = value;
        params[6] = "REPOSITORYNAME";
        params[7] = repositoryName;
        params[8] = "INDEXNAME";
        params[9] = indexName;
        params[10] = "RESULTPAGEXSLT";
        params[11] = resultPageXslt;
        String xsltPath = config.getConfigName()+"/index/"+config.getIndexName(indexName)+"/"+config.getUpdateIndexResultXslt(indexName, resultPageXslt);
        StringBuffer sb = (new GTransformer()).transform(
        		xsltPath,
                resultXml,
                params);
        return sb.toString();
    }
    
    private void createEmpty(
            String indexName,
            StringBuffer resultXml)
    throws java.rmi.RemoteException {
        try {
			iw.deleteAll();
		} catch (IOException e) {
            throw new GenericSearchException("createEmpty deleteAll error indexName=" + indexName+ " :\n", e);
		}
    	if (logger.isDebugEnabled())
    		logger.debug("getIndexWriter indexName=" + indexName+ " docCount=" + docCount);
        resultXml.append("<createEmpty/>\n");
    }
    
    private void deletePid(
            String pid,
            String indexName,
            StringBuffer resultXml)
    throws java.rmi.RemoteException {
    	getIndexReader(indexName, false);
        try {
        	deleteTotal += ir.deleteDocuments(new Term("PID", pid));
		} catch (StaleReaderException e) {
            throw new GenericSearchException("updateIndex deletePid error indexName="+indexName+" pid="+pid+"\n", e);
		} catch (CorruptIndexException e) {
            throw new GenericSearchException("updateIndex deletePid error indexName="+indexName+" pid="+pid+"\n", e);
		} catch (LockObtainFailedException e) {
            throw new GenericSearchException("updateIndex deletePid error indexName="+indexName+" pid="+pid+"\n", e);
        } catch (IOException e) {
            throw new GenericSearchException("updateIndex deletePid error indexName="+indexName+" pid="+pid+"\n", e);
        } finally {
        	closeIndexReader(indexName);
        }
        resultXml.append("<deletePid pid=\""+pid+"\"/>\n");
    }
    
    private void optimize(
            String indexName,
    		StringBuffer resultXml)
    throws java.rmi.RemoteException {
    	try {
            iw.forceMergeDeletes();
		} catch (CorruptIndexException e) {
            throw new GenericSearchException("updateIndex optimize error indexName="+indexName+"\n", e);
        } catch (IOException e) {
            throw new GenericSearchException("updateIndex optimize error indexName="+indexName+"\n", e);
    	}
        resultXml.append("<optimize/>\n");
    }
    
    private void fromFoxmlFiles(
            String filePath,
            String repositoryName,
            String indexName,
            StringBuffer resultXml,
            String indexDocXslt)
    throws java.rmi.RemoteException {
        if (logger.isDebugEnabled())
            logger.debug("fromFoxmlFiles filePath="+filePath+" repositoryName="+repositoryName+" indexName="+indexName);
        File objectDir = null;
        if (filePath==null || filePath.equals(""))
            objectDir = config.getFedoraObjectDir(repositoryName);
        else objectDir = new File(filePath);
        indexDocs(objectDir, repositoryName, indexName, resultXml, indexDocXslt);
        docCount = docCount-warnCount;
        resultXml.append("<warnCount>"+warnCount+"</warnCount>\n");
        resultXml.append("<docCount>"+docCount+"</docCount>\n");
    }
    
    private void indexDocs(
            File file, 
            String repositoryName,
            String indexName,
            StringBuffer resultXml, 
            String indexDocXslt)
    throws java.rmi.RemoteException
    {
		if (file.isHidden()) return;
        if (file.isDirectory())
        {
            String[] files = file.list();
            for (int i = 0; i < files.length; i++) {
                if (i % 100 == 0)
                    logger.info("updateIndex fromFoxmlFiles "+file.getAbsolutePath()
                    		+" indexDirSpace="+indexDirSpace(new File(config.getIndexDir(indexName)))
                    		+" docCount="+docCount);
                indexDocs(new File(file, files[i]), repositoryName, indexName, resultXml, indexDocXslt);
            }
        }
        else
        {
            try {
                indexDoc(file.getName(), repositoryName, indexName, new FileInputStream(file), resultXml, indexDocXslt);
            } catch (RemoteException e) {
                resultXml.append("<warning no=\""+(++warnCount)+"\">file="+file.getAbsolutePath()+" exception="+e.toString()+"</warning>\n");
                logger.warn("<warning no=\""+(warnCount)+"\">file="+file.getAbsolutePath()+" exception="+e.toString()+"</warning>");
            } catch (FileNotFoundException e) {
              resultXml.append("<warning no=\""+(++warnCount)+"\">file="+file.getAbsolutePath()+" exception="+e.toString()+"</warning>\n");
              logger.warn("<warning no=\""+(warnCount)+"\">file="+file.getAbsolutePath()+" exception="+e.toString()+"</warning>");
            }
        }
    }
    
    private void fromPid(
            String pid,
            String repositoryName,
            String indexName,
            StringBuffer resultXml,
            String indexDocXslt)
    throws java.rmi.RemoteException {
    	if (pid==null || pid.length()<1) return;
		getFoxmlFromPid(pid, repositoryName);
        indexDoc(pid, repositoryName, indexName, new ByteArrayInputStream(foxmlRecord), resultXml, indexDocXslt);
    }
    
    private void indexDoc(
    		String pidOrFilename,
    		String repositoryName,
    		String indexName,
    		InputStream foxmlStream,
    		StringBuffer resultXml,
    		String indexDocXslt)
    throws java.rmi.RemoteException {
    	IndexDocumentHandler hdlr = null;
    	String xsltName = indexDocXslt;
    	String[] params = new String[12];
    	int beginParams = indexDocXslt.indexOf("(");
    	if (beginParams > -1) {
    		xsltName = indexDocXslt.substring(0, beginParams).trim();
    		int endParams = indexDocXslt.indexOf(")");
    		if (endParams < beginParams)
    			throw new GenericSearchException("Format error (no ending ')') in indexDocXslt="+indexDocXslt+": ");
    		StringTokenizer st = new StringTokenizer(indexDocXslt.substring(beginParams+1, endParams), ",");
    		params = new String[12+2*st.countTokens()];
    		int i=1; 
    		while (st.hasMoreTokens()) {
    			String param = st.nextToken().trim();
    			if (param==null || param.length()<1)
    				throw new GenericSearchException("Format error (empty param) in indexDocXslt="+indexDocXslt+" params["+i+"]="+param);
    			int eq = param.indexOf("=");
    			if (eq < 0)
    				throw new GenericSearchException("Format error (no '=') in indexDocXslt="+indexDocXslt+" params["+i+"]="+param);
    			String pname = param.substring(0, eq).trim();
    			String pvalue = param.substring(eq+1).trim();
    			if (pname==null || pname.length()<1)
    				throw new GenericSearchException("Format error (no param name) in indexDocXslt="+indexDocXslt+" params["+i+"]="+param);
    			if (pvalue==null || pvalue.length()<1)
                    throw new GenericSearchException("Format error (no param value) in indexDocXslt="+indexDocXslt+" params["+i+"]="+param);
            	params[10+2*i] = pname;
            	params[11+2*i++] = pvalue;
            }
        }
        params[0] = "REPOSITORYNAME";
        params[1] = repositoryName;
        params[2] = "FEDORASOAP";
        params[3] = config.getFedoraSoap(repositoryName);
        params[4] = "FEDORAUSER";
        params[5] = config.getFedoraUser(repositoryName);
        params[6] = "FEDORAPASS";
        params[7] = config.getFedoraPass(repositoryName);
        params[8] = "TRUSTSTOREPATH";
        params[9] = config.getTrustStorePath(repositoryName);
        params[10] = "TRUSTSTOREPASS";
        params[11] = config.getTrustStorePass(repositoryName);
    	String xsltPath = config.getConfigName()+"/index/"+indexName+"/"+config.getUpdateIndexDocXslt(indexName, xsltName);
    	StringBuffer sb = (new GTransformer()).transform(
    			xsltPath, 
    			new StreamSource(foxmlStream),
    			config.getURIResolver(indexName),
    			params);
    	if (logger.isDebugEnabled())
    		logger.debug("IndexDocument=\n"+sb.toString());
    	hdlr = new IndexDocumentHandler(
    			this,
    			repositoryName,
    			pidOrFilename,
    			sb);
		String pid = hdlr.getPid();
    	try {
    		ListIterator li = hdlr.getIndexDocument().getFields().listIterator();
    		if (li.hasNext()) {
                iw.updateDocument(new Term("PID", pid), hdlr.getIndexDocument());
    				updateTotal++;
        			resultXml.append("<updated>"+pid+"</updated>\n");
    			StringBuffer untokenizedFields = new StringBuffer(config.getUntokenizedFields(indexName));
    			while (li.hasNext()) {
    				Field f = (Field)li.next();
    				if (!f.isTokenized() && f.isIndexed() && untokenizedFields.indexOf(f.name())<0) {
    					untokenizedFields.append(" "+f.name());
    					config.setUntokenizedFields(indexName, untokenizedFields.toString());
    				}
    			}
    			logger.info("IndexDocument="+pid);
    		}
    		else {
    			logger.warn("IndexDocument "+pid+" does not contain any IndexFields!!! RepositoryName="+repositoryName+" IndexName="+indexName);
                closeIndexWriter(indexName);
    			deletePid(pid, indexName, resultXml);
                getIndexWriter(indexName);
    		}
    	} catch (IOException e) {
    		throw new GenericSearchException("Update error pidOrFilename="+pidOrFilename, e);
    	}
    }
    
    public Analyzer getAnalyzer(String indexName)
    throws GenericSearchException {
    	return getAnalyzer(indexName, "");
    }
    
    public Analyzer getAnalyzer(String indexName, String className)
    throws GenericSearchException {
    	String analyzerClassName = className;
    	if (analyzerClassName.length()==0) {
        	analyzerClassName= config.getAnalyzer(indexName);
    	}
		String stopwordsLocation = config.getStopwordsLocation(indexName); 
        if (logger.isDebugEnabled())
            logger.debug("getAnalyzer analyzerClassName=" + analyzerClassName+ " stopwordsLocation="+stopwordsLocation);
        Analyzer analyzer = null;
        if ("org.apache.lucene.analysis.KeywordAnalyzer".equals(analyzerClassName)) {
        	analyzer = new KeywordAnalyzer();
        } else {
    		try {
    			Version version = Version.LUCENE_35;
    			Class analyzerClass = Class.forName(analyzerClassName);
                if (logger.isDebugEnabled())
                    logger.debug("getAnalyzer analyzerClass=" + analyzerClass.toString());
    			if (stopwordsLocation == null || stopwordsLocation.equals("")) {
    				analyzer = (Analyzer) analyzerClass.getConstructor(new Class[] { Version.class})
    				.newInstance(new Object[] { version });
    			} else {
    				analyzer = (Analyzer) analyzerClass.getConstructor(new Class[] { Version.class, File.class})
    				.newInstance(new Object[] { version, new File(stopwordsLocation) });
    			}
            } catch (Exception e) {
                throw new GenericSearchException(analyzerClassName
                        + ": instantiation error.\n", e);
            }
        }
        if (logger.isDebugEnabled())
            logger.debug("getAnalyzer analyzer=" + analyzer.toString());
        return analyzer;
    }
    
    public Analyzer getQueryAnalyzer(String indexName)
    throws GenericSearchException {
        if (logger.isDebugEnabled())
            logger.debug("getQueryAnalyzer indexName=" + indexName);
        Analyzer analyzer = getAnalyzer(indexName);
        Map<String,Analyzer> fieldAnalyzers = new HashMap<String, Analyzer>();
        String configFieldAnalyzers = "";
        try {
			configFieldAnalyzers = config.getFieldAnalyzers(indexName);
		} catch (Exception e) {
            throw new ConfigException("getQueryAnalyzer config.getFieldAnalyzers "+" :\n", e);
		}
        if (logger.isDebugEnabled())
            logger.debug("getQueryAnalyzer configFieldAnalyzers=" + configFieldAnalyzers);
        if (configFieldAnalyzers != null && configFieldAnalyzers.length()>0) {
            StringTokenizer stConfigFieldAnalyzers = new StringTokenizer(configFieldAnalyzers);
        	while (stConfigFieldAnalyzers.hasMoreElements()) {
        		String fieldAnalyzer = stConfigFieldAnalyzers.nextToken();
                if (logger.isDebugEnabled())
                    logger.debug("getQueryAnalyzer fieldAnalyzer=" + fieldAnalyzer);
        		int i = fieldAnalyzer.indexOf("::");
        		if (i<0) {
                    throw new ConfigException("getQueryAnalyzer fgsindex.fieldAnalyzer="+fieldAnalyzer+ " missing '::'");
        		}
    			String fieldName = "-";
    			String analyzerClassName = "-";
        		try {
    				fieldName = fieldAnalyzer.substring(0, i);
    				analyzerClassName = fieldAnalyzer.substring(i+2);
    				fieldAnalyzers.put(fieldName, getAnalyzer(indexName, analyzerClassName));
    			} catch (Exception e) {
    	            throw new ConfigException("getQueryAnalyzer getAnalyzer fieldName="+fieldName+" analyzerClassName="+analyzerClassName+" :\n", e);
    			}
        	}
        }
    	StringTokenizer untokenizedFields = new StringTokenizer(config.getUntokenizedFields(indexName));
    	while (untokenizedFields.hasMoreElements()) {
    		String fieldName = untokenizedFields.nextToken();
    		if (!fieldAnalyzers.containsKey(fieldName)) {
        		fieldAnalyzers.put(fieldName, new KeywordAnalyzer());
    		}
    	}
        PerFieldAnalyzerWrapper pfanalyzer = new PerFieldAnalyzerWrapper(analyzer, fieldAnalyzers);
        if (logger.isDebugEnabled())
            logger.debug("getQueryAnalyzer indexName=" + indexName+ " untokenizedFields="+untokenizedFields);
        return pfanalyzer;
    }
    
    private void getIndexReader(String indexName)
    throws GenericSearchException {
    	getIndexReader(indexName, true);
    }
    
    private void getIndexReader(String indexName, boolean readonly)
    throws GenericSearchException {
		IndexReader irreopened = null;
		if (ir != null && readonly) {
	    	try {
				irreopened = IndexReader.openIfChanged(ir, readonly);
			} catch (Exception e) {
				throw new GenericSearchException("IndexReader reopen error indexName=" + indexName+ " :\n", e);
			}
			if (null != irreopened){
				try {
					ir.close();
				} catch (Exception e) {
					ir = null;
					throw new GenericSearchException("IndexReader close after reopen error indexName=" + indexName+ " :\n", e);
				}
				ir = irreopened;
			}
		} else {
	        try {
	        	closeIndexReader(indexName);
				Directory dir = new SimpleFSDirectory(new File(config.getIndexDir(indexName)));
				ir = IndexReader.open(dir, readonly);
			} catch (Exception e) {
				throw new GenericSearchException("IndexReader open error indexName=" + indexName+ " :\n", e);
			}
		}
        docCount = ir.numDocs();
    	if (logger.isDebugEnabled())
    		logger.debug("getIndexReader indexName=" + indexName+ " docCount=" + docCount);
    }
    
    private void closeIndexReader(String indexName)
    throws GenericSearchException {
		if (ir != null) {
            docCount = ir.numDocs();
            try {
                ir.close();
            } catch (Exception e) {
                throw new GenericSearchException("IndexReader close error indexName=" + indexName+ " :\n", e);
            } finally {
            	ir = null;
            	if (logger.isDebugEnabled())
            		logger.debug("closeIndexReader indexName=" + indexName+ " docCount=" + docCount);
            }
		}
    }
    
    private void getIndexWriter(String indexName)
    throws GenericSearchException {
    	if (iw == null) {
    		Directory dir;
    		try {
    			dir = new SimpleFSDirectory(new File(config.getIndexDir(indexName)));
    		} catch (Exception e) {
                throw new GenericSearchException("IndexWriter new error indexName=" + indexName+ " :\n", e);
    		}
    		IndexWriterConfig iwconfig = new IndexWriterConfig(Version.LUCENE_35, getQueryAnalyzer(indexName));
    		int maxBufferedDocs = config.getMaxBufferedDocs(indexName);
    		if (maxBufferedDocs > 0) {
    			iwconfig.setMaxBufferedDocs(maxBufferedDocs);
    		}
    		int mergeFactor = config.getMergeFactor(indexName);
    		if (mergeFactor > 0) {
    			LogDocMergePolicy ldmp = new LogDocMergePolicy();
    			ldmp.setMergeFactor(mergeFactor);
    			iwconfig.setMergePolicy(ldmp);
    		}
    		long defaultWriteLockTimeout = config.getDefaultWriteLockTimeout(indexName);
    		if (defaultWriteLockTimeout > 0) {
    			IndexWriterConfig.setDefaultWriteLockTimeout(defaultWriteLockTimeout);
    		}
    	    try {
                iw = new IndexWriter(dir, iwconfig);
            } catch (Exception e) {
                    throw new GenericSearchException("IndexWriter new error indexName=" + indexName+ " :\n", e);
            }
    	}
        try {
			docCount = iw.numDocs();
		} catch (Exception e) {
			closeIndexWriter(indexName);
            throw new GenericSearchException("IndexWriter numDocs error indexName=" + indexName+ " :\n", e);
		}
    	if (logger.isDebugEnabled())
    		logger.debug("getIndexWriter indexName=" + indexName+ " docCount=" + docCount);
    }
    
    private void closeIndexWriter(String indexName)
    throws GenericSearchException {
		if (iw != null) {
            try {
    			docCount = iw.numDocs();
                iw.close();
            } catch (Exception e) {
                throw new GenericSearchException("IndexWriter close error indexName=" + indexName+ " :\n", e);
            } finally {
            	iw = null;
            	if (logger.isDebugEnabled())
            		logger.debug("closeIndexWriter indexName=" + indexName);
            }
		}
    }
    
    private long indexDirSpace(File dir) {
    	long ids = 0;
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
        	File f = files[i];
            if (f.isDirectory()) {
            	ids += indexDirSpace(f);
            } else {
            	ids += f.length();
            }
        }
		return ids;
    }
    
}