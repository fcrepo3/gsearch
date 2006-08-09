//$Id$
/*
 * <p><b>License and Copyright: </b>The contents of this file will be subject to the
 * same open source license as the Fedora Repository System at www.fedora.info
 * It is expected to be released with Fedora version 2.2.
 *
 * This file was first auto-generated from WSDL
 * by the Apache Axis 1.3 WSDL2Java emitter.
 * 
 * <p>Copyright &copy; 2006 by The Technical University of Denmark.
 * All rights reserved.</p>
 */
package dk.defxws.fedoragsearch.server.fedorasoap;

/**
 * performs SOAP calls to the configured Fedora repositories
 * 
 * @author  gsp@dtv.dk
 * @version 
 */
public class FedoraAPIABindingSOAPHTTPStub extends org.apache.axis.client.Stub implements fedora.server.access.FedoraAPIA {
    private java.util.Vector cachedSerClasses = new java.util.Vector();
    private java.util.Vector cachedSerQNames = new java.util.Vector();
    private java.util.Vector cachedSerFactories = new java.util.Vector();
    private java.util.Vector cachedDeserFactories = new java.util.Vector();
    
    static org.apache.axis.description.OperationDesc [] _operations;
    
    static {
        _operations = new org.apache.axis.description.OperationDesc[9];
        _initOperationDesc1();
    }
    
