/*
 * <p><b>License and Copyright: </b>The contents of this file is subject to the
 * same open source license as the Fedora Repository System at www.fedora-commons.org
 * Copyright &copy; 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013 by The Technical University of Denmark.
 * All rights reserved.</p>
 */
package dk.defxws.fedoragsearch.server;

/**
 * specifies the operations
 * 
 * @author  gsp@dtv.dk
 * @version 
 */
public interface Operations {
    
    public String updateIndex(
            String action,
            String value,
            String repositoryName,
            String indexName,
            String indexDocXslt,
            String resultPageXslt) throws java.rmi.RemoteException;
    
    public String gfindObjects(
            String query,
            int hitPageStart,
            int hitPageSize,
            int snippetsMax,
            int fieldMaxLength,
            String indexName,
            String sortFields,
            String resultPageXslt) throws java.rmi.RemoteException;
    
    public String browseIndex(
            String startTerm,
            int termPageSize,
            String fieldName,
            String indexName,
            String resultPageXslt) throws java.rmi.RemoteException;
    
    public String getRepositoryInfo(
            String repositoryName,
            String resultPageXslt) throws java.rmi.RemoteException;
    
    public String getIndexInfo(
            String indexName,
            String resultPageXslt) throws java.rmi.RemoteException;
}
