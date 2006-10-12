/**
 * Operations.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.3 Oct 05, 2005 (05:23:37 EDT) WSDL2Java emitter.
 */

package dk.defxws.fedoragsearch.client;

public interface Operations extends java.rmi.Remote {
    public java.lang.String updateIndex(java.lang.String action, java.lang.String value, java.lang.String repositoryName, java.lang.String indexName, java.lang.String indexDocXslt, java.lang.String resultPageXslt) throws java.rmi.RemoteException;
    public java.lang.String browseIndex(java.lang.String startTerm, int termPageSize, java.lang.String fieldName, java.lang.String indexName, java.lang.String resultPageXslt) throws java.rmi.RemoteException;
    public java.lang.String gfindObjects(java.lang.String query, int hitPageStart, int hitPageSize, int snippetsMax, int fieldMaxLength, java.lang.String indexName, java.lang.String resultPageXslt) throws java.rmi.RemoteException;
    public java.lang.String getRepositoryInfo(java.lang.String repositoryName, java.lang.String resultPageXslt) throws java.rmi.RemoteException;
    public java.lang.String getIndexInfo(java.lang.String indexName, java.lang.String resultPageXslt) throws java.rmi.RemoteException;
}
