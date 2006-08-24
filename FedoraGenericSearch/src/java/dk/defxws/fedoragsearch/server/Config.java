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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.axis.AxisFault;
import org.apache.axis.client.AdminClient;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;

import dk.defxws.fedoragsearch.server.errors.ConfigException;
import dk.defxws.fedoragsearch.server.fedorasoap.FedoraAPIMBindingSOAPHTTPStub;

/**
 * Reads and checks the configuration files,
 * sets and gets the properties,
 * generates index-specific operationsImpl object.
 * 
 * @author  gsp@dtv.dk
 * @version $Id$
 */
public class Config {
    
    private static Config currentConfig = null;
    
    private Properties fgsProps = null;
    
    private Hashtable repositoryNameToProps = null;
    
    private String defaultRepositoryName = null;
    
    private Hashtable indexNameToProps = null;
    
    private String defaultIndexName = null;
    
    private int maxPageSize = 50;
    
    private int defaultGfindObjectsHitPageStart = 1;
    
    private int defaultGfindObjectsHitPageSize = 10;
    
    private int defaultGfindObjectsSnippetsMax = 3;
    
    private int defaultGfindObjectsFieldMaxLength = 100;
    
    private int defaultBrowseIndexTermPageSize = 20;
    
    private StringBuffer errors = null;
    
    private final Logger logger = Logger.getLogger(Config.class);
    
    public static void configure() throws ConfigException {
        currentConfig = (new Config());
    }
    
    public static Config getCurrentConfig() throws ConfigException {
        if (currentConfig == null)
            currentConfig = (new Config());
        return currentConfig;
    }
    
