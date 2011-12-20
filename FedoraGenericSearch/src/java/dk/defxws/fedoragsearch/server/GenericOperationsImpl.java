//$Id$
/*
 * <p><b>License and Copyright: </b>The contents of this file is subject to the
 * same open source license as the Fedora Repository System at www.fedora-commons.org
 * Copyright &copy; 2006, 2007, 2008, 2009, 2010, 2011 by The Technical University of Denmark.
 * All rights reserved.</p>
 */
package dk.defxws.fedoragsearch.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.rmi.RemoteException;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.transform.stream.StreamSource;

import dk.defxws.fedoragsearch.server.errors.ConfigException;
import dk.defxws.fedoragsearch.server.errors.FedoraObjectNotFoundException;
import dk.defxws.fedoragsearch.server.errors.GenericSearchException;

import org.apache.log4j.Logger;

import org.fcrepo.client.FedoraClient;

import org.fcrepo.common.Constants;

import org.fcrepo.server.access.FedoraAPIA;
import org.fcrepo.server.management.FedoraAPIM;
import org.fcrepo.server.types.gen.Datastream;
import org.fcrepo.server.types.gen.MIMETypedStream;

import sun.misc.BASE64Encoder;

/**
 * performs the generic parts of the operations
 * 
 * @author  gsp@dtv.dk
 * @version 
 */
public class GenericOperationsImpl implements Operations {
    
    private static final Logger logger =
        Logger.getLogger(GenericOperationsImpl.class);
    int debuglength = 500;

    private static final Map<String, FedoraClient> fedoraClients = new HashMap<String, FedoraClient>();
    
    protected Map<String, Set<String>> fgsUserAttributes;

    protected String fgsUserName;
    protected String indexName;
    protected Config config;
    protected SearchResultFiltering srf;
    protected int insertTotal = 0;
    protected int updateTotal = 0;
    protected int deleteTotal = 0;
    protected int docCount = 0;
    protected int warnCount = 0;

    protected String usingQuery;
    protected StringBuffer embeddedResult;
    protected byte[] foxmlRecord;
    protected String dsID;
    protected byte[] ds;
    protected String dsText;
    protected String[] params = null;

    private static FedoraClient getFedoraClient(
    		String repositoryName,
    		String fedoraSoap,
    		String fedoraUser,
    		String fedoraPass,
    		String trustStorePath,
    		String trustStorePass)
    throws GenericSearchException {
        if (logger.isDebugEnabled())
            logger.debug("getFedoraClient repositoryName="+repositoryName+" fedoraSoap="+fedoraSoap+" fedoraUser="+fedoraUser+" fedoraPass="+fedoraPass);
    	if (trustStorePath!=null)
    		System.setProperty("javax.net.ssl.trustStore", trustStorePath);
    	if (trustStorePass!=null)
    		System.setProperty("javax.net.ssl.trustStorePassword", trustStorePass);
        try {
            String baseURL = getBaseURL(fedoraSoap);
            String user = fedoraUser; 
            String clientId = user + "@" + baseURL;
            synchronized (fedoraClients) {
                if (fedoraClients.containsKey(clientId)) {
                    return fedoraClients.get(clientId);
                } else {
                    FedoraClient client = new FedoraClient(baseURL,
                            user, fedoraPass);
                    fedoraClients.put(clientId, client);
                    return client;
                }
            }
        } catch (Exception e) {
            throw new GenericSearchException("Error getting FedoraClient"
                    + " for repository: " + repositoryName, e);
        }
    }

    private static String getBaseURL(String fedoraSoap)
            throws Exception {
        final String end = "/services";
        String baseURL = fedoraSoap;
        if (fedoraSoap.endsWith(end)) {
            return fedoraSoap.substring(0, fedoraSoap.length() - end.length());
        } else {
            throw new Exception("Unable to determine baseURL from fedoraSoap"
                    + " value (expected it to end with '" + end + "'): "
                    + fedoraSoap);
        }
    }

    private static FedoraAPIA getAPIA(
    		String repositoryName,
    		String fedoraSoap,
    		String fedoraUser,
    		String fedoraPass,
    		String trustStorePath,
    		String trustStorePass)
    throws GenericSearchException {
    	FedoraClient client = getFedoraClient(repositoryName, fedoraSoap, fedoraUser, fedoraPass, trustStorePath, trustStorePass);
    	try {
    		return client.getAPIA();
    	} catch (Exception e) {
    		throw new GenericSearchException("Error getting API-A stub"
    				+ " for repository: " + repositoryName, e);
    	}
    }
    
