/*
 * <p><b>License and Copyright: </b>The contents of this file is subject to the
 * same open source license as the Fedora Repository System at www.fedora-commons.org
 * Copyright &copy; 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013 by The Technical University of Denmark.
 * All rights reserved.</p>
 */
package dk.defxws.fgssolr;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

import dk.defxws.fedoragsearch.server.GTransformer;
import dk.defxws.fedoragsearch.server.GenericOperationsImpl;
import dk.defxws.fedoragsearch.server.errors.ConfigException;
import dk.defxws.fedoragsearch.server.errors.GenericSearchException;

/**
 * performs the Solr specific parts of the operations
 * 
 * @author  gsp@dtv.dk
 * @version 
 */
public class OperationsImpl extends GenericOperationsImpl {
    
    private static final Logger logger = Logger.getLogger(OperationsImpl.class);
    
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
            getIndexReaderAndSearcher(usingIndexName);
			resultSet = (new Connection()).createStatement().executeQuery(
					searcher,
					usingQuery,
			        hitPageStart,
			        hitPageSize,
			        snippetsMax,
			        fieldMaxLength,
			        getQueryAnalyzer(usingIndexName),
			        config.getDefaultQueryFields(usingIndexName),
			        config.getIndexDir(usingIndexName),
			        usingIndexName,
			        config.getSnippetBegin(usingIndexName),
			        config.getSnippetEnd(usingIndexName),
			        config.getSortFields(usingIndexName, sortFields));
		} catch (Exception e) {
            throw new GenericSearchException("gfindObjects executeQuery error:\n" + e.toString());
        } finally {
            closeIndexReaderAndSearcher(usingIndexName);
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
        StringBuffer resultXml = new StringBuffer();
		optimize(indexName, resultXml);
        int termNo = 0;
        try {
            getIndexReaderAndSearcher(indexName);
            Fields fields;
			try {
				fields = MultiFields.getFields(ir);
			} catch (Exception e) {
              throw new GenericSearchException("MultiFields.getFields error:\n" + e.toString());
			}
            resultXml.append("<fields>");
            if (fields != null) {
            	for(String field : fields) {
                    resultXml.append("<field>"+field+"</field>");
            	}
			}
            resultXml.append("</fields>");
            resultXml.append("<terms>");
            if (fields != null && fieldName != null && fieldName.length()>0) {
                int pageSize = 0;
        	    Terms terms;
				try {
					terms = fields.terms(fieldName);
				} catch (Exception e) {
		              throw new GenericSearchException("fields.terms error:\n" + e.toString());
				}
        	    TermsEnum termsEnum;
				try {
					termsEnum = terms.iterator(null);
				} catch (Exception e) {
		              throw new GenericSearchException("terms.iterator error:\n" + e.toString());
				}
        	    BytesRef text;
        	    try {
					while((text = termsEnum.next()) != null) {
					    termNo++;
					    String termString = text.utf8ToString();
					    if (startTerm.compareTo(termString) <= 0 && pageSize < termPageSize) {
					        pageSize++;
					        resultXml.append("<term no=\""+termNo+"\""
					                +" fieldtermhittotal=\""+termsEnum.docFreq()
					                +"\">"+encode(termString)+"</term>");
					    }
					}
				} catch (Exception e) {
		              throw new GenericSearchException("termsEnum.next error:\n" + e.toString());
				}
            }
            resultXml.append("</terms>");
        } catch (IOException e) {
            throw new GenericSearchException("IndexReader open error:\n" + e.toString());
        } finally {
            closeIndexReaderAndSearcher(indexName);
        }
        resultXml.insert(0, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<solrbrowseindex "+
                "   xmlns:dc=\"http://purl.org/dc/elements/1.1/"+
                "\" startTerm=\""+encode(startTerm)+
                "\" termPageSize=\""+termPageSize+
                "\" fieldName=\""+fieldName+
                "\" indexName=\""+indexName+
                "\" termTotal=\""+termNo+"\">");
        resultXml.append("</solrbrowseindex>");
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
        emptyTotal = 0;
        int initDocCount = 0;
        StringBuffer resultXml = new StringBuffer(); 
        resultXml.append("<solrUpdateIndex");
        resultXml.append(" indexName=\""+indexName+"\"");
        resultXml.append(">\n");
        try {
    		getIndexReaderAndSearcher(indexName);
    		initDocCount = docCount;
        	if ("createEmpty".equals(action)) {
        		createEmpty(indexName, resultXml);
        		initDocCount = 0;
        	}
        	else {
        		if ("deletePid".equals(action))
        			deletePid(value, indexName, resultXml);
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
        	closeIndexReaderAndSearcher(indexName);
        	docCount = initDocCount + insertTotal - deleteTotal;
        }
        logger.info("updateIndex "+action+" indexName="+indexName
        		+" indexDirSpace="+indexDirSpace(new File(config.getIndexDir(indexName)))
        		+" docCount="+docCount);
        resultXml.append("<counts");
        resultXml.append(" insertTotal=\""+insertTotal+"\"");
        resultXml.append(" updateTotal=\""+updateTotal+"\"");
        resultXml.append(" deleteTotal=\""+deleteTotal+"\"");
        resultXml.append(" emptyTotal=\""+emptyTotal+"\"");
        resultXml.append(" docCount=\""+docCount+"\"");
        resultXml.append(" warnCount=\""+warnCount+"\"");
        resultXml.append("/>\n");
        resultXml.append("</solrUpdateIndex>\n");
        if (logger.isDebugEnabled())
            logger.debug("resultXml =\n"+resultXml.toString());
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
        throw new GenericSearchException("createEmpty: Stop solr, remove the index dir, restart solr");
    }
    
    private void deletePid(
            String pid,
            String indexName,
            StringBuffer resultXml)
    throws java.rmi.RemoteException {
        if (logger.isDebugEnabled())
            logger.debug("deletePid indexName="+indexName+" pid="+pid);
        if (pid.length()>0) {
            boolean existed = indexDocExists(pid);
            StringBuffer sb = new StringBuffer("<delete><id>"+pid+"</id></delete>");
            postData(config.getIndexBase(indexName)+"/update", new StringReader(sb.toString()), resultXml);
            if (existed) {
              deleteTotal++;
              docCount--;
            }
            resultXml.append("<deletePid pid=\""+pid+"\"/>\n");
        }
    }
    
    private void optimize(
            String indexName,
    		StringBuffer resultXml)
    throws java.rmi.RemoteException {
        StringBuffer sb = new StringBuffer("<optimize/>");
        postData(config.getIndexBase(indexName)+"/update", new StringReader(sb.toString()), resultXml);
        if (logger.isDebugEnabled())
            logger.debug("optimize indexName="+indexName);
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
                indexDoc(getPidFromObjectFilename(file.getName()), repositoryName, indexName, new FileInputStream(file), resultXml, indexDocXslt);
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
    		String pid,
    		String repositoryName,
    		String indexName,
    		InputStream foxmlStream,
    		StringBuffer resultXml,
    		String indexDocXslt)
    throws java.rmi.RemoteException {
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
    		logger.debug("indexDoc=\n"+sb.toString());
    	if (sb.indexOf("</field>") > 0) {  // skip if no fields
        	postData(config.getIndexBase(indexName)+"/update", new StringReader(sb.toString()), resultXml);
            if (indexDocExists(pid)) {
                updateTotal++;
        		resultXml.append("<updated>"+pid+"</updated>\n");
            } else {
                insertTotal++;
                docCount++;
        		resultXml.append("<inserted>"+pid+"</inserted>\n");
            }
			logger.info("IndexDocument="+pid+" insertTotal="+insertTotal+" updateTotal="+updateTotal+" deleteTotal="+deleteTotal+" emptyTotal="+emptyTotal+" warnCount="+warnCount+" docCount="+docCount);
		}
		else {
			deletePid(pid, indexName, resultXml);
			logger.warn("IndexDocument "+pid+" does not contain any IndexFields!!! RepositoryName="+repositoryName+" IndexName="+indexName);
			emptyTotal++;
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
    			Version version = Version.LUCENE_42;
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

    /**
     * Reads data from the data reader and posts it to solr,
     * writes the response to output
     */
    private void postData(String solrUrlString, Reader data, StringBuffer output)
    throws GenericSearchException {

      URL solrUrl = null;
	try {
		solrUrl = new URL(solrUrlString);
	} catch (MalformedURLException e) {
        throw new GenericSearchException("solrUrl="+solrUrlString+": ", e);
	}
      HttpURLConnection urlc = null;
      String POST_ENCODING = "UTF-8";
      try {
        urlc = (HttpURLConnection) solrUrl.openConnection();
        try {
          urlc.setRequestMethod("POST");
        } catch (ProtocolException e) {
          throw new GenericSearchException("Shouldn't happen: HttpURLConnection doesn't support POST??", e);
        }
        urlc.setDoOutput(true);
        urlc.setDoInput(true);
        urlc.setUseCaches(false);
        urlc.setAllowUserInteraction(false);
        urlc.setRequestProperty("Content-type", "text/xml; charset=" + POST_ENCODING);
        
        OutputStream out = urlc.getOutputStream();
        
        try {
          Writer writer = new OutputStreamWriter(out, POST_ENCODING);
          pipe(data, writer);
          writer.close();
        } catch (IOException e) {
          throw new GenericSearchException("IOException while posting data", e);
        } finally {
          if(out!=null) out.close();
        }
        
        InputStream in = urlc.getInputStream();
        int status = urlc.getResponseCode();
        StringBuffer errorStream = new StringBuffer();
        try {
        	if (status!=HttpURLConnection.HTTP_OK) {
        		errorStream.append("postData URL="+solrUrlString+" HTTP response code="+status+" ");
                throw new GenericSearchException("URL="+solrUrlString+" HTTP response code="+status);
        	}
          Reader reader = new InputStreamReader(in);
          pipeString(reader, output);
          reader.close();
        } catch (IOException e) {
          throw new GenericSearchException("IOException while reading response", e);
        } finally {
          if(in!=null) in.close();
        }
        
        InputStream es = urlc.getErrorStream();
        if (es != null) {
            try {
                Reader reader = new InputStreamReader(es);
                pipeString(reader, errorStream);
                reader.close();
              } catch (IOException e) {
                throw new GenericSearchException("IOException while reading response", e);
              } finally {
                if(es!=null) es.close();
              }
        }
        if (errorStream.length()>0) {
            throw new GenericSearchException("postData error: " + errorStream.toString());
        }
        
      } catch (IOException e) {
          throw new GenericSearchException("Connection error (is Solr running at " + solrUrl + " ?): " + e);
      } finally {
        if(urlc!=null) urlc.disconnect();
      }
    }

    /**
     * Pipes everything from the reader to the writer via a buffer
     */
    private static void pipe(Reader reader, Writer writer) throws IOException {
      char[] buf = new char[1024];
      int read = 0;
      while ( (read = reader.read(buf) ) >= 0) {
        writer.write(buf, 0, read);
      }
      writer.flush();
    }

    /**
     * Pipes everything from the reader to the writer via a buffer
     * except lines starting with '<?'
     */
    private static void pipeString(Reader reader, StringBuffer writer) throws IOException {
      char[] buf = new char[1024];
      int read = 0;
      while ( (read = reader.read(buf) ) >= 0) {
    	  if (!(buf[0]=='<' && buf[1]=='?'))
    		  writer.append(buf, 0, read);
      }
    }
    
    private void getIndexReaderAndSearcher(String indexName)
    throws GenericSearchException {
    	DirectoryReader irreopened = null;
		if (ir != null) {
	    	try {
				irreopened = DirectoryReader.openIfChanged(ir);
			} catch (Exception e) {
				throw new GenericSearchException("IndexReader reopen error indexName=" + indexName+ " :\n", e);
			}
			if (null != irreopened){
				try {
					ir.close();
				} catch (Exception e) {
					ir = null;
					try {
						irreopened.close();
					} catch (Exception e1) {
					}
					throw new GenericSearchException("IndexReader close after reopen error indexName=" + indexName+ " :\n", e);
				}
				ir = irreopened;
			}
		} else {
	        try {
	        	closeIndexReaderAndSearcher(indexName);
				Directory dir = new SimpleFSDirectory(new File(config.getIndexDir(indexName)));
				ir = DirectoryReader.open(dir);
			} catch (Exception e) {
				throw new GenericSearchException("IndexReader open error indexName=" + indexName+ " :\n", e);
			}
			searcher = new IndexSearcher(ir);
		}
        docCount = ir.numDocs();
    	if (logger.isDebugEnabled())
    		logger.debug("getIndexReaderAndSearcher indexName=" + indexName+ " docCount=" + docCount);
    }
    
    private void closeIndexReaderAndSearcher(String indexName)
    throws GenericSearchException {
        searcher = null;
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