    public Config() throws ConfigException {
        errors = new StringBuffer();
        
//      Read and set fedoragsearch properties
        try {
            InputStream propStream = Config.class
            .getResourceAsStream("/config/fedoragsearch.properties");
            if (propStream == null) {
                throw new ConfigException(
                "*** config/fedoragsearch.properties not found in classpath");
            }
            fgsProps = new Properties();
            fgsProps.load(propStream);
            propStream.close();
        } catch (IOException e) {
            throw new ConfigException(
                    "*** Error loading config/fedoragsearch.properties:\n" + e.toString());
        }
        
        if (logger.isDebugEnabled())
            logger.debug("fedoragsearch.properties=" + fgsProps.toString());
        
//      Read soap deployment parameters and try to deploy the wsdd file
        String [] params = new String[4];
        params[0] = "-l"+fgsProps.getProperty("fedoragsearch.soapBase");
        params[1] =      fgsProps.getProperty("fedoragsearch.deployFile");
        params[2] = "-u"+fgsProps.getProperty("fedoragsearch.soapUser");
        params[3] = "-w"+fgsProps.getProperty("fedoragsearch.soapPass");
        if (logger.isDebugEnabled())
            logger.debug("AdminClient()).process(soapBase="+params[0]+" soapUser="+params[2]+" soapPass="+params[3]+" deployFile="+params[1]+")");
        try {
            (new AdminClient()).process(params);
        } catch (Exception e) {
            errors.append("\n*** Unable to deploy\n"+e.toString());
        }
        
//      Check rest stylesheets
        checkRestStylesheet("fedoragsearch.defaultNoXslt");
        checkRestStylesheet("fedoragsearch.defaultGfindObjectsRestXslt");
        checkRestStylesheet("fedoragsearch.defaultUpdateIndexRestXslt");
        checkRestStylesheet("fedoragsearch.defaultBrowseIndexRestXslt");
        checkRestStylesheet("fedoragsearch.defaultGetRepositoryInfoRestXslt");
        checkRestStylesheet("fedoragsearch.defaultGetIndexInfoRestXslt");
        
//      Check mimeTypes  
        checkMimeTypes("fedoragsearch", fgsProps, "fedoragsearch.mimeTypes");
        
//      Check resultPage properties
        try {
            maxPageSize = Integer.parseInt(fgsProps.getProperty("fedoragsearch.maxPageSize"));
        } catch (NumberFormatException e) {
            errors.append("\n*** maxPageSize is not valid:\n" + e.toString());
        }
        try {
            defaultBrowseIndexTermPageSize = Integer.parseInt(fgsProps.getProperty("fedoragsearch.defaultBrowseIndexTermPageSize"));
        } catch (NumberFormatException e) {
            errors.append("\n*** defaultBrowseIndexTermPageSize is not valid:\n" + e.toString());
        }
        try {
            defaultGfindObjectsHitPageSize = Integer.parseInt(fgsProps.getProperty("fedoragsearch.defaultGfindObjectsHitPageSize"));
        } catch (NumberFormatException e) {
            errors.append("\n*** defaultGfindObjectsHitPageSize is not valid:\n" + e.toString());
        }
        try {
            defaultGfindObjectsSnippetsMax = Integer.parseInt(fgsProps.getProperty("fedoragsearch.defaultGfindObjectsSnippetsMax"));
        } catch (NumberFormatException e) {
            errors.append("\n*** defaultGfindObjectsSnippetsMax is not valid:\n" + e.toString());
        }
        try {
            defaultGfindObjectsFieldMaxLength = Integer.parseInt(fgsProps.getProperty("fedoragsearch.defaultGfindObjectsFieldMaxLength"));
        } catch (NumberFormatException e) {
            errors.append("\n*** defaultGfindObjectsFieldMaxLength is not valid:\n" + e.toString());
        }
        
//      Check repository properties
        repositoryNameToProps = new Hashtable();
        defaultRepositoryName = null;
        StringTokenizer repositoryNames = new StringTokenizer(fgsProps.getProperty("fedoragsearch.repositoryNames"));
        while (repositoryNames.hasMoreTokens()) {
            String repositoryName = repositoryNames.nextToken();
            if (defaultRepositoryName == null)
                defaultRepositoryName = repositoryName;
            try {
                InputStream propStream = Config.class
                .getResourceAsStream("/config/repository/" + repositoryName + "/repository.properties");
                if (propStream != null) {
                    Properties props = new Properties();
                    props.load(propStream);
                    propStream.close();
                    
                    if (logger.isDebugEnabled())
                        logger.debug("/config/repository/" + repositoryName + "/repository.properties=" + props.toString());
                    
//                  Check repositoryName
                    String propsRepositoryName = props.getProperty("fgsrepository.repositoryName");
                    if (!repositoryName.equals(propsRepositoryName)) {
                        errors.append("\n*** config/repository/" + repositoryName +
                                ": fgsrepository.repositoryName must be=" + repositoryName);
                    }
                    
//                  Check repository access
//                  String fedoraSoap = props.getProperty("fgsrepository.fedoraSoap");
//                  String fedoraUser = props.getProperty("fgsrepository.fedoraUser");
//                  String fedoraPass = props.getProperty("fgsrepository.fedoraPass");
//                  
//                  FedoraAPIMBindingSOAPHTTPStub stub = null;
//                  try {
//                  stub = new FedoraAPIMBindingSOAPHTTPStub(
//                  new java.net.URL(fedoraSoap+"/management"), null);
//                  stub.describeUser(fedoraUser, fedoraPass);
//                  } catch (AxisFault e) {
//                  errors.append("\n*** config/repository/" + repositoryName +
//                  ": Access to " + fedoraSoap + " failed:\n" + e.toString());
//                  } catch (MalformedURLException e) {
//                  errors.append("\n*** config/repository/" + repositoryName +
//                  ": Access to " + fedoraSoap + "failed:\n" + e.toString());
//                  } catch (RemoteException e) {
//                  errors.append("\n*** config/repository/" + repositoryName
//                  + ": Access to " + fedoraSoap + " failed:\n" + e.toString());
//                  }
                    
//                  Check fedoraObjectDir
                    String fedoraObjectDirName = props.getProperty("fgsrepository.fedoraObjectDir");
                    File fedoraObjectDir = new File(fedoraObjectDirName);
                    if (fedoraObjectDir == null) {
                        errors.append("\n*** config/repository/" + repositoryName
                                + ": fgsrepository.fedoraObjectDir="
                                + fedoraObjectDirName + " not found");
                    }
                    
//                  Check result stylesheets
                    checkResultStylesheet("repository/"+repositoryName, props, 
                    "fgsrepository.defaultGetRepositoryInfoResultXslt");
                    
//                  Check mimeTypes
                    checkMimeTypes(repositoryName, props, "fgsrepository.mimeTypes");
                    
                    repositoryNameToProps.put(repositoryName, props);
                }
                else {
                    errors.append("\n*** config/repository/" + repositoryName
                            + "/repository.properties not found in classpath");
                }
            } catch (IOException e) {
                errors.append("\n*** Error loading config/repository/" + repositoryName
                        + ".properties:\n" + e.toString());
            }
        }
        
//      Check index properties
        indexNameToProps = new Hashtable();
//      indexNameToClassName = new7 Hashtable();
        defaultIndexName = null;
        StringTokenizer indexNames = new StringTokenizer(fgsProps.getProperty("fedoragsearch.indexNames"));
        while (indexNames.hasMoreTokens()) {
            String indexName = indexNames.nextToken();
            if (defaultIndexName == null)
                defaultIndexName = indexName;
            try {
                InputStream propStream = Config.class
                .getResourceAsStream("/config/index/" + indexName + "/index.properties");
                if (propStream != null) {
                    Properties props = new Properties();
                    props = new Properties();
                    props.load(propStream);
                    propStream.close();
                    
                    if (logger.isDebugEnabled())
                        logger.debug("/config/index/" + indexName + "/index.properties=" + props.toString());
                    
//                  Check indexName
                    String propsIndexName = props.getProperty("fgsindex.indexName");
                    if (!indexName.equals(propsIndexName)) {
                        errors.append("\n*** config/index/" + indexName
                                + ": fgsindex.indexName must be=" + indexName);
                    }
                    
//                  Check operationsImpl class
                    String operationsImpl = props.getProperty("fgsindex.operationsImpl");
                    if (operationsImpl == null || operationsImpl.equals("")) {
                        errors.append("\n*** config/index/" + indexName
                                + ": fgsindex.operationsImpl must be set in config/index/ "
                                + indexName + ".properties");
                    }
                    try {
                        Class operationsImplClass = Class.forName(operationsImpl);
                        try {
                            GenericOperationsImpl ops = (GenericOperationsImpl) operationsImplClass
                            .getConstructor(new Class[] {})
                            .newInstance(new Object[] {});
                        } catch (InstantiationException e) {
                            errors.append("\n*** config/index/"+indexName
                                    + ": fgsindex.operationsImpl="+operationsImpl
                                    + ": instantiation error.\n"+e.toString());
                        } catch (IllegalAccessException e) {
                            errors.append("\n*** config/index/"+indexName
                                    + ": fgsindex.operationsImpl="+operationsImpl
                                    + ": instantiation error.\n"+e.toString());
                        } catch (InvocationTargetException e) {
                            errors.append("\n*** config/index/"+indexName
                                    + ": fgsindex.operationsImpl="+operationsImpl
                                    + ": instantiation error.\n"+e.toString());
                        } catch (NoSuchMethodException e) {
                            errors.append("\n*** config/index/"+indexName
                                    + ": fgsindex.operationsImpl="+operationsImpl
                                    + ": instantiation error.\n"+e.toString());
                        }
                    } catch (ClassNotFoundException e) {
                        errors.append("\n*** config/index/" + indexName
                                + ": fgsindex.operationsImpl="+operationsImpl
                                + ": class not found.\n"+e);
                    }
                    
//                  Check result stylesheets
                    checkResultStylesheet("index/"+indexName, props, 
                    "fgsindex.defaultUpdateIndexDocXslt");
                    checkResultStylesheet("index/"+indexName, props, 
                    "fgsindex.defaultUpdateIndexResultXslt");
                    checkResultStylesheet("index/"+indexName, props, 
                    "fgsindex.defaultGfindObjectsResultXslt");
                    checkResultStylesheet("index/"+indexName, props, 
                    "fgsindex.defaultBrowseIndexResultXslt");
                    checkResultStylesheet("index/"+indexName, props, 
                    "fgsindex.defaultGetIndexInfoResultXslt");
                    
//                  Check indexDir for lucene
                    if (operationsImpl.indexOf("fgslucene")>-1) {
                        String indexDir = props.getProperty("fgsindex.indexDir"); 
                        File indexDirFile = new File(indexDir);
                        if (indexDirFile == null) {
                            errors.append("\n*** config/index/"+indexName+" fgsindex.indexDir="
                                    + indexDir + " must exist as a directory");
                        }
                    }
                    
//                  Check analyzer class
                    String analyzer = props.getProperty("fgsindex.analyzer"); 
                    if (analyzer == null || analyzer.equals("")) {
                        errors.append("\n*** config/index/" + indexName
                                +": fgsindex.analyzer must be set in config/index/ "
                                + indexName + ".properties");
                    }
                    try {
                        Class analyzerClass = Class.forName(analyzer);
                        try {
                            Analyzer a = (Analyzer) analyzerClass
                            .getConstructor(new Class[] {})
                            .newInstance(new Object[] {});
                        } catch (InstantiationException e) {
                            errors.append("\n*** config/index/"+indexName+" "+analyzer
                                    + ": fgsindex.analyzer="+analyzer
                                    + ": instantiation error.\n"+e.toString());
                        } catch (IllegalAccessException e) {
                            errors.append("\n*** config/index/"+indexName+" "+analyzer
                                    + ": fgsindex.analyzer="+analyzer
                                    + ": instantiation error.\n"+e.toString());
                        } catch (InvocationTargetException e) {
                            errors.append("\n*** config/index/"+indexName+" "+analyzer
                                    + ": fgsindex.analyzer="+analyzer
                                    + ": instantiation error.\n"+e.toString());
                        } catch (NoSuchMethodException e) {
                            errors.append("\n*** config/index/"+indexName+" "+analyzer
                                    + ": fgsindex.analyzer="+analyzer
                                    + ": instantiation error:\n"+e.toString());
                        }
                    } catch (ClassNotFoundException e) {
                        errors.append("\n*** config/index/" + indexName
                                + ": fgsindex.analyzer="+analyzer
                                + ": class not found:\n"+e.toString());
                    }
                    
//                  Check defaultQueryFields - how can we check this?
                    String defaultQueryFields = props.getProperty("fgsindex.defaultQueryFields");
                    
                    indexNameToProps.put(indexName, props);
                }
                else {
                    errors.append("\n*** config/index/" + indexName
                            + "/index.properties not found in classpath");
                }
            } catch (IOException e) {
                errors.append("\n*** Error loading config/index/" + indexName
                        + "/index.properties:\n"+e.toString());
            }
            if (errors.length()>0)
                throw new ConfigException(errors.toString());
        }
    }
    
