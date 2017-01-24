/*
 * <p><b>License and Copyright: </b>The contents of this file is subject to the
 * same open source license as the Fedora Repository System at www.fedora-commons.org
 * Copyright &copy; 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014, 2015 by The Technical University of Denmark.
 * All rights reserved.</p>
 */
package dk.defxws.fgssolrremote;

import java.io.BufferedReader;
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
import java.net.URL;
import java.rmi.RemoteException;
import java.util.StringTokenizer;

import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;

import dk.defxws.fedoragsearch.server.GTransformer;
import dk.defxws.fedoragsearch.server.GenericOperationsImpl;
import dk.defxws.fedoragsearch.server.errors.GenericSearchException;

import static org.apache.commons.lang3.StringEscapeUtils.escapeXml11;

/**
 * performs the SolrRemote specific parts of the operations
 * 
 * @author  gertsp45@gmail.com
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
        throw new GenericSearchException("gfindObjects: The operation is not available in the SolrRemote plugin, use Solr operations.");
    }
    
    public String browseIndex(
            String startTerm,
            int termPageSize,
            String fieldName,
            String indexName,
            String resultPageXslt)
    throws java.rmi.RemoteException {
        throw new GenericSearchException("browseIndex: The operation is not available in the SolrRemote plugin, use Solr operations.");
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
        StringBuffer resultXml = new StringBuffer();
        try {
        	resultXml = sendToSolr("/select?q=*%3A*&rows=0", null);
		} catch (Exception e) {
	        throw new GenericSearchException("updateIndex sendToSolr:\n"+e);
		}
        int i = resultXml.indexOf("numFound=");
        int j = resultXml.indexOf("\"", i+10);
		String numFound = resultXml.substring(i+10, j);
		try {
			docCount = Integer.parseInt(numFound);
		} catch (NumberFormatException e) {
	        throw new GenericSearchException("updateIndex NumberFormatException numFound="+numFound+"\n"+resultXml, e);
		}
        int initDocCount = 0;
        resultXml = new StringBuffer(); 
        resultXml.append("<solrUpdateIndex");
        resultXml.append(" indexName=\""+indexName+"\"");
        resultXml.append(">\n");
        try {
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
        	docCount = initDocCount + insertTotal - deleteTotal;
        }
        logger.info("updateIndex "+action+" indexName="+indexName
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
            try {
            	sendToSolr("/update", sb.toString());
      		} catch (Exception e) {
      	        throw new GenericSearchException("updateIndex deletePid sendToSolr\n"+e);
      		}
            if (existed) {
              deleteTotal++;
              docCount--;
            }
            resultXml.append("<deletePid pid=\""+pid+"\"/>\n");
        }
    }

    protected boolean indexDocExists(String pid) 
    throws GenericSearchException {
    	boolean indexDocExists = true;
        StringBuffer resultXml = new StringBuffer();
      try {
      	resultXml = sendToSolr("/select?q=PID%3A\""+pid+"\"&rows=0", null);
		} catch (Exception e) {
	        throw new GenericSearchException("updateIndex sendToSolr:\n"+e);
		}
      int i = resultXml.indexOf("numFound=");
      int j = resultXml.indexOf("\"", i+10);
		String numFound = resultXml.substring(i+10, j);
      if (numFound.equals("0")) indexDocExists = false;
        if (logger.isDebugEnabled())
            logger.debug("indexDocExists pid="+pid+" indexDocExists="+indexDocExists);
		return indexDocExists;
    }
    
    private void optimize(
            String indexName,
    		StringBuffer resultXml)
    throws java.rmi.RemoteException {
        throw new GenericSearchException("optimize: The operation is not available in the SolrRemote plugin, use Solr operations.");
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
                    		+" docCount="+docCount);
                indexDocs(new File(file, files[i]), repositoryName, indexName, resultXml, indexDocXslt);
            }
        }
        else
        {
            try {
                indexDoc(getPidFromObjectFilename(file.getName()), repositoryName, indexName, new FileInputStream(file),
                        resultXml, indexDocXslt);
            } catch (RemoteException | FileNotFoundException e) {
                String message = String.format("<warning no=\"%d\">file=%s exception=%s</warning>", 
                        ++warnCount,
                        escapeXml11(file.getAbsolutePath()),
                        escapeXml11(e.toString()));
                resultXml.append(message);
                logger.warn(message);
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
            try {
            	sendToSolr("/update", sb.toString());
    		} catch (Exception e) {
    	        throw new GenericSearchException("updateIndex sendToSolr:\n"+e);
    		}
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
	
	private StringBuffer sendToSolr(String solrCommand, String postParameters) throws Exception {
    	if (logger.isDebugEnabled())
    		logger.debug("sendToSolr solrCommand="+solrCommand+"\nPost parameters=\n" + postParameters);
    	String base = config.getIndexBase(indexName);
		URL url = new URL(base+solrCommand);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestProperty("Content-type", "text/xml; charset=UTF-8");
        if (postParameters != null) {
    		con.setRequestMethod("POST");
    		con.setDoOutput(true);
            OutputStream out = con.getOutputStream();
            Writer writer = new OutputStreamWriter(out, "UTF-8");
            Reader reader = new StringReader(postParameters);
            char[] buf = new char[1024];
            int read = 0;
            while ( (read = reader.read(buf) ) >= 0) {
              writer.write(buf, 0, read);
            }
            writer.flush();
            writer.close();
            out.close();
        }

		int responseCode = con.getResponseCode();
    	if (logger.isDebugEnabled())
    		logger.debug("sendToSolr solrCommand="+solrCommand+" response Code="+responseCode);
 
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
    	if (logger.isDebugEnabled())
    		logger.debug("sendToSolr solrCommand="+solrCommand+"\n response="+response);
    	return response;
	}
        
}
