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
package dk.defxws.fedoragsearch.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.StringTokenizer;

import javax.xml.transform.stream.StreamSource;

import org.apache.axis.AxisFault;
import org.apache.log4j.Logger;

import dk.defxws.fedoragsearch.server.errors.GenericSearchException;
import dk.defxws.fedoragsearch.server.fedorasoap.FedoraAPIMBindingSOAPHTTPStub;
import dk.defxws.fedoragsearch.server.fedorasoap.FedoraAPIABindingSOAPHTTPStub;

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
    
    protected Config config;
    protected int insertTotal = 0;
    protected int updateTotal = 0;
    protected int deleteTotal = 0;
    protected int docCount = 0;
    
    protected byte[] foxmlRecord;
    protected String dsID;
    protected byte[] ds;
    protected String dsText;
    protected String[] params = null;
    
    public void init(Config currentConfig) {
        config = currentConfig;
    }
    
    public String gfindObjects(
            String query,
            long hitPageStart,
            int hitPageSize,
            int snippetsMax,
            int fieldMaxLength,
            String indexName,
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
                    " resultPageXslt="+resultPageXslt);
        params = new String[12];
        params[0] = "OPERATION";
        params[1] = "gfindObjects";
        params[2] = "QUERY";
        params[3] = query;
        params[4] = "HITPAGESTART";
        params[5] = Long.toString(hitPageStart);
        params[6] = "HITPAGESIZE";
        params[7] = Integer.toString(hitPageSize);
        params[8] = "INDEXNAME";
        params[9] = indexName;
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
        String repositoryInfoPath = "/config/repository/"+config.getRepositoryName(repositoryName)+"/repositoryInfo.xml";
        try {
            repositoryStream =  this.getClass().getResourceAsStream(repositoryInfoPath);
            if (repositoryStream == null) {
                throw new GenericSearchException("Error "+repositoryInfoPath+" not found in classpath");
            }
        } catch (IOException e) {
            throw new GenericSearchException("Error "+repositoryInfoPath+" not found in classpath", e);
        }
        StringBuffer sb = (new GTransformer()).transform(
                config.getRepositoryName(repositoryName)
                +"/"+config.getRepositoryInfoResultXslt(repositoryName, resultPageXslt), 
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
            String repositoryName,
            String indexNames,
            String indexDocXslt,
            String resultPageXslt)
    throws java.rmi.RemoteException {
        if (logger.isDebugEnabled())
            logger.debug("updateIndex" +
                    " action="+action+
                    " value="+value+
                    " repositoryName="+repositoryName+
                    " indexNames="+indexNames+
                    " indexDocXslt="+indexDocXslt+
                    " resultPageXslt="+resultPageXslt);
        StringBuffer resultXml = new StringBuffer(); 
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
            Operations ops = config.getOperationsImpl(indexName);
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
        
        if (logger.isDebugEnabled())
            logger.debug("getFoxmlFromPid" +
                    " pid="+pid);
        FedoraAPIMBindingSOAPHTTPStub stub = null;
        try {
            stub = new FedoraAPIMBindingSOAPHTTPStub(
                    new java.net.URL(config.getFedoraSoap(repositoryName)+"/management"), null);
        } catch (AxisFault e) {
            throw new RemoteException(e.getClass().getName()+": "+e.toString());
        } catch (MalformedURLException e) {
            throw new RemoteException(e.getClass().getName()+": "+e.toString());
        }
        foxmlRecord = stub.export(pid, "foxml1.0", "public", config.getFedoraUser(repositoryName), config.getFedoraPass(repositoryName));
    }
    
    public String getDatastreamText(
            String pid,
            String repositoryName,
            String dsId)
    throws GenericSearchException {
        if (logger.isDebugEnabled())
            logger.debug("getDatastreamText" +
            		" pid="+pid+" repositoryName="+repositoryName+" dsId="+dsId);
        StringBuffer dsBuffer = new StringBuffer();
        String mimetype = "";
        ds = null;
        if (dsId != null) {
            try {
            	java.net.URL url = new java.net.URL(config.getFedoraSoap(repositoryName)+"/access");
                if (url==null) return "";
            	FedoraAPIABindingSOAPHTTPStub stub = new FedoraAPIABindingSOAPHTTPStub(url, null);
                if (stub==null) return "";
                MIMETypedStream mts = stub.getDatastreamDissemination(pid, dsId, null, config.getFedoraUser(repositoryName), config.getFedoraPass(repositoryName));
                if (mts==null) return "";
                ds = mts.getStream();
                mimetype = mts.getMIMEType();
            } catch (AxisFault e) {
                if (e.getFaultString().indexOf("DatastreamNotFoundException")>-1 ||
                        e.getFaultString().indexOf("DefaulAccess")>-1)
                    return new String();
                else
                    throw new GenericSearchException(e.getFaultString()+": "+e.toString());
            } catch (MalformedURLException e) {
                throw new GenericSearchException(e.getClass().getName()+": "+e.toString());
            } catch (RemoteException e) {
                throw new GenericSearchException(e.getClass().getName()+": "+e.toString());
            }
        }
        if (ds != null) {
            dsBuffer = (new TransformerToText().getText(ds, mimetype));
        }
        if (logger.isDebugEnabled())
            logger.debug("getDatastreamText" +
                    " pid="+pid+
                    " dsId="+dsId+
                    " mimetype="+mimetype+
                    " dsBuffer="+dsBuffer.toString());
        return dsBuffer.toString();
    }
    
    public StringBuffer getFirstDatastreamText(
            String pid,
            String repositoryName,
            String dsMimetypes)
    throws GenericSearchException {
        if (logger.isDebugEnabled())
            logger.debug("getFirstDatastreamText" +
                    " pid="+pid+" dsMimetypes="+dsMimetypes);
        StringBuffer dsBuffer = new StringBuffer();
        Datastream[] dsds = null;
        try {
            dsds = (new FedoraAPIMBindingSOAPHTTPStub(
                    new java.net.URL(config.getFedoraSoap(repositoryName)+"/management"), null)).getDatastreams(pid, null, "A", config.getFedoraUser(repositoryName), config.getFedoraPass(repositoryName));
        } catch (AxisFault e) {
            throw new GenericSearchException(e.getClass().getName()+": "+e.toString());
        } catch (MalformedURLException e) {
            throw new GenericSearchException(e.getClass().getName()+": "+e.toString());
        } catch (RemoteException e) {
            throw new GenericSearchException(e.getClass().getName()+": "+e.toString());
        }
//      String mimetypes = "text/plain text/html application/pdf application/ps application/msword";
        String mimetypes = config.getMimeTypes();
        if (dsMimetypes!=null && dsMimetypes.length()>0)
            mimetypes = dsMimetypes;
        String mimetype = "";
        dsID = null;
        if (dsds != null) {
            int best = 99999;
            for (int i = 0; i < dsds.length; i++) {
                int j = mimetypes.indexOf(dsds[i].getMIMEType());
                if (j > -1 && best > j) {
                    dsID = dsds[i].getID();
                    best = j;
                    mimetype = dsds[i].getMIMEType();
                }
            }
        }
        ds = null;
        if (dsID != null) {
            try {
                ds = (new FedoraAPIABindingSOAPHTTPStub(
                        new java.net.URL(config.getFedoraSoap(repositoryName)+"/access"), null)).getDatastreamDissemination(pid, dsID, null, config.getFedoraUser(repositoryName), config.getFedoraPass(repositoryName)).getStream();
            } catch (AxisFault e) {
                throw new GenericSearchException(e.getClass().getName()+": "+e.toString());
            } catch (MalformedURLException e) {
                throw new GenericSearchException(e.getClass().getName()+": "+e.toString());
            } catch (RemoteException e) {
                throw new GenericSearchException(e.getClass().getName()+": "+e.toString());
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
        if (logger.isDebugEnabled())
            logger.debug("getDisseminationText" +
                    " pid="+pid+
                    " bDefPid="+bDefPid+
                    " methodName="+methodName+
                    " parameters="+parameters+
                    " asOfDateTime="+asOfDateTime);
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
                MIMETypedStream mts = (new FedoraAPIABindingSOAPHTTPStub(
                        new java.net.URL(config.getFedoraSoap(repositoryName)+"/access"), null)).getDissemination(pid, bDefPid, methodName, params, asOfDateTime, config.getFedoraUser(repositoryName), config.getFedoraPass(repositoryName));
                if (mts==null) {
                    throw new GenericSearchException("getDissemination returned null");
                }
                ds = mts.getStream();
                mimetype = mts.getMIMEType();
                if (logger.isDebugEnabled())
                    logger.debug("getDisseminationText" +
                            " mimetype="+mimetype);
            } catch (GenericSearchException e) {
                if (e.toString().indexOf("DisseminatorNotFoundException")>-1)
                    return new StringBuffer();
                else
                    throw new GenericSearchException(e.toString());
            } catch (AxisFault e) {
                if (e.getFaultString().indexOf("DisseminatorNotFoundException")>-1)
                    return new StringBuffer();
                else
                    throw new GenericSearchException(e.getFaultString()+": "+e.toString());
            } catch (MalformedURLException e) {
                throw new GenericSearchException(e.getClass().getName()+": "+e.toString());
            } catch (RemoteException e) {
                throw new GenericSearchException(e.getClass().getName()+": "+e.toString());
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
    
}