    private void checkRestStylesheet(String propName) {
        String propValue = fgsProps.getProperty(propName);
        String xsltPathName = "/config/rest/"+propValue+".xslt";
        InputStream stylesheet = null;
        stylesheet = Config.class.getResourceAsStream(xsltPathName);
        if (stylesheet==null) {
            errors.append("\n*** Rest stylesheet "+propName+"="+propValue+" not found");
        } else
            checkStylesheet(xsltPathName, stylesheet);
    }
    
    private void checkResultStylesheet(String configPath, Properties props, String propName) {
        String propValue = props.getProperty(propName);
        String xsltPathName = "/config/"+configPath+"/"+propValue+".xslt";
        InputStream stylesheet = Config.class.getResourceAsStream(xsltPathName);
        if (stylesheet==null) {
            xsltPathName = "/config/index/common/"+propValue+".xslt";
            stylesheet = Config.class.getResourceAsStream(xsltPathName);
        }
        if (stylesheet==null) {
            errors.append("\n*** Result stylesheet "+configPath + ": " 
                    + propName + "=" + propValue + " not found");
        }
        else
            checkStylesheet(xsltPathName, stylesheet);
    }
    
    private void checkStylesheet(String xsltPathName, InputStream stylesheet) {
        Transformer transformer = null;
        try {
            TransformerFactory tfactory = TransformerFactory.newInstance();
            StreamSource xslt = new StreamSource(stylesheet);
            transformer = tfactory.newTransformer(xslt);
            String testSource = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
            "<emptyTestDocumentRoot/>";
            StringReader sr = new StringReader(testSource);
            StreamResult destStream = new StreamResult(new StringWriter());
            try {
                transformer.transform(new StreamSource(sr), destStream);
            } catch (TransformerException e) {
                errors.append("\n*** Stylesheet "+xsltPathName+" error:\n"+e.toString());
            }
        } catch (TransformerConfigurationException e) {
            errors.append("\n*** Stylesheet "+xsltPathName+" error:\n"+e.toString());
        } catch (TransformerFactoryConfigurationError e) {
            errors.append("\n*** Stylesheet "+xsltPathName+" error:\n"+e.toString());
        }
    }
    
