//$Id$
/*
 * <p><b>License and Copyright: </b>The contents of this file is subject to the
 * same open source license as the Fedora Repository System at www.fedora-commons.org
 * Copyright &copy; 2006, 2007, 2008, 2009, 2010, 2011 by The Technical University of Denmark.
 * All rights reserved.</p>
 */
package dk.defxws.fedoragsearch.server;

import java.util.Date;

import org.apache.log4j.Logger;

/**
 * target for SOAP calls, calls the operationsImpl
 * 
 * @author  gsp@dtv.dk
 * @version 
 */
public class SOAPImpl implements Operations {
    
    private static final Logger logger =
        Logger.getLogger(SOAPImpl.class);
    
    public String gfindObjects(
            String query,
            int hitPageStart,
            int hitPageSize,
            int snippetsMax,
            int fieldMaxLength,
            String indexName,
            String resultPageXslt)
    throws java.rmi.RemoteException {
        String result = gfindObjects(query, hitPageStart, hitPageSize, snippetsMax, fieldMaxLength, indexName, "", resultPageXslt);
        return result;
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
    	Date startTime = new Date();
        if (logger.isInfoEnabled())
            logger.info("gfindObjects" +
                    " query="+query+
                    " hitPageStart="+hitPageStart+
                    " hitPageSize="+hitPageSize+
                    " indexName="+indexName+
                    " resultPageXslt="+resultPageXslt);
        Operations ops = getConfig().getOperationsImpl(indexName);
        String result = ops.gfindObjects(query, hitPageStart, hitPageSize, snippetsMax, fieldMaxLength, indexName, sortFields, resultPageXslt);
        String timeusedms = Long.toString((new Date()).getTime() - startTime.getTime());
        if (logger.isInfoEnabled())
            logger.info("gfindObjects" +
                    " query="+query+
                    " hitPageStart="+hitPageStart+
                    " hitPageSize="+hitPageSize+
                    " indexName="+indexName+
                    " resultPageXslt="+resultPageXslt+
                    " timeusedms="+timeusedms);
        return result;
    }
    
    public String browseIndex(
            String startTerm,
            int termPageSize,
            String fieldName,
            String indexName,
            String resultPageXslt)
    throws java.rmi.RemoteException {
    	Date startTime = new Date();
        if (logger.isInfoEnabled())
            logger.info("browseIndex" +
                    " startTerm="+startTerm+
                    " termPageSize="+termPageSize+
                    " fieldName="+fieldName+
                    " indexName="+indexName+
                    " resultPageXslt="+resultPageXslt);
        Operations ops = getConfig().getOperationsImpl(indexName);
        String result = ops.browseIndex(startTerm, termPageSize, fieldName, indexName, resultPageXslt);
        String timeusedms = Long.toString((new Date()).getTime() - startTime.getTime());
        if (logger.isInfoEnabled())
            logger.info("browseIndex" +
                    " startTerm="+startTerm+
                    " termPageSize="+termPageSize+
                    " fieldName="+fieldName+
                    " indexName="+indexName+
                    " resultPageXslt="+resultPageXslt+
                    " timeusedms="+timeusedms);
        return result;
    }
    
    public String getRepositoryInfo(
            String repositoryName,
            String resultPageXslt)
    throws java.rmi.RemoteException {
    	Date startTime = new Date();
        if (logger.isInfoEnabled())
            logger.info("getRepositoryInfo" +
                    " repositoryName="+repositoryName+
                    " resultPageXslt="+resultPageXslt);
        GenericOperationsImpl ops = (new GenericOperationsImpl());
        ops.init("", getConfig());
        String result = ops.getRepositoryInfo(repositoryName, resultPageXslt);
        String timeusedms = Long.toString((new Date()).getTime() - startTime.getTime());
        if (logger.isInfoEnabled())
            logger.info("getRepositoryInfo" +
                    " repositoryName="+repositoryName+
                    " resultPageXslt="+resultPageXslt+
                    " timeusedms="+timeusedms);
        return result;
    }
    
    public String getIndexInfo(
            String indexName,
            String resultPageXslt)
    throws java.rmi.RemoteException {
    	Date startTime = new Date();
        if (logger.isInfoEnabled())
            logger.info("getIndexInfo" +
                    " indexName="+indexName+
                    " resultPageXslt="+resultPageXslt);
        Operations ops = getConfig().getOperationsImpl(indexName);
        String result = ops.getIndexInfo(indexName, resultPageXslt);
        String timeusedms = Long.toString((new Date()).getTime() - startTime.getTime());
        if (logger.isInfoEnabled())
            logger.info("getIndexInfo" +
                    " indexName="+indexName+
                    " resultPageXslt="+resultPageXslt+
                    " timeusedms="+timeusedms);
        return result;
    }
    
    public String updateIndex(
            String action,
            String value,
            String repositoryName,
            String indexName,
            String indexDocXslt,
            String resultPageXslt)
    throws java.rmi.RemoteException {
    	Date startTime = new Date();
        if (logger.isInfoEnabled())
            logger.info("updateIndex" +
                    " action="+action+
                    " value="+value+
                    " repositoryName="+repositoryName+
                    " indexName="+indexName+
                    " indexDocXslt="+indexDocXslt+
                    " resultPageXslt="+resultPageXslt);
        GenericOperationsImpl ops = (new GenericOperationsImpl());
        ops.init(indexName, getConfig());
        String result = ops.updateIndex(action, value, repositoryName, indexName, indexDocXslt, resultPageXslt);
        String timeusedms = Long.toString((new Date()).getTime() - startTime.getTime());
        if (logger.isInfoEnabled())
            logger.info("updateIndex" +
                    " action="+action+
                    " value="+value+
                    " repositoryName="+repositoryName+
                    " indexName="+indexName+
                    " indexDocXslt="+indexDocXslt+
                    " resultPageXslt="+resultPageXslt+
                    " timeusedms="+timeusedms);
        return result;
    }
    
    public Config getConfig() throws java.rmi.RemoteException {
        Config config = Config.getCurrentConfig();
        if(!config.wsddDeployed()) {
            config.deployWSDD();
        }
        return config;
    }
    
}