    private static void _initOperationDesc1(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("describeRepository");
        oper.setReturnType(new javax.xml.namespace.QName("http://www.fedora.info/definitions/1/0/types/", "RepositoryInfo"));
        oper.setReturnClass(fedora.server.types.gen.RepositoryInfo.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "response"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[0] = oper;
        
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getObjectProfile");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "pid"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "asOfDateTime"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.fedora.info/definitions/1/0/types/", "ObjectProfile"));
        oper.setReturnClass(fedora.server.types.gen.ObjectProfile.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "response"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[1] = oper;
        
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("listMethods");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "pid"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "asOfDateTime"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.fedora.info/definitions/1/0/types/", "ArrayOfObjectMethodsDef"));
        oper.setReturnClass(fedora.server.types.gen.ObjectMethodsDef[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "response"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[2] = oper;
        
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("listDatastreams");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "pid"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "asOfDateTime"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.fedora.info/definitions/1/0/types/", "ArrayOfDatastreamDef"));
        oper.setReturnClass(fedora.server.types.gen.DatastreamDef[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "response"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[3] = oper;
        
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getDatastreamDissemination");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "pid"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "dsID"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "asOfDateTime"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.fedora.info/definitions/1/0/types/", "MIMETypedStream"));
        oper.setReturnClass(fedora.server.types.gen.MIMETypedStream.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "response"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[4] = oper;
        
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getDissemination");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "pid"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "bDefPid"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "methodName"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "parameters"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.fedora.info/definitions/1/0/types/", "ArrayOfProperty"), fedora.server.types.gen.Property[].class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "asOfDateTime"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.fedora.info/definitions/1/0/types/", "MIMETypedStream"));
        oper.setReturnClass(fedora.server.types.gen.MIMETypedStream.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "response"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[5] = oper;
        
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("findObjects");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "resultFields"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.fedora.info/definitions/1/0/types/", "ArrayOfString"), java.lang.String[].class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "maxResults"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "nonNegativeInteger"), org.apache.axis.types.NonNegativeInteger.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "query"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.fedora.info/definitions/1/0/types/", "FieldSearchQuery"), fedora.server.types.gen.FieldSearchQuery.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.fedora.info/definitions/1/0/types/", "FieldSearchResult"));
        oper.setReturnClass(fedora.server.types.gen.FieldSearchResult.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "response"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[6] = oper;
        
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("resumeFindObjects");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "sessionToken"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.fedora.info/definitions/1/0/types/", "FieldSearchResult"));
        oper.setReturnClass(fedora.server.types.gen.FieldSearchResult.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "response"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[7] = oper;
        
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getObjectHistory");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "pid"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.fedora.info/definitions/1/0/types/", "ArrayOfString"));
        oper.setReturnClass(java.lang.String[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "response"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[8] = oper;
        
    }
    
    public FedoraAPIABindingSOAPHTTPStub() throws org.apache.axis.AxisFault {
        this(null);
    }
    
    public FedoraAPIABindingSOAPHTTPStub(java.net.URL endpointURL, javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
        this(service);
        super.cachedEndpoint = endpointURL;
    }
    
    public FedoraAPIABindingSOAPHTTPStub(javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
        if (service == null) {
            super.service = new org.apache.axis.client.Service();
        } else {
            super.service = service;
        }
        ((org.apache.axis.client.Service)super.service).setTypeMappingVersion("1.2");
        java.lang.Class cls;
        javax.xml.namespace.QName qName;
        javax.xml.namespace.QName qName2;
        java.lang.Class beansf = org.apache.axis.encoding.ser.BeanSerializerFactory.class;
        java.lang.Class beandf = org.apache.axis.encoding.ser.BeanDeserializerFactory.class;
        java.lang.Class enumsf = org.apache.axis.encoding.ser.EnumSerializerFactory.class;
        java.lang.Class enumdf = org.apache.axis.encoding.ser.EnumDeserializerFactory.class;
        java.lang.Class arraysf = org.apache.axis.encoding.ser.ArraySerializerFactory.class;
        java.lang.Class arraydf = org.apache.axis.encoding.ser.ArrayDeserializerFactory.class;
        java.lang.Class simplesf = org.apache.axis.encoding.ser.SimpleSerializerFactory.class;
        java.lang.Class simpledf = org.apache.axis.encoding.ser.SimpleDeserializerFactory.class;
        java.lang.Class simplelistsf = org.apache.axis.encoding.ser.SimpleListSerializerFactory.class;
        java.lang.Class simplelistdf = org.apache.axis.encoding.ser.SimpleListDeserializerFactory.class;
        qName = new javax.xml.namespace.QName("http://www.fedora.info/definitions/1/0/types/", "ArrayOfCondition");
        cachedSerQNames.add(qName);
        cls = fedora.server.types.gen.Condition[].class;
        cachedSerClasses.add(cls);
        qName = new javax.xml.namespace.QName("http://www.fedora.info/definitions/1/0/types/", "Condition");
        qName2 = null;
        cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
        cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
        
        qName = new javax.xml.namespace.QName("http://www.fedora.info/definitions/1/0/types/", "ArrayOfDatastreamDef");
        cachedSerQNames.add(qName);
        cls = fedora.server.types.gen.DatastreamDef[].class;
        cachedSerClasses.add(cls);
        qName = new javax.xml.namespace.QName("http://www.fedora.info/definitions/1/0/types/", "DatastreamDef");
        qName2 = null;
        cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
        cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
        
        qName = new javax.xml.namespace.QName("http://www.fedora.info/definitions/1/0/types/", "ArrayOfMethodParmDef");
        cachedSerQNames.add(qName);
        cls = fedora.server.types.gen.MethodParmDef[].class;
        cachedSerClasses.add(cls);
        qName = new javax.xml.namespace.QName("http://www.fedora.info/definitions/1/0/types/", "MethodParmDef");
        qName2 = null;
        cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
        cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
        
        qName = new javax.xml.namespace.QName("http://www.fedora.info/definitions/1/0/types/", "ArrayOfObjectFields");
        cachedSerQNames.add(qName);
        cls = fedora.server.types.gen.ObjectFields[].class;
        cachedSerClasses.add(cls);
        qName = new javax.xml.namespace.QName("http://www.fedora.info/definitions/1/0/types/", "ObjectFields");
        qName2 = null;
        cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
        cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
        
        qName = new javax.xml.namespace.QName("http://www.fedora.info/definitions/1/0/types/", "ArrayOfObjectMethodsDef");
        cachedSerQNames.add(qName);
        cls = fedora.server.types.gen.ObjectMethodsDef[].class;
        cachedSerClasses.add(cls);
        qName = new javax.xml.namespace.QName("http://www.fedora.info/definitions/1/0/types/", "ObjectMethodsDef");
        qName2 = null;
        cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
        cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
        
        qName = new javax.xml.namespace.QName("http://www.fedora.info/definitions/1/0/types/", "ArrayOfProperty");
        cachedSerQNames.add(qName);
        cls = fedora.server.types.gen.Property[].class;
        cachedSerClasses.add(cls);
        qName = new javax.xml.namespace.QName("http://www.fedora.info/definitions/1/0/types/", "Property");
        qName2 = null;
        cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
        cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
        
        qName = new javax.xml.namespace.QName("http://www.fedora.info/definitions/1/0/types/", "ArrayOfString");
        cachedSerQNames.add(qName);
        cls = java.lang.String[].class;
        cachedSerClasses.add(cls);
        qName = new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string");
        qName2 = null;
        cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
        cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
        
        qName = new javax.xml.namespace.QName("http://www.fedora.info/definitions/1/0/types/", "ComparisonOperator");
        cachedSerQNames.add(qName);
        cls = fedora.server.types.gen.ComparisonOperator.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(enumsf);
        cachedDeserFactories.add(enumdf);
        
        qName = new javax.xml.namespace.QName("http://www.fedora.info/definitions/1/0/types/", "Condition");
        cachedSerQNames.add(qName);
        cls = fedora.server.types.gen.Condition.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);
        
        qName = new javax.xml.namespace.QName("http://www.fedora.info/definitions/1/0/types/", "DatastreamDef");
        cachedSerQNames.add(qName);
        cls = fedora.server.types.gen.DatastreamDef.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);
        
        qName = new javax.xml.namespace.QName("http://www.fedora.info/definitions/1/0/types/", "datastreamInputType");
        cachedSerQNames.add(qName);
        cls = fedora.server.types.gen.DatastreamInputType.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(enumsf);
        cachedDeserFactories.add(enumdf);
        
        qName = new javax.xml.namespace.QName("http://www.fedora.info/definitions/1/0/types/", "defaultInputType");
        cachedSerQNames.add(qName);
        cls = fedora.server.types.gen.DefaultInputType.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(enumsf);
        cachedDeserFactories.add(enumdf);
        
        qName = new javax.xml.namespace.QName("http://www.fedora.info/definitions/1/0/types/", "FieldSearchQuery");
        cachedSerQNames.add(qName);
        cls = fedora.server.types.gen.FieldSearchQuery.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);
        
        qName = new javax.xml.namespace.QName("http://www.fedora.info/definitions/1/0/types/", "FieldSearchResult");
        cachedSerQNames.add(qName);
        cls = fedora.server.types.gen.FieldSearchResult.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);
        
        qName = new javax.xml.namespace.QName("http://www.fedora.info/definitions/1/0/types/", "ListSession");
        cachedSerQNames.add(qName);
        cls = fedora.server.types.gen.ListSession.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);
        