    private void checkMimeTypes(String repositoryName, Properties props, String propName) {
        StringTokenizer mimeTypes = new StringTokenizer(props.getProperty(propName));
//      String handledMimeTypes = "text/plain text/html application/pdf application/ps application/msword";
        String[] handledMimeTypes = TransformerToText.handledMimeTypes;
        while (mimeTypes.hasMoreTokens()) {
            String mimeType = mimeTypes.nextToken();
            boolean handled = false;
            for (int i=0; i<handledMimeTypes.length; i++)
                if (handledMimeTypes[i].equals(mimeType)) {
                    handled = true;
                }
            if (!handled) {
                errors.append("\n*** "+repositoryName+":"+propName+": MimeType "+mimeType+" is not handled.");
            }
        }
    }
    
    public String getSoapBase() {
        return fgsProps.getProperty("fgsrepository.soapBase");
    }
    
    public String getSoapUser() {
        return fgsProps.getProperty("fgsrepository.soapUser");
    }
    
    public String getSoapPass() {
        return fgsProps.getProperty("fgsrepository.soapPass");
    }
    
    public String getDeployFile() {
        return fgsProps.getProperty("fedoragsearch.deployFile");
    }
    
    public String getDefaultNoXslt() {
        return fgsProps.getProperty("fedoragsearch.defaultNoXslt");
    }
    
