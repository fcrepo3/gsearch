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
                    " indexName="+indexName+
                    " resultPageXslt="+resultPageXslt);
        Operations ops = Config.getCurrentConfig().getOperationsImpl(indexName);
        return ops.gfindObjects(query, hitPageStart, hitPageSize, snippetsMax, fieldMaxLength, indexName, resultPageXslt);
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
        Operations ops = Config.getCurrentConfig().getOperationsImpl(indexName);
        return ops.browseIndex(startTerm, termPageSize, fieldName, indexName, resultPageXslt);
    }
    
    public String getRepositoryInfo(
            String repositoryName,
            String resultPageXslt)
    throws java.rmi.RemoteException {
        if (logger.isDebugEnabled())
            logger.debug("getRepositoryInfo" +
                    " repositoryName="+repositoryName+
                    " resultPageXslt="+resultPageXslt);
        GenericOperationsImpl ops = (new GenericOperationsImpl());
        ops.init(Config.getCurrentConfig());
        return ops.getRepositoryInfo(repositoryName, resultPageXslt);
    }
    
    public String getIndexInfo(
            String indexName,
            String resultPageXslt)
    throws java.rmi.RemoteException {
        if (logger.isDebugEnabled())
            logger.debug("getIndexInfo" +
                    " indexName="+indexName+
                    " resultPageXslt="+resultPageXslt);
        Operations ops = Config.getCurrentConfig().getOperationsImpl(indexName);
        return ops.getIndexInfo(indexName, resultPageXslt);
    }
    
    public String updateIndex(
            String action,
            String value,
            String repositoryName,
            String indexName,
            String indexXslt,
            String resultPageXslt)
    throws java.rmi.RemoteException {
        if (logger.isDebugEnabled())
            logger.debug("updateIndex" +
                    " action="+action+
                    " value="+value+
                    " repositoryName="+repositoryName+
                    " indexName="+indexName+
                    " indexXslt="+indexXslt+
                    " resultPageXslt="+resultPageXslt);
        GenericOperationsImpl ops = (new GenericOperationsImpl());
        ops.init(Config.getCurrentConfig());
        String result = ops.updateIndex(action, value, repositoryName, indexName, indexXslt, resultPageXslt);
        return result;
    }
    
}