        qName = new javax.xml.namespace.QName("http://www.fedora.info/definitions/1/0/types/", "MethodParmDef");
        cachedSerQNames.add(qName);
        cls = fedora.server.types.gen.MethodParmDef.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);
        
        qName = new javax.xml.namespace.QName("http://www.fedora.info/definitions/1/0/types/", "MIMETypedStream");
        cachedSerQNames.add(qName);
        cls = fedora.server.types.gen.MIMETypedStream.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);
        
        qName = new javax.xml.namespace.QName("http://www.fedora.info/definitions/1/0/types/", "ObjectFields");
        cachedSerQNames.add(qName);
        cls = fedora.server.types.gen.ObjectFields.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);
        
        qName = new javax.xml.namespace.QName("http://www.fedora.info/definitions/1/0/types/", "ObjectMethodsDef");
        cachedSerQNames.add(qName);
        cls = fedora.server.types.gen.ObjectMethodsDef.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);
        
        qName = new javax.xml.namespace.QName("http://www.fedora.info/definitions/1/0/types/", "ObjectProfile");
        cachedSerQNames.add(qName);
        cls = fedora.server.types.gen.ObjectProfile.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);
        
        qName = new javax.xml.namespace.QName("http://www.fedora.info/definitions/1/0/types/", "passByRef");
        cachedSerQNames.add(qName);
        cls = fedora.server.types.gen.PassByRef.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(enumsf);
        cachedDeserFactories.add(enumdf);
        
        qName = new javax.xml.namespace.QName("http://www.fedora.info/definitions/1/0/types/", "passByValue");
        cachedSerQNames.add(qName);
        cls = fedora.server.types.gen.PassByValue.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(enumsf);
        cachedDeserFactories.add(enumdf);
        
        qName = new javax.xml.namespace.QName("http://www.fedora.info/definitions/1/0/types/", "Property");
        cachedSerQNames.add(qName);
        cls = fedora.server.types.gen.Property.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);
        
        qName = new javax.xml.namespace.QName("http://www.fedora.info/definitions/1/0/types/", "RepositoryInfo");
        cachedSerQNames.add(qName);
        cls = fedora.server.types.gen.RepositoryInfo.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);
        
        qName = new javax.xml.namespace.QName("http://www.fedora.info/definitions/1/0/types/", "userInputType");
        cachedSerQNames.add(qName);
        cls = fedora.server.types.gen.UserInputType.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(enumsf);
        cachedDeserFactories.add(enumdf);
        
    }
    
    protected org.apache.axis.client.Call createCall() throws java.rmi.RemoteException {
        try {
            org.apache.axis.client.Call _call = super._createCall();
            if (super.maintainSessionSet) {
                _call.setMaintainSession(super.maintainSession);
            }
            if (super.cachedUsername != null) {
                _call.setUsername(super.cachedUsername);
            }
            if (super.cachedPassword != null) {
                _call.setPassword(super.cachedPassword);
            }
            if (super.cachedEndpoint != null) {
                _call.setTargetEndpointAddress(super.cachedEndpoint);
            }
            if (super.cachedTimeout != null) {
                _call.setTimeout(super.cachedTimeout);
            }
            if (super.cachedPortName != null) {
                _call.setPortName(super.cachedPortName);
            }
            java.util.Enumeration keys = super.cachedProperties.keys();
            while (keys.hasMoreElements()) {
                java.lang.String key = (java.lang.String) keys.nextElement();
                _call.setProperty(key, super.cachedProperties.get(key));
            }
            // All the type mapping information is registered
            // when the first call is made.
            // The type mapping information is actually registered in
            // the TypeMappingRegistry of the service, which
            // is the reason why registration is only needed for the first call.
            synchronized (this) {
                if (firstCall()) {
                    // must set encoding style before registering serializers
                    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
                    _call.setEncodingStyle(org.apache.axis.Constants.URI_SOAP11_ENC);
                    for (int i = 0; i < cachedSerFactories.size(); ++i) {
                        java.lang.Class cls = (java.lang.Class) cachedSerClasses.get(i);
                        javax.xml.namespace.QName qName =
                            (javax.xml.namespace.QName) cachedSerQNames.get(i);
                        java.lang.Object x = cachedSerFactories.get(i);
                        if (x instanceof Class) {
                            java.lang.Class sf = (java.lang.Class)
                            cachedSerFactories.get(i);
                            java.lang.Class df = (java.lang.Class)
                            cachedDeserFactories.get(i);
                            _call.registerTypeMapping(cls, qName, sf, df, false);
                        }
                        else if (x instanceof javax.xml.rpc.encoding.SerializerFactory) {
                            org.apache.axis.encoding.SerializerFactory sf = (org.apache.axis.encoding.SerializerFactory)
                            cachedSerFactories.get(i);
                            org.apache.axis.encoding.DeserializerFactory df = (org.apache.axis.encoding.DeserializerFactory)
                            cachedDeserFactories.get(i);
                            _call.registerTypeMapping(cls, qName, sf, df, false);
                        }
                    }
                }
            }
            return _call;
        }
        catch (java.lang.Throwable _t) {
            throw new org.apache.axis.AxisFault("Failure trying to get the Call object", _t);
        }
    }
    
    public fedora.server.types.gen.RepositoryInfo describeRepository() throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[0]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://www.fedora.info/definitions/1/0/api/#describeRepository");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://www.fedora.info/definitions/1/0/api/", "describeRepository"));
        
        setRequestHeaders(_call);
        setAttachments(_call);
        try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {});
        
        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (fedora.server.types.gen.RepositoryInfo) _resp;
            } catch (java.lang.Exception _exception) {
                return (fedora.server.types.gen.RepositoryInfo) org.apache.axis.utils.JavaUtils.convert(_resp, fedora.server.types.gen.RepositoryInfo.class);
            }
        }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            throw axisFaultException;
        }
    }
    
    public fedora.server.types.gen.ObjectProfile getObjectProfile(java.lang.String pid, java.lang.String asOfDateTime) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[1]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://www.fedora.info/definitions/1/0/api/#getObjectProfile");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://www.fedora.info/definitions/1/0/api/", "getObjectProfile"));
        
        setRequestHeaders(_call);
        setAttachments(_call);
        try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {pid, asOfDateTime});
        
        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (fedora.server.types.gen.ObjectProfile) _resp;
            } catch (java.lang.Exception _exception) {
                return (fedora.server.types.gen.ObjectProfile) org.apache.axis.utils.JavaUtils.convert(_resp, fedora.server.types.gen.ObjectProfile.class);
            }
        }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            throw axisFaultException;
        }
    }
    
    public fedora.server.types.gen.ObjectMethodsDef[] listMethods(java.lang.String pid, java.lang.String asOfDateTime) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[2]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://www.fedora.info/definitions/1/0/api/#listMethods");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://www.fedora.info/definitions/1/0/api/", "listMethods"));
        
        setRequestHeaders(_call);
        setAttachments(_call);
        try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {pid, asOfDateTime});
        
        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (fedora.server.types.gen.ObjectMethodsDef[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (fedora.server.types.gen.ObjectMethodsDef[]) org.apache.axis.utils.JavaUtils.convert(_resp, fedora.server.types.gen.ObjectMethodsDef[].class);
            }
        }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            throw axisFaultException;
        }
    }
    
    public fedora.server.types.gen.DatastreamDef[] listDatastreams(
            java.lang.String pid, 
            java.lang.String asOfDateTime) 
    throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[3]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://www.fedora.info/definitions/1/0/api/#listDatastreams");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://www.fedora.info/definitions/1/0/api/", "listDatastreams"));
        
        setRequestHeaders(_call);
        setAttachments(_call);
        try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {pid, asOfDateTime});
        
        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (fedora.server.types.gen.DatastreamDef[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (fedora.server.types.gen.DatastreamDef[]) org.apache.axis.utils.JavaUtils.convert(_resp, fedora.server.types.gen.DatastreamDef[].class);
            }
        }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            throw axisFaultException;
        }
    }
    
    public fedora.server.types.gen.DatastreamDef[] listDatastreams(
            java.lang.String pid, 
            java.lang.String asOfDateTime, 
            java.lang.String fedoraUser, 
            java.lang.String fedoraPass) 
    throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[3]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://www.fedora.info/definitions/1/0/api/#listDatastreams");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://www.fedora.info/definitions/1/0/api/", "listDatastreams"));
        
        _call.setUsername(fedoraUser);
        _call.setPassword(fedoraPass);
        setRequestHeaders(_call);
        setAttachments(_call);
        try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {pid, asOfDateTime});
        
        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (fedora.server.types.gen.DatastreamDef[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (fedora.server.types.gen.DatastreamDef[]) org.apache.axis.utils.JavaUtils.convert(_resp, fedora.server.types.gen.DatastreamDef[].class);
            }
        }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            throw axisFaultException;
        }
    }
    
    public fedora.server.types.gen.MIMETypedStream getDatastreamDissemination(
            java.lang.String pid, 
            java.lang.String dsID, 
            java.lang.String asOfDateTime) 
    throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[4]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://www.fedora.info/definitions/1/0/api/#getDatastreamDissemination");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://www.fedora.info/definitions/1/0/api/", "getDatastreamDissemination"));
        
        setRequestHeaders(_call);
        setAttachments(_call);
        try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {pid, dsID, asOfDateTime});
        
        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (fedora.server.types.gen.MIMETypedStream) _resp;
            } catch (java.lang.Exception _exception) {
                return (fedora.server.types.gen.MIMETypedStream) org.apache.axis.utils.JavaUtils.convert(_resp, fedora.server.types.gen.MIMETypedStream.class);
            }
        }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            throw axisFaultException;
        }
    }
    
    public fedora.server.types.gen.MIMETypedStream getDatastreamDissemination(
            java.lang.String pid, 
            java.lang.String dsID, 
            java.lang.String asOfDateTime, 
            java.lang.String fedoraUser, 
            java.lang.String fedoraPass) 
    throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[4]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://www.fedora.info/definitions/1/0/api/#getDatastreamDissemination");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://www.fedora.info/definitions/1/0/api/", "getDatastreamDissemination"));
        
        _call.setUsername(fedoraUser);
        _call.setPassword(fedoraPass);
        setRequestHeaders(_call);
        setAttachments(_call);
        try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {pid, dsID, asOfDateTime});
        
        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (fedora.server.types.gen.MIMETypedStream) _resp;
            } catch (java.lang.Exception _exception) {
                return (fedora.server.types.gen.MIMETypedStream) org.apache.axis.utils.JavaUtils.convert(_resp, fedora.server.types.gen.MIMETypedStream.class);
            }
        }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            throw axisFaultException;
        }
    }
    
    public fedora.server.types.gen.MIMETypedStream getDissemination(
            java.lang.String pid, 
            java.lang.String bDefPid, 
            java.lang.String methodName, 
            fedora.server.types.gen.Property[] parameters, 
            java.lang.String asOfDateTime) 
    throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[5]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://www.fedora.info/definitions/1/0/api/#getDissemination");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://www.fedora.info/definitions/1/0/api/", "getDissemination"));
        
        setRequestHeaders(_call);
        setAttachments(_call);
        try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {pid, bDefPid, methodName, parameters, asOfDateTime});
        
        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (fedora.server.types.gen.MIMETypedStream) _resp;
            } catch (java.lang.Exception _exception) {
                return (fedora.server.types.gen.MIMETypedStream) org.apache.axis.utils.JavaUtils.convert(_resp, fedora.server.types.gen.MIMETypedStream.class);
            }
        }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            throw axisFaultException;
        }
    }
    
    public fedora.server.types.gen.MIMETypedStream getDissemination(
            java.lang.String pid, 
            java.lang.String bDefPid, 
            java.lang.String methodName, 
            fedora.server.types.gen.Property[] parameters, 
            java.lang.String asOfDateTime, 
            java.lang.String fedoraUser, 
            java.lang.String fedoraPass) 
    throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[5]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://www.fedora.info/definitions/1/0/api/#getDissemination");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://www.fedora.info/definitions/1/0/api/", "getDissemination"));
        
        _call.setUsername(fedoraUser);
        _call.setPassword(fedoraPass);
        setRequestHeaders(_call);
        setAttachments(_call);
        try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {pid, bDefPid, methodName, parameters, asOfDateTime});
        
        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (fedora.server.types.gen.MIMETypedStream) _resp;
            } catch (java.lang.Exception _exception) {
                return (fedora.server.types.gen.MIMETypedStream) org.apache.axis.utils.JavaUtils.convert(_resp, fedora.server.types.gen.MIMETypedStream.class);
            }
        }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            throw axisFaultException;
        }
    }
    
    public fedora.server.types.gen.FieldSearchResult findObjects(java.lang.String[] resultFields, org.apache.axis.types.NonNegativeInteger maxResults, fedora.server.types.gen.FieldSearchQuery query) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[6]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://www.fedora.info/definitions/1/0/api/#findObjects");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://www.fedora.info/definitions/1/0/api/", "findObjects"));
        
        setRequestHeaders(_call);
        setAttachments(_call);
        try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {resultFields, maxResults, query});
        
        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (fedora.server.types.gen.FieldSearchResult) _resp;
            } catch (java.lang.Exception _exception) {
                return (fedora.server.types.gen.FieldSearchResult) org.apache.axis.utils.JavaUtils.convert(_resp, fedora.server.types.gen.FieldSearchResult.class);
            }
        }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            throw axisFaultException;
        }
    }
    
    public fedora.server.types.gen.FieldSearchResult resumeFindObjects(java.lang.String sessionToken) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[7]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://www.fedora.info/definitions/1/0/api/#resumeFindObjects");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://www.fedora.info/definitions/1/0/api/", "resumeFindObjects"));
        
        setRequestHeaders(_call);
        setAttachments(_call);
        try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {sessionToken});
        
        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (fedora.server.types.gen.FieldSearchResult) _resp;
            } catch (java.lang.Exception _exception) {
                return (fedora.server.types.gen.FieldSearchResult) org.apache.axis.utils.JavaUtils.convert(_resp, fedora.server.types.gen.FieldSearchResult.class);
            }
        }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            throw axisFaultException;
        }
    }
    
    public java.lang.String[] getObjectHistory(java.lang.String pid) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[8]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://www.fedora.info/definitions/1/0/api/#getObjectHistory");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://www.fedora.info/definitions/1/0/api/", "getObjectHistory"));
        
        setRequestHeaders(_call);
        setAttachments(_call);
        try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {pid});
        
        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (java.lang.String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String[]) org.apache.axis.utils.JavaUtils.convert(_resp, java.lang.String[].class);
            }
        }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            throw axisFaultException;
        }
    }
    
}