    public String getDefaultGfindObjectsRestXslt() {
        return fgsProps.getProperty("fedoragsearch.defaultGfindObjectsRestXslt");
    }
    
    public int getMaxPageSize() {
        return maxPageSize;
    }
    
    public int getDefaultGfindObjectsHitPageStart() {
        return defaultGfindObjectsHitPageStart;
    }
    
    public int getDefaultGfindObjectsHitPageSize() {
        return defaultGfindObjectsHitPageSize;
    }
    
    public int getDefaultGfindObjectsSnippetsMax() {
        return defaultGfindObjectsSnippetsMax;
    }
    
    public int getDefaultGfindObjectsFieldMaxLength() {
        return defaultGfindObjectsFieldMaxLength;
    }
    
    public String getDefaultBrowseIndexRestXslt() {
        return fgsProps.getProperty("fedoragsearch.defaultBrowseIndexRestXslt");
    }
    
    public int getDefaultBrowseIndexTermPageSize() {
        return defaultBrowseIndexTermPageSize;
    }
    
    public String getDefaultGetRepositoryInfoRestXslt() {
        return fgsProps.getProperty("fedoragsearch.defaultGetRepositoryInfoRestXslt");
    }
    
    public String getDefaultGetIndexInfoRestXslt() {
        return fgsProps.getProperty("fedoragsearch.defaultGetIndexInfoRestXslt");
    }
    
    public String getDefaultUpdateIndexRestXslt() {
        return fgsProps.getProperty("fedoragsearch.defaultUpdateIndexRestXslt");
    }
    
    public String getMimeTypes() {
        return fgsProps.getProperty("fedoragsearch.mimeTypes");
    }
    
    public String getIndexNames(String indexNames) {
        if (indexNames==null || indexNames.equals("")) 
            return fgsProps.getProperty("fedoragsearch.indexNames");
        else 
            return indexNames;
    }
    
    public String getRepositoryName(String repositoryName) {
        if (repositoryName==null || repositoryName.equals("")) 
            return defaultRepositoryName;
        else 
            return repositoryName;
    }
    
    private Properties getRepositoryProps(String repositoryName) {
        return (Properties) (repositoryNameToProps.get(repositoryName));
    }
    
    public String getFedoraSoap(String repositoryName) {
        return (getRepositoryProps(repositoryName)).getProperty("fgsrepository.fedoraSoap");
    }
    
    public String getFedoraUser(String repositoryName) {
        return (getRepositoryProps(repositoryName)).getProperty("fgsrepository.fedoraUser");
    }
    
    public String getFedoraPass(String repositoryName) {
        return (getRepositoryProps(repositoryName)).getProperty("fgsrepository.fedoraPass");
    }
    
    public File getFedoraObjectDir(String repositoryName) 
    throws ConfigException {
        String fedoraObjectDirName = getRepositoryProps(repositoryName).getProperty("fgsrepository.fedoraObjectDir");
        File fedoraObjectDir = new File(fedoraObjectDirName);
        if (fedoraObjectDir == null) {
            throw new ConfigException(repositoryName+": fgsrepository.fedoraObjectDir="
                    + fedoraObjectDirName + " not found");
        }
        return fedoraObjectDir;
    }
    
    public String getRepositoryInfoResultXslt(String repositoryName, String resultPageXslt) {
        if (resultPageXslt==null || resultPageXslt.equals("")) 
            return (getRepositoryProps(getRepositoryName(repositoryName))).getProperty("fgsrepository.defaultGetRepositoryInfoResultXslt");
        else 
            return resultPageXslt;
    }
    
