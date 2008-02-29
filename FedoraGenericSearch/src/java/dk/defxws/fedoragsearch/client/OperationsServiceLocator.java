/**
 * OperationsServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.3 Oct 05, 2005 (05:23:37 EDT) WSDL2Java emitter.
 */

package dk.defxws.fedoragsearch.client;

public class OperationsServiceLocator extends org.apache.axis.client.Service implements dk.defxws.fedoragsearch.client.OperationsService {

	private static final long serialVersionUID = 1L;
	 
    public OperationsServiceLocator() {
    }


    public OperationsServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public OperationsServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for FgsOperations
    private java.lang.String FgsOperations_address = "http://localhost:8080/fedoragsearch/services/FgsOperations";

    public java.lang.String getFgsOperationsAddress() {
        return FgsOperations_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String FgsOperationsWSDDServiceName = "FgsOperations";

    public java.lang.String getFgsOperationsWSDDServiceName() {
        return FgsOperationsWSDDServiceName;
    }

    public void setFgsOperationsWSDDServiceName(java.lang.String name) {
        FgsOperationsWSDDServiceName = name;
    }

    public dk.defxws.fedoragsearch.client.Operations getFgsOperations() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(FgsOperations_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getFgsOperations(endpoint);
    }

    public dk.defxws.fedoragsearch.client.Operations getFgsOperations(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            dk.defxws.fedoragsearch.client.FgsOperationsSoapBindingStub _stub = new dk.defxws.fedoragsearch.client.FgsOperationsSoapBindingStub(portAddress, this);
            _stub.setPortName(getFgsOperationsWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setFgsOperationsEndpointAddress(java.lang.String address) {
        FgsOperations_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (dk.defxws.fedoragsearch.client.Operations.class.isAssignableFrom(serviceEndpointInterface)) {
                dk.defxws.fedoragsearch.client.FgsOperationsSoapBindingStub _stub = new dk.defxws.fedoragsearch.client.FgsOperationsSoapBindingStub(new java.net.URL(FgsOperations_address), this);
                _stub.setPortName(getFgsOperationsWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("FgsOperations".equals(inputPortName)) {
            return getFgsOperations();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://server.fedoragsearch.defxws.dk", "OperationsService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://server.fedoragsearch.defxws.dk", "FgsOperations"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("FgsOperations".equals(portName)) {
            setFgsOperationsEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