    private static FedoraAPIM getAPIM(
    		String repositoryName,
    		String fedoraSoap,
    		String fedoraUser,
    		String fedoraPass,
    		String trustStorePath,
    		String trustStorePass)
    throws GenericSearchException {
    	FedoraClient client = getFedoraClient(repositoryName, fedoraSoap, fedoraUser, fedoraPass, trustStorePath, trustStorePass);
    	try {
    		return client.getAPIM();
    	} catch (Exception e) {
    		throw new GenericSearchException("Error getting API-M stub"
    				+ " for repository: " + repositoryName, e);
    	}
    }
    
    public void init(String indexName, Config currentConfig) {
    	init(null, indexName, currentConfig);
    }
    
    public void init(String fgsUserName, String indexName, Config currentConfig) {
    	init(null, indexName, currentConfig, null);
    }
    
    public void init(String fgsUserName, String indexName, Config currentConfig, Map<String, Set<String>> fgsUserAttributes) {
    	this.fgsUserName = fgsUserName;
    	this.indexName = indexName;
    	this.fgsUserAttributes = fgsUserAttributes;
        config = currentConfig;
        if (null==this.fgsUserName || this.fgsUserName.length()==0) {
        	try {
				this.fgsUserName = config.getProperty("fedoragsearch.testUserName");
			} catch (ConfigException e) {
				this.fgsUserName = "fedoragsearch.testUserName";
			}
        }
    }

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
        
