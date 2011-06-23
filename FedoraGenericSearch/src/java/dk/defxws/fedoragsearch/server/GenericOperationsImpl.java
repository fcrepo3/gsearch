//$Id$
/*
 * <p><b>License and Copyright: </b>The contents of this file is subject to the
 * same open source license as the Fedora Repository System at www.fedora-commons.org
 * Copyright &copy; 2006, 2007, 2008, 2009, 2010 by The Technical University of Denmark.
 * All rights reserved.</p>
 */
package dk.defxws.fedoragsearch.server;

import java.io.IOException;
import java.io.InputStream;

import java.rmi.RemoteException;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.transform.stream.StreamSource;

import dk.defxws.fedoragsearch.server.errors.ConfigException;
import dk.defxws.fedoragsearch.server.errors.FedoraObjectNotFoundException;
import dk.defxws.fedoragsearch.server.errors.GenericSearchException;

import org.apache.axis.AxisFault;

import org.apache.log4j.Logger;

import fedora.client.FedoraClient;

import fedora.common.Constants;

import fedora.server.access.FedoraAPIA;
import fedora.server.management.FedoraAPIM;
import fedora.server.types.gen.Datastream;
import fedora.server.types.gen.MIMETypedStream;

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

    private static final Map fedoraClients = new HashMap();

    protected String fgsUserName;
    protected String indexName;
    protected Config config;
    protected SearchResultFiltering srf;
    protected int insertTotal = 0;
    protected int updateTotal = 0;
    protected int deleteTotal = 0;
    protected int docCount = 0;
    protected int warnCount = 0;
    
    protected byte[] foxmlRecord;
    protected String dsID;
    protected byte[] ds;
    protected String dsText;
    protected String[] params = null;

    private static FedoraClient getFedoraClient(
    		String repositoryName,
    		String fedoraSoap,
    		String fedoraUser,
    		String fedoraPass)
            throws GenericSearchException {
        try {
            String baseURL = getBaseURL(fedoraSoap);
            String user = fedoraUser; 
            String clientId = user + "@" + baseURL;
            synchronized (fedoraClients) {
                if (fedoraClients.containsKey(clientId)) {
                    return (FedoraClient) fedoraClients.get(clientId);
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
    	if (trustStorePath!=null)
    		System.setProperty("javax.net.ssl.trustStore", trustStorePath);
    	if (trustStorePass!=null)
    		System.setProperty("javax.net.ssl.trustStorePassword", trustStorePass);
    	FedoraClient client = getFedoraClient(repositoryName, fedoraSoap, fedoraUser, fedoraPass);
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
    	if (trustStorePath!=null)
    		System.setProperty("javax.net.ssl.trustStore", trustStorePath);
    	if (trustStorePass!=null)
    		System.setProperty("javax.net.ssl.trustStorePassword", trustStorePass);
    	FedoraClient client = getFedoraClient(repositoryName, fedoraSoap, fedoraUser, fedoraPass);
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
    	this.fgsUserName = fgsUserName;
    	this.indexName = indexName;
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
        return "";
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
        try {
        	foxmlRecord = apim.export(pid, format, "public");
        } catch (RemoteException e) {
        	throw new FedoraObjectNotFoundException("Fedora Object "+pid+" not found at "+repositoryName, e);
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
                MIMETypedStream mts = apia.getDatastreamDissemination(pid, 
                        dsId, null);
                if (mts==null) return "";
                ds = mts.getStream();
                mimetype = mts.getMIMEType().split(";")[0]; // MIMETypedStream can include encoding, eg "text/xml;charset=utf-8" - split this off
                if (ds != null) {
                    dsBuffer = (new TransformerToText().getText(ds, mimetype));
                }
            } catch (Exception e) {
            	return emptyIndexField("getDatastreamText", pid, dsId, mimetype, e);
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
            dsds = apim.getDatastreams(pid, null, "A");
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
                MIMETypedStream mts = apia.getDatastreamDissemination(pid, 
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
        fedora.server.types.gen.Property[] params = new fedora.server.types.gen.Property[st.countTokens()];
        for (int i=0; i<st.countTokens(); i++) {
            String param = st.nextToken();
            String[] nameAndValue = param.split("=");
            params[i] = new fedora.server.types.gen.Property(nameAndValue[0], nameAndValue[1]);
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
                MIMETypedStream mts = apia.getDissemination(pid, bDefPid, 
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
    
}
