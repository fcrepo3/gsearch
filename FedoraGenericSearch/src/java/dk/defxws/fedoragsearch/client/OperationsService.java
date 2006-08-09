/**
 * OperationsService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.3 Oct 05, 2005 (05:23:37 EDT) WSDL2Java emitter.
 */

package dk.defxws.fedoragsearch.client;

public interface OperationsService extends javax.xml.rpc.Service {
    public java.lang.String getFgsOperationsAddress();

    public dk.defxws.fedoragsearch.client.Operations getFgsOperations() throws javax.xml.rpc.ServiceException;

    public dk.defxws.fedoragsearch.client.Operations getFgsOperations(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