        if (logger.isDebugEnabled())
            logger.debug("gfindObjects" +
                    " query="+query+
                    " hitPageStart="+hitPageStart+
                    " hitPageSize="+hitPageSize+
                    " snippetsMax="+snippetsMax+
                    " fieldMaxLength="+fieldMaxLength+
                    " indexName="+indexName+
                    " sortFields="+sortFields+
                    " resultPageXslt="+resultPageXslt);
        srf = config.getSearchResultFiltering();
        params = new String[18];
        params[0] = "OPERATION";
        params[1] = "gfindObjects";
        params[2] = "QUERY";
        params[3] = query;
        params[4] = "HITPAGESTART";
        params[5] = Integer.toString(hitPageStart);
        params[6] = "HITPAGESIZE";
        params[7] = Integer.toString(hitPageSize);
        params[8] = "INDEXNAME";
        params[9] = indexName;
        params[10] = "SORTFIELDS";
        params[11] = sortFields;
        params[14] = "FGSUSERNAME";
        params[15] = fgsUserName;
        params[16] = "SRFTYPE";
        params[17] = config.getSearchResultFilteringType();
        embeddedResult = new StringBuffer();
        usingQuery = handleEmbeddedQueries("", query);
        return "";
    }

    private String handleEmbeddedQueries(String embedType, String query) 
    throws GenericSearchException {
    	String newQuery = query;
        if (logger.isDebugEnabled())
            logger.debug("handleEmbeddedQueries embedType="+embedType+" query="+query);
        String beginString = "(::";
        String beginEndString = "::";
        String endString = "::)";
        String endBeginString = "::";
        int beginLength = beginString.length();
        int endLength = endString.length();
        int beginIndex = query.indexOf(beginString);
        int endIndex = query.indexOf(endString);
        String embeddedEmbedType = "UNKNOWN";
        if (logger.isDebugEnabled())
            logger.debug("handleEmbeddedQueries beginIndex="+beginIndex+" endIndex="+endIndex+" newQuery=\n"+newQuery);
    	while (beginIndex > -1 && endIndex > -1 && beginIndex < endIndex) {
    		int beginEndIndex = newQuery.indexOf(beginEndString, beginIndex+beginLength);
    		embeddedEmbedType = newQuery.substring(beginIndex+beginLength, beginEndIndex);
    		endIndex = newQuery.indexOf(endBeginString+embeddedEmbedType+endString, beginEndIndex);
    		if (endIndex == -1) {
                throw new GenericSearchException("handleEmbeddedQueries: No end for embedType="+embeddedEmbedType+" found."+" newQuery=\n"+newQuery);
    		}
    		String embeddedQuery = newQuery.substring(beginEndIndex+beginEndString.length(), endIndex);
    		String newQueryPart = embeddedQuery;
    		if (embeddedQuery.indexOf(beginString) > -1) {
        		newQueryPart = handleEmbeddedQueries(embeddedEmbedType, embeddedQuery);
    		}
    		String decodedEmbeddedQuery = "";
    		try {
				decodedEmbeddedQuery = URLDecoder.decode(embeddedQuery, "UTF-8");
			} catch (UnsupportedEncodingException e) {
	            throw new GenericSearchException("handleEmbeddedQueries decode exception="+e.toString());
			}
			newQueryPart = processEmbeddedQuery(embeddedEmbedType, decodedEmbeddedQuery);
    		newQuery = newQuery.substring(0,beginIndex) + newQueryPart + newQuery.substring(endIndex+(endBeginString+embeddedEmbedType+endString).length());
            beginIndex = newQuery.indexOf(beginString);
            endIndex = newQuery.indexOf(endString);
            if (logger.isDebugEnabled())
                logger.debug("handleEmbeddedQueries embeddedEmbedType="+embeddedEmbedType+" beginIndex="+beginIndex+" endIndex="+endIndex+" newQuery=\n"+newQuery);
    	}
    	if ("".equals(embedType) && query.indexOf(beginString) == 0) {
    		embeddedResult = new StringBuffer(newQuery);
    		newQuery = "";
    	}
    	return newQuery;
    }

    private String processEmbeddedQuery(String embedType, String embeddedQuery) 
    throws GenericSearchException {
    	// embeddedQuery :: [[["reposName/"<reposName>"/"]["indexName/"<indexName>"/"]]["xsltName"/<xsltName>]["?"]]<parameters>
        if (logger.isDebugEnabled())
            logger.debug("processEmbeddedQuery embedType="+embedType+" embeddedQuery="+embeddedQuery);
        String firstPart = "";
        String secondPart = embeddedQuery;
        int i = embeddedQuery.indexOf("?");
        int j;
        if (i == -1) {
//        	secondPart = "?" + secondPart;
        } else {
        	firstPart = embeddedQuery.substring(0, i);
        	secondPart = embeddedQuery.substring(i);
        }
    	String embeddedRepositoryName = config.getRepositoryName(null);
    	String embeddedIndexName = config.getIndexName(null);
    	String embeddedXsltName = "copyXml";
    	if (firstPart.length() > 0) {
    		StringTokenizer st = new StringTokenizer(firstPart, "/");
    		while (st.hasMoreTokens()) {
    			String t = st.nextToken();
    			String tv = "";
    			if (st.hasMoreTokens()) {
    				tv = st.nextToken();
    			}
    			if ("reposName".equals(t)) {
    				embeddedRepositoryName = tv;
    			}
    			if ("indexName".equals(t)) {
    				embeddedIndexName = tv;
    			}
    			if ("xsltName".equals(t)) {
    				embeddedXsltName = tv;
    			}
    		}
    	}
        if (logger.isDebugEnabled())
            logger.debug("processEmbeddedQuery embeddedRepositoryName="+embeddedRepositoryName+" embeddedIndexName="+embeddedIndexName+" embeddedXsltName="+embeddedXsltName);
		String baseUrl = firstPart;
		String userPassword = "";
		if ("GSEARCH".equals(embedType)) {
			try {
				baseUrl = getBaseURL(config.getSoapBase())+"/rest";
				userPassword = config.getSoapUser()+":"+config.getSoapPass();
			} catch (Exception e) {
	            throw new GenericSearchException("processEmbeddedQuery getBaseURL exception=\n"+e.toString());
			}
		} else if ("RISEARCH".equals(embedType)) {
			baseUrl = config.getFedoraSoap(embeddedRepositoryName);
			userPassword = config.getFedoraUser(embeddedRepositoryName)+":"+config.getFedoraPass(embeddedRepositoryName);
		} else if ("SOLR".equals(embedType)) {
			try {
				baseUrl = config.getIndexBase(embeddedIndexName)+"/select";
			} catch (Exception e) {
	            throw new GenericSearchException("processEmbeddedQuery embeddedIndexName="+embeddedIndexName+" hasnoSolrserver exception=\n"+e.toString());
			}
			userPassword = config.getSoapUser()+":"+config.getSoapPass();
			String queryContents = "";
			i = secondPart.indexOf("q=");
			j = -1;
			if (i > -1) {
				j = secondPart.indexOf("&", i+2);
				if (j == -1) {
					j = secondPart.length();
				}
				queryContents = secondPart.substring(i+2, j);
			}
			if (i == -1 || queryContents.length() == 0) {
	            throw new GenericSearchException("processEmbeddedQuery: No query contents found?"+" finalQuery=\n"+secondPart);
			}
			try {
				queryContents = URLEncoder.encode(queryContents, "UTF-8");
			} catch (UnsupportedEncodingException e) {
	            throw new GenericSearchException(e.toString());
			}
			secondPart = secondPart.substring(0, i+2) + queryContents + secondPart.substring(j);
		}
        String urlString = baseUrl+"?"+secondPart;
        if (logger.isDebugEnabled())
            logger.debug("processEmbeddedQuery userPassword="+userPassword+" url=\n"+urlString);
		URL url = null;
		try {
			url = new URL(urlString);
		} catch (MalformedURLException e) {
            throw new GenericSearchException(e.toString());
		}
        InputStream content = null;
        URLConnection conn = null;
        try {
			conn = url.openConnection();
		} catch (IOException e) {
	          throw new GenericSearchException("processEmbeddedQuery url.openConnection() exception="+e.toString());
		}
        conn.setRequestProperty("Authorization", 
        		"Basic "+(new BASE64Encoder()).encode(userPassword.getBytes()));
        try {
			conn.connect();
		} catch (IOException e) {
	          throw new GenericSearchException("processEmbeddedQuery conn.connect() exception="+e.toString());
		}
        try {
			content = (InputStream) conn.getContent();
		} catch (IOException e) {
          throw new GenericSearchException("processEmbeddedQuery conn.getContent() exception="+e.toString());
		}
        if (logger.isDebugEnabled())
            logger.debug("processEmbeddedQuery after get content");
        params = new String[12];
        params[0] = "OPERATION";
        params[1] = "gfindObjects";
        params[2] = "ACTION";
        params[3] = "processEmbeddedQuery";
        params[4] = "VALUE";
        params[5] = urlString;
        params[6] = "REPOSITORYNAME";
        params[7] = embeddedRepositoryName;
        params[8] = "INDEXNAME";
        params[9] = embeddedIndexName;
        params[10] = "RESULTPAGEXSLT";
        params[11] = embeddedXsltName;
        String xsltPath = config.getConfigName()+"/rest/"+embeddedXsltName;
        StringBuffer resultXml = (new GTransformer()).transform(
        		xsltPath,
        		new StreamSource(content),
    			config.getURIResolver(embeddedIndexName),
                params);
//        i = resultXml.indexOf("<?xml");
//        if (i>-1) {
//            j = resultXml.indexOf("?>", i);
//            if (j > -1) {
//            	resultXml.delete(0, j+2);
//            }
//        }
        String newQueryPart = resultXml.toString();
        i = resultXml.indexOf("newQueryPart>");
        if (i>-1) {
            j = resultXml.indexOf("</newQueryPart", i);
            if (j > -1) {
                newQueryPart = resultXml.substring(i, j);
            }
        }
        if (logger.isDebugEnabled())
            logger.debug("processEmbeddedQuery newQueryPart=\n"+newQueryPart);
    	return newQueryPart;
    }
    
    public String browseIndex(
            String startTerm,
            int termPageSize,
            String fieldName,
            String indexName,
            String resultPageXslt)
    throws java.rmi.RemoteException {
        
        if (logger.isDebugEnabled())
            logger.debug("browseIndex" +
                    " startTerm="+startTerm+
                    " termPageSize="+termPageSize+
                    " fieldName="+fieldName+
                    " indexName="+indexName+
                    " resultPageXslt="+resultPageXslt);
        params = new String[12];
        params[0] = "OPERATION";
        params[1] = "browseIndex";
        params[2] = "STARTTERM";
        params[3] = startTerm;
        params[4] = "TERMPAGESIZE";
        params[5] = Integer.toString(termPageSize);
        params[6] = "INDEXNAME";
        params[7] = indexName;
        params[8] = "FIELDNAME";
        params[9] = fieldName;
        return "";
    }
    
    public String getRepositoryInfo(
            String repositoryName,
            String resultPageXslt) throws java.rmi.RemoteException {
        if (logger.isDebugEnabled())
            logger.debug("getRepositoryInfo" +
                    " repositoryName="+repositoryName+
                    " resultPageXslt="+resultPageXslt);
        InputStream repositoryStream =  null;
        String repositoryInfoPath = "/"+config.getConfigName()+"/repository/"+config.getRepositoryName(repositoryName)+"/repositoryInfo.xml";
        try {
            repositoryStream =  this.getClass().getResourceAsStream(repositoryInfoPath);
            if (repositoryStream == null) {
                throw new GenericSearchException("Error "+repositoryInfoPath+" not found in classpath");
            }
        } catch (IOException e) {
            throw new GenericSearchException("Error "+repositoryInfoPath+" not found in classpath", e);
        }
        String xsltPath = config.getConfigName()
        		+"/repository/"+config.getRepositoryName(repositoryName)+"/"
        		+config.getRepositoryInfoResultXslt(repositoryName, resultPageXslt);
        StringBuffer sb = (new GTransformer()).transform(
        		xsltPath,
                new StreamSource(repositoryStream),
                new String[] {});
        return sb.toString();
    }
    
    public String getIndexInfo(
            String indexName,
            String resultPageXslt) throws java.rmi.RemoteException {
        if (logger.isDebugEnabled())
            logger.debug("getIndexInfo" +
                    " indexName="+indexName+
                    " resultPageXslt="+resultPageXslt);
        return "";
    }
    
    public String updateIndex(
            String action,
            String value,
            String repositoryNameParam,
            String indexNames,
            String indexDocXslt,
            String resultPageXslt)
    throws java.rmi.RemoteException {
        if (logger.isDebugEnabled())
            logger.debug("updateIndex" +
                    " action="+action+
                    " value="+value+
                    " repositoryName="+repositoryNameParam+
                    " indexNames="+indexNames+
                    " indexDocXslt="+indexDocXslt+
                    " resultPageXslt="+resultPageXslt);
        StringBuffer resultXml = new StringBuffer(); 
        String repositoryName = repositoryNameParam;
        if (repositoryNameParam==null || repositoryNameParam.equals(""))
        	repositoryName = config.getRepositoryName(repositoryName);
        resultXml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        resultXml.append("<resultPage");
        resultXml.append(" operation=\"updateIndex\"");
        resultXml.append(" action=\""+action+"\"");
        resultXml.append(" value=\""+value+"\"");
        resultXml.append(" repositoryName=\""+repositoryName+"\"");
        resultXml.append(" indexNames=\""+indexNames+"\"");
        resultXml.append(" resultPageXslt=\""+resultPageXslt+"\"");
        resultXml.append(" dateTime=\""+new Date()+"\"");
        resultXml.append(">\n");
        StringTokenizer st = new StringTokenizer(config.getIndexNames(indexNames));
        while (st.hasMoreTokens()) {
            String indexName = st.nextToken();
            Operations ops = config.getOperationsImpl(fgsUserName, indexName);
            resultXml.append(ops.updateIndex(action, value, repositoryName, indexName, indexDocXslt, resultPageXslt));
        }
        resultXml.append("</resultPage>\n");
        if (logger.isDebugEnabled())
            logger.debug("resultXml="+resultXml);
        return resultXml.toString();
    }
    
    public void getFoxmlFromPid(
            String pid,
            String repositoryName)
    throws java.rmi.RemoteException {
        
        if (logger.isInfoEnabled())
            logger.info("getFoxmlFromPid" +
                    " pid="+pid +
                    " repositoryName="+repositoryName);
        FedoraAPIM apim = getAPIM(repositoryName, 
        		config.getFedoraSoap(repositoryName), 
        		config.getFedoraUser(repositoryName), 
        		config.getFedoraPass(repositoryName), 
        		config.getTrustStorePath(repositoryName), 
        		config.getTrustStorePass(repositoryName) );
        
        String fedoraVersion = config.getFedoraVersion(repositoryName);
        String format = Constants.FOXML1_1.uri;
        if(fedoraVersion != null && fedoraVersion.startsWith("2.")) {
            format = Constants.FOXML1_0_LEGACY;
        }
        String realPID = getRealPID(pid);
        try {
        	foxmlRecord = apim.export(realPID, format, "public");
        } catch (RemoteException e) {
        	throw new FedoraObjectNotFoundException("Fedora Object "+realPID+" not found at "+repositoryName, e);
        }
    }
    
    public String getDatastreamText(
            String pid,
            String repositoryName,
            String dsId)
    throws GenericSearchException {
    	return getDatastreamText(pid, repositoryName, dsId,
                		config.getFedoraSoap(repositoryName), 
                		config.getFedoraUser(repositoryName), 
                		config.getFedoraPass(repositoryName), 
                		config.getTrustStorePath(repositoryName), 
                		config.getTrustStorePass(repositoryName) );
    }
    
    public String getDatastreamText(
            String pid,
            String repositoryName,
            String dsId,
    		String fedoraSoap,
    		String fedoraUser,
    		String fedoraPass,
    		String trustStorePath,
    		String trustStorePass) {
        if (logger.isInfoEnabled())
            logger.info("getDatastreamText"
            		+" pid="+pid
            		+" repositoryName="+repositoryName
            		+" dsId="+dsId
            		+" fedoraSoap="+fedoraSoap
            		+" fedoraUser="+fedoraUser
            		+" fedoraPass="+fedoraPass
            		+" trustStorePath="+trustStorePath
            		+" trustStorePass="+trustStorePass);
        StringBuffer dsBuffer = new StringBuffer();
        String mimetype = "";
        ds = null;
        if (dsId != null) {
            try {
                FedoraAPIA apia = getAPIA(
                		repositoryName, 
                		fedoraSoap, 
                		fedoraUser,
                		fedoraPass,
                		trustStorePath,
                		trustStorePass );
                MIMETypedStream mts = apia.getDatastreamDissemination(getRealPID(pid), 
                        dsId, null);
                if (mts==null) return "";
                ds = mts.getStream();
                mimetype = mts.getMIMEType().split(";")[0]; // MIMETypedStream can include encoding, eg "text/xml;charset=utf-8" - split this off
            } catch (Exception e) {
            	return emptyIndexField("getDatastreamText mimetype", pid, dsId, mimetype, e);
            }
            if (logger.isDebugEnabled())
                logger.debug("getDatastreamText" +
                        " pid="+pid+
                        " dsId="+dsId+
                        " mimetype="+mimetype);
        	TransformerToText transformerToText = null;
            if (ds != null) {
            	try {
					transformerToText = new TransformerToText();
				} catch (Exception e) {
	            	return emptyIndexField("getDatastreamText TransformerToText", pid, dsId, mimetype, e);
				}
	            if (logger.isDebugEnabled())
	                logger.debug("getDatastreamText" +
	                        " pid="+pid+
	                        " dsId="+dsId+
	                        " TransformerToText="+transformerToText);
                try {
					dsBuffer = transformerToText.getText(ds, mimetype);
				} catch (Exception e) {
		            if (logger.isDebugEnabled())
		                logger.debug("getDatastreamText" +
		                        " pid="+pid+
		                        " dsId="+dsId+
		                        " TransformerToText="+transformerToText+
		                        " Exception="+e);
	            	return emptyIndexField("getDatastreamText getText", pid, dsId, mimetype, e);
				} finally {
		            if (logger.isDebugEnabled())
		                logger.debug("getDatastreamText finally " +
		                        " pid="+pid+
		                        " dsId="+dsId);
				}
            }
        }
        if (logger.isDebugEnabled())
            logger.debug("getDatastreamText" +
                    " pid="+pid+
                    " dsId="+dsId+
                    " mimetype="+mimetype+
                    " dsBuffer="+getDebugString(dsBuffer.toString()));
        return dsBuffer.toString();
    }
    
    public StringBuffer getFirstDatastreamText(
            String pid,
            String repositoryName,
            String dsMimetypes)
    throws GenericSearchException {
    	return getFirstDatastreamText(pid, repositoryName, dsMimetypes,
            		config.getFedoraSoap(repositoryName), 
            		config.getFedoraUser(repositoryName), 
            		config.getFedoraPass(repositoryName), 
            		config.getTrustStorePath(repositoryName), 
            		config.getTrustStorePass(repositoryName));
    }
    
    public StringBuffer getFirstDatastreamText(
            String pid,
            String repositoryName,
            String dsMimetypes,
    		String fedoraSoap,
    		String fedoraUser,
    		String fedoraPass,
    		String trustStorePath,
    		String trustStorePass)
    throws GenericSearchException {
        if (logger.isInfoEnabled())
            logger.info("getFirstDatastreamText"
                    +" pid="+pid
            		+" dsMimetypes="+dsMimetypes
            		+" fedoraSoap="+fedoraSoap
            		+" fedoraUser="+fedoraUser
            		+" fedoraPass="+fedoraPass
            		+" trustStorePath="+trustStorePath
            		+" trustStorePass="+trustStorePass);
        StringBuffer dsBuffer = new StringBuffer();
        String mimetype = "";
        Datastream[] dsds = null;
        try {
            FedoraAPIM apim = getAPIM(
            		repositoryName, 
            		fedoraSoap, 
            		fedoraUser,
            		fedoraPass,
            		trustStorePath,
            		trustStorePass );
            dsds = apim.getDatastreams(getRealPID(pid), null, "A");
        } catch (Exception e) {
        	return new StringBuffer(emptyIndexField("getFirstDatastreamText", pid, "", mimetype, e));
        }
        String mimetypes = config.getMimeTypes();
        if (dsMimetypes!=null && dsMimetypes.length()>0)
            mimetypes = dsMimetypes;
        dsID = null;
        if (dsds != null) {
            int best = 99999;
            for (int i = 0; i < dsds.length; i++) {
                int j = mimetypes.indexOf(dsds[i].getMIMEType());
                if (j > -1 && best > j) {
                    dsID = dsds[i].getID();
                    best = j;
                    mimetype = dsds[i].getMIMEType().split(";")[0]; // MIMETypedStream can include encoding, eg "text/xml;charset=utf-8" - split this off
                }
            }
        }
        ds = null;
        if (dsID != null) {
            try {
                FedoraAPIA apia = getAPIA(
                		repositoryName, 
                		fedoraSoap, 
                		fedoraUser,
                		fedoraPass,
                		trustStorePath,
                		trustStorePass );
                MIMETypedStream mts = apia.getDatastreamDissemination(getRealPID(pid), 
                        dsID, null);
                ds = mts.getStream();
                mimetype = mts.getMIMEType().split(";")[0]; // MIMETypedStream can include encoding, eg "text/xml;charset=utf-8" - split this off
            } catch (Exception e) {
            	return new StringBuffer(emptyIndexField("getFirstDatastreamText", pid, dsID, mimetype, e));
            }
        }
        if (ds != null) {
            dsBuffer = (new TransformerToText().getText(ds, mimetype));
        }
        if (logger.isDebugEnabled())
            logger.debug("getFirstDatastreamText" +
                    " pid="+pid+
                    " dsID="+dsID+
                    " mimetype="+mimetype+
                    " dsBuffer="+dsBuffer.toString());
        return dsBuffer;
    }
    
    public StringBuffer getDisseminationText(
            String pid,
            String repositoryName,
            String bDefPid, 
            String methodName, 
            String parameters, 
            String asOfDateTime)
    throws GenericSearchException {
    	return getDisseminationText(pid, repositoryName, bDefPid, methodName, parameters, asOfDateTime,
                		config.getFedoraSoap(repositoryName), 
                		config.getFedoraUser(repositoryName), 
                		config.getFedoraPass(repositoryName), 
                		config.getTrustStorePath(repositoryName), 
                		config.getTrustStorePass(repositoryName) );
    }
    
    public StringBuffer getDisseminationText(
            String pid,
            String repositoryName,
            String bDefPid, 
            String methodName, 
            String parameters, 
            String asOfDateTime,
    		String fedoraSoap,
    		String fedoraUser,
    		String fedoraPass,
    		String trustStorePath,
    		String trustStorePass)
    throws GenericSearchException {
        if (logger.isInfoEnabled())
            logger.info("getDisseminationText" +
                    " pid="+pid+
                    " bDefPid="+bDefPid+
                    " methodName="+methodName+
                    " parameters="+parameters+
                    " asOfDateTime="+asOfDateTime
            		+" fedoraSoap="+fedoraSoap
            		+" fedoraUser="+fedoraUser
            		+" fedoraPass="+fedoraPass
            		+" trustStorePath="+trustStorePath
            		+" trustStorePass="+trustStorePass);
        StringTokenizer st = new StringTokenizer(parameters);
        org.fcrepo.server.types.gen.Property[] params = new org.fcrepo.server.types.gen.Property[st.countTokens()];
        for (int i=0; i<st.countTokens(); i++) {
            String param = st.nextToken();
            String[] nameAndValue = param.split("=");
            params[i] = new org.fcrepo.server.types.gen.Property(nameAndValue[0], nameAndValue[1]);
        }
        if (logger.isDebugEnabled())
            logger.debug("getDisseminationText" +
                    " #parameters="+params.length);
        StringBuffer dsBuffer = new StringBuffer();
        String mimetype = "";
        ds = null;
        if (pid != null) {
            try {
                FedoraAPIA apia = getAPIA(
                		repositoryName, 
                		fedoraSoap, 
                		fedoraUser,
                		fedoraPass,
                		trustStorePath,
                		trustStorePass );
                MIMETypedStream mts = apia.getDissemination(getRealPID(pid), bDefPid, 
                        methodName, params, asOfDateTime);
                if (mts==null) {
                    throw new GenericSearchException("getDissemination returned null");
                }
                ds = mts.getStream();
                mimetype = mts.getMIMEType().split(";")[0]; // MIMETypedStream can include encoding, eg "text/xml;charset=utf-8" - split this off
                if (logger.isDebugEnabled())
                    logger.debug("getDisseminationText" +
                            " mimetype="+mimetype);
            } catch (Exception e) {
            	return new StringBuffer(emptyIndexField("getDisseminationText", pid, bDefPid, mimetype, e));
            }
        }
        if (ds != null) {
            dsBuffer = (new TransformerToText().getText(ds, mimetype));
        }
        if (logger.isDebugEnabled())
            logger.debug("getDisseminationText" +
                    " pid="+pid+
                    " bDefPid="+bDefPid+
                    " mimetype="+mimetype+
                    " dsBuffer="+dsBuffer.toString());
        return dsBuffer;
    }
    
    public String getDatastreamTextFromTika(
            String pid,
            String repositoryName,
            String dsId,
            String indexFieldTagName, 
            String textIndexField,
    		String fedoraSoap,
    		String fedoraUser,
    		String fedoraPass,
    		String trustStorePath,
    		String trustStorePass) {
    	return getDatastreamFromTika(pid, repositoryName, dsId, indexFieldTagName, textIndexField, null, null, fedoraSoap, fedoraUser, fedoraPass, trustStorePath, trustStorePass);
    }
    
    public String getDatastreamMetadataFromTika(
            String pid,
            String repositoryName,
            String dsId,
            String indexFieldTagName, 
            String indexFieldNamePrefix, 
            String selectedFields,
    		String fedoraSoap,
    		String fedoraUser,
    		String fedoraPass,
    		String trustStorePath,
    		String trustStorePass) {
    	return getDatastreamFromTika(pid, repositoryName, dsId, indexFieldTagName, null, indexFieldNamePrefix, selectedFields, fedoraSoap, fedoraUser, fedoraPass, trustStorePath, trustStorePass);
    }
    
    public String getDatastreamFromTika(
            String pid,
            String repositoryName,
            String dsId,
            String indexFieldTagName, 
            String textIndexField,
            String indexFieldNamePrefix, 
            String selectedFields,
    		String fedoraSoap,
    		String fedoraUser,
    		String fedoraPass,
    		String trustStorePath,
    		String trustStorePass) {
        if (logger.isInfoEnabled())
            logger.info("getDatastreamFromTika"
            		+" pid="+pid
            		+" repositoryName="+repositoryName
            		+" dsId="+dsId
            		+" indexFieldTagName="+indexFieldTagName
            		+" textIndexField="+textIndexField
            		+" indexFieldNamePrefix="+indexFieldNamePrefix
            		+" selectedFields="+selectedFields
            		+" fedoraSoap="+fedoraSoap
            		+" fedoraUser="+fedoraUser
            		+" fedoraPass="+fedoraPass
            		+" trustStorePath="+trustStorePath
            		+" trustStorePass="+trustStorePass);
        StringBuffer dsBuffer = new StringBuffer();
        String mimetype = "";
        ds = null;
        if (dsId != null) {
            try {
                FedoraAPIA apia = getAPIA(
                		repositoryName, 
                		fedoraSoap, 
                		fedoraUser,
                		fedoraPass,
                		trustStorePath,
                		trustStorePass );
                MIMETypedStream mts = apia.getDatastreamDissemination(getRealPID(pid), 
                        dsId, null);
                if (mts==null) return "";
                ds = mts.getStream();
                mimetype = mts.getMIMEType().split(";")[0]; // MIMETypedStream can include encoding, eg "text/xml;charset=utf-8" - split this off
            } catch (Exception e) {
            	return emptyIndexField("getDatastreamFromTika mimetype", pid, dsId, mimetype, e);
            }
            if (logger.isDebugEnabled())
                logger.debug("getDatastreamFromTika" +
                        " pid="+pid+
                        " dsId="+dsId+
                        " mimetype="+mimetype);
        	TransformerToText transformerToText = null;
            if (ds != null) {
            	try {
					transformerToText = new TransformerToText();
				} catch (Exception e) {
	            	return emptyIndexField("getDatastreamFromTika TransformerToText", pid, dsId, mimetype, e);
				}
	            if (logger.isDebugEnabled())
	                logger.debug("getDatastreamFromTika" +
	                        " pid="+pid+
	                        " dsId="+dsId+
	                        " TransformerToText="+transformerToText);
                try {
					dsBuffer = transformerToText.getFromTika(repositoryName+"/"+pid+"/"+dsId, ds, indexFieldTagName, textIndexField, indexFieldNamePrefix, selectedFields);
				} catch (Exception e) {
		            if (logger.isDebugEnabled())
		                logger.debug("getDatastreamFromTika" +
		                        " pid="+pid+
		                        " dsId="+dsId+
		                        " TransformerToText="+transformerToText+
		                        " Exception="+e);
	            	return emptyIndexField("getDatastreamFromTika getText", pid, dsId, mimetype, e);
				} finally {
		            if (logger.isDebugEnabled())
		                logger.debug("getDatastreamFromTika finally " +
		                        " pid="+pid+
		                        " dsId="+dsId);
				}
            }
        }
        if (logger.isDebugEnabled())
            logger.debug("getDatastreamFromTika" +
                    " pid="+pid+
                    " dsId="+dsId+
                    " mimetype="+mimetype+
                    " dsBuffer="+getDebugString(dsBuffer.toString()));
        return dsBuffer.toString();
    }
    
    private String emptyIndexField(
    			String methodName, 
    			String pid,
    			String dsId,
    			String mimetype,
    			Exception e) {
//    	no exception to be thrown,
//    	because then the index document being created will be deleted,
//    	instead put log warning and send empty index field text.
    	logger.warn("exception and empty index field from " + methodName +
                " pid="+pid+
                " dsId="+dsId+
                " mimetype="+mimetype+
                " exception="+e.toString());
        return "";
    }
    
    private String getDebugString(String debugString) {
    	String result = debugString;
    	if (debugString.length()>debuglength) {
    		result = result.substring(0,debuglength)+"...\n...";
    	}
    	return result;
    }

    
    private String getRealPID(String pid) {
    	int j = pid.indexOf("$");
    	if (j==-1) j = pid.length();
    	return pid.substring(0, j);
    }
    
}