    public String getIndexName(String indexName) {
        if (indexName==null || indexName.equals("")) 
            return defaultIndexName;
        else 
            return indexName;
    }
    
    private Properties getIndexProps(String indexName) {
        return (Properties) (indexNameToProps.get(getIndexName(indexName)));
    }
    
    public String getUpdateIndexDocXslt(String indexName, String indexDocXslt) {
        if (indexDocXslt==null || indexDocXslt.equals("")) 
            return (getIndexProps(indexName)).getProperty("fgsindex.defaultUpdateIndexDocXslt");
        else 
            return indexDocXslt;
    }
    
    public String getUpdateIndexResultXslt(String indexName, String resultPageXslt) {
        if (resultPageXslt==null || resultPageXslt.equals("")) 
            return (getIndexProps(indexName)).getProperty("fgsindex.defaultUpdateIndexResultXslt");
        else 
            return resultPageXslt;
    }
    
    public String getGfindObjectsResultXslt(String indexName, String resultPageXslt) {
        if (resultPageXslt==null || resultPageXslt.equals("")) 
            return (getIndexProps(indexName)).getProperty("fgsindex.defaultGfindObjectsResultXslt");
        else 
            return resultPageXslt;
    }
    
    public String getBrowseIndexResultXslt(String indexName, String resultPageXslt) {
        if (resultPageXslt==null || resultPageXslt.equals("")) 
            return (getIndexProps(indexName)).getProperty("fgsindex.defaultBrowseIndexResultXslt");
        else 
            return resultPageXslt;
    }
    
    public String getIndexInfoResultXslt(String indexName, String resultPageXslt) {
        if (resultPageXslt==null || resultPageXslt.equals("")) 
            return (getIndexProps(indexName)).getProperty("fgsindex.defaultGetIndexInfoResultXslt");
        else 
            return resultPageXslt;
    }
    
    public String getMimeTypes(String indexName) {
        return getIndexProps(indexName).getProperty("fgsindex.mimeTypes");
    }
    
    public String getIndexBase(String indexName) {
        return getIndexProps(indexName).getProperty("fgsindex.indexBase");
    }
    
    public String getIndexDir(String indexName) {
        return getIndexProps(indexName).getProperty("fgsindex.indexDir");
    }
    
    public String getAnalyzer(String indexName) {
        return getIndexProps(indexName).getProperty("fgsindex.analyzer");
    }
    
    public String getDefaultQueryFields(String indexName) {
        return getIndexProps(indexName).getProperty("fgsindex.defaultQueryFields");
    }
    
    public GenericOperationsImpl getOperationsImpl(String indexNameParam)
    throws ConfigException {
        GenericOperationsImpl ops = null;
        String indexName = getIndexName(indexNameParam);
        String operationsImpl = (String)getIndexProps(indexName).getProperty("fgsindex.operationsImpl");
        if (operationsImpl == null)
            throw new ConfigException("The indexName " + indexName
                    + " is not configured.\n");
        if (logger.isDebugEnabled())
            logger.debug("indexName=" + indexName + " operationsImpl="
                    + operationsImpl);
        try {
            Class operationsImplClass = Class.forName(operationsImpl);
            if (logger.isDebugEnabled())
                logger.debug("operationsImplClass=" + operationsImplClass.toString());
            ops = (GenericOperationsImpl) operationsImplClass
            .getConstructor(new Class[] {})
            .newInstance(new Object[] {});
            if (logger.isDebugEnabled())
                logger.debug("ops=" + ops.toString());
        } catch (ClassNotFoundException e) {
            throw new ConfigException(operationsImpl
                    + ": class not found.\n", e);
        } catch (InstantiationException e) {
            throw new ConfigException(operationsImpl
                    + ": instantiation error.\n", e);
        } catch (IllegalAccessException e) {
            throw new ConfigException(operationsImpl
                    + ": instantiation error.\n", e);
        } catch (InvocationTargetException e) {
            throw new ConfigException(operationsImpl
                    + ": instantiation error.\n", e);
        } catch (NoSuchMethodException e) {
            throw new ConfigException(operationsImpl
                    + ": instantiation error.\n", e);
        }
        ops.init(this);
        return ops;
    }
    
    public static void main(String[] args) {
        try {
            Config.getCurrentConfig();
            System.out.println("Configuration OK!");
        } catch (ConfigException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
}
