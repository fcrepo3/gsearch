//$Id$
/*
 * <p><b>License and Copyright: </b>The contents of this file is subject to the
 * same open source license as the Fedora Repository System at www.fedora-commons.org
 * Copyright &copy; 2006, 2007, 2008 by The Technical University of Denmark.
 * All rights reserved.</p>
 */
package dk.defxws.fedoragsearch.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.ListIterator;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.axis.client.AdminClient;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;

import dk.defxws.fedoragsearch.server.errors.ConfigException;

/**
 * Reads and checks the configuration files,
 * sets and gets the properties,
 * generates index-specific operationsImpl object.
 * 
 * @author  gsp@dtv.dk
 * @version
 */
public class Config {
    
    private static Config currentConfig = null;
    
    private String configName = null;
    
    private Properties fgsProps = null;
    
    private Hashtable repositoryNameToProps = null;
    
    private String defaultRepositoryName = null;
    
    private Hashtable indexNameToProps = null;
    
    private Hashtable indexNameToUriResolvers = null;
    
    private String defaultIndexName = null;
    
    private int maxPageSize = 50;
    
    private int defaultGfindObjectsHitPageStart = 1;
    
    private int defaultGfindObjectsHitPageSize = 10;
    
    private int defaultGfindObjectsSnippetsMax = 3;
    
    private int defaultGfindObjectsFieldMaxLength = 100;
    
    private int defaultBrowseIndexTermPageSize = 20;
    
    private String defaultSnippetBegin = "<span class=\"highlight\">";
    
    private String defaultSnippetEnd = "</span>";
    
    private String defaultAnalyzer = "org.apache.lucene.analysis.standard.StandardAnalyzer";
    
    private StringBuffer errors = null;
    
    private final Logger logger = Logger.getLogger(Config.class);
    
    public static void configure(String configName) throws ConfigException {
        currentConfig = (new Config(configName));
    }
    
    public static void configure(String configName, String propertyName, String propertyValue) throws ConfigException {
    	currentConfig = (new Config(configName)).setProperty(propertyName, propertyValue);
    }
    
    public static Config getCurrentConfig() throws ConfigException {
        if (currentConfig == null)
            currentConfig = (new Config("config"));
        return currentConfig;
    }
    
    public Config(String configNameIn) throws ConfigException {
    	configName = configNameIn;
        errors = new StringBuffer();
        
//      Read and set fedoragsearch properties
        try {
            InputStream propStream = Config.class
            .getResourceAsStream("/"+configName+"/fedoragsearch.properties");
            if (propStream == null) {
                throw new ConfigException(
                "*** "+configName+"/fedoragsearch.properties not found in classpath");
            }
            fgsProps = new Properties();
            fgsProps.load(propStream);
            propStream.close();
        } catch (IOException e) {
            throw new ConfigException(
                    "*** Error loading "+configName+"/fedoragsearch.properties:\n" + e.toString());
        }
        
        if (logger.isInfoEnabled())
            logger.info("fedoragsearch.properties=" + fgsProps.toString());
        
//      Read soap deployment parameters and try to deploy the wsdd file
        String [] params = new String[4];
        params[0] = "-l"+fgsProps.getProperty("fedoragsearch.soapBase");
        params[1] =      insertSystemProperties(fgsProps.getProperty("fedoragsearch.deployFile"));
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
//		Check for unknown properties, indicating typos or wrong property names
        String[] propNames = {
        		"fedoragsearch.deployFile",
        		"fedoragsearch.soapBase",
        		"fedoragsearch.soapUser",
        		"fedoragsearch.soapPass",
        		"fedoragsearch.defaultNoXslt",
        		"fedoragsearch.defaultGfindObjectsRestXslt",
        		"fedoragsearch.defaultUpdateIndexRestXslt",
        		"fedoragsearch.defaultBrowseIndexRestXslt",
        		"fedoragsearch.defaultGetRepositoryInfoRestXslt",
        		"fedoragsearch.defaultGetIndexInfoRestXslt",
        		"fedoragsearch.mimeTypes",
        		"fedoragsearch.maxPageSize",
        		"fedoragsearch.defaultBrowseIndexTermPageSize",
        		"fedoragsearch.defaultGfindObjectsHitPageSize",
        		"fedoragsearch.defaultGfindObjectsSnippetsMax",
        		"fedoragsearch.defaultGfindObjectsFieldMaxLength",
        		"fedoragsearch.repositoryNames",
        		"fedoragsearch.indexNames"
        };
        checkPropNames("fedoragsearch.properties", fgsProps, propNames);
        
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
                .getResourceAsStream("/"+configName+"/repository/" + repositoryName + "/repository.properties");
                if (propStream != null) {
                    Properties props = new Properties();
                    props.load(propStream);
                    propStream.close();
                    
                    if (logger.isInfoEnabled())
                        logger.info("/"+configName+"/repository/" + repositoryName + "/repository.properties=" + props.toString());
                    
//                  Check repositoryName
                    String propsRepositoryName = props.getProperty("fgsrepository.repositoryName");
                    if (!repositoryName.equals(propsRepositoryName)) {
                        errors.append("\n*** "+configName+"/repository/" + repositoryName +
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
//                  errors.append("\n*** "+configName+"/repository/" + repositoryName +
//                  ": Access to " + fedoraSoap + " failed:\n" + e.toString());
//                  } catch (MalformedURLException e) {
//                  errors.append("\n*** "+configName+"/repository/" + repositoryName +
//                  ": Access to " + fedoraSoap + "failed:\n" + e.toString());
//                  } catch (RemoteException e) {
//                  errors.append("\n*** "+configName+"/repository/" + repositoryName
//                  + ": Access to " + fedoraSoap + " failed:\n" + e.toString());
//                  }
                    
//                  Check fedoraObjectDir
                    String fedoraObjectDirName = insertSystemProperties(props.getProperty("fgsrepository.fedoraObjectDir"));
                    File fedoraObjectDir = new File(fedoraObjectDirName);
                    if (fedoraObjectDir == null) {
                        errors.append("\n*** "+configName+"/repository/" + repositoryName
                                + ": fgsrepository.fedoraObjectDir="
                                + fedoraObjectDirName + " not found");
                    }
                    
//                  Check result stylesheets
                    checkResultStylesheet("repository/"+repositoryName, props, 
                    "fgsrepository.defaultGetRepositoryInfoResultXslt");
                    
//            		Check for unknown properties, indicating typos or wrong property names
                    String[] reposPropNames = {
                    		"fgsrepository.repositoryName",
                    		"fgsrepository.fedoraSoap",
                    		"fgsrepository.fedoraUser",
                    		"fgsrepository.fedoraPass",
                    		"fgsrepository.fedoraObjectDir",
                    		"fgsrepository.defaultGetRepositoryInfoResultXslt",
                    		"fgsrepository.trustStorePath",
                    		"fgsrepository.trustStorePass"
                    };
                    checkPropNames(configName+"/repository/"+repositoryName+"/repository.properties", props, reposPropNames);
                    
                    repositoryNameToProps.put(repositoryName, props);
                }
                else {
                    errors.append("\n*** "+configName+"/repository/" + repositoryName
                            + "/repository.properties not found in classpath");
                }
            } catch (IOException e) {
                errors.append("\n*** Error loading "+configName+"/repository/" + repositoryName
                        + ".properties:\n" + e.toString());
            }
        }
        
//      Check index properties
        indexNameToProps = new Hashtable();
        indexNameToUriResolvers = new Hashtable();
        defaultIndexName = null;
        StringTokenizer indexNames = new StringTokenizer(fgsProps.getProperty("fedoragsearch.indexNames"));
        while (indexNames.hasMoreTokens()) {
            String indexName = indexNames.nextToken();
            if (defaultIndexName == null)
                defaultIndexName = indexName;
            try {
                InputStream propStream = Config.class
                .getResourceAsStream("/"+configName+"/index/" + indexName + "/index.properties");
                if (propStream != null) {
                    Properties props = new Properties();
                    props = new Properties();
                    props.load(propStream);
                    propStream.close();
                    
                    if (logger.isInfoEnabled())
                        logger.info("/"+configName+"/index/" + indexName + "/index.properties=" + props.toString());
                    
//                  Check indexName
                    String propsIndexName = props.getProperty("fgsindex.indexName");
                    if (!indexName.equals(propsIndexName)) {
                        errors.append("\n*** "+configName+"/index/" + indexName
                                + ": fgsindex.indexName must be=" + indexName);
                    }
                    
//                  Check operationsImpl class
                    String operationsImpl = props.getProperty("fgsindex.operationsImpl");
                    if (operationsImpl == null || operationsImpl.equals("")) {
                        errors.append("\n*** "+configName+"/index/" + indexName
                                + ": fgsindex.operationsImpl must be set in "+configName+"/index/ "
                                + indexName + ".properties");
                    }
                    try {
                        Class operationsImplClass = Class.forName(operationsImpl);
                        try {
                            GenericOperationsImpl ops = (GenericOperationsImpl) operationsImplClass
                            .getConstructor(new Class[] {})
                            .newInstance(new Object[] {});
                        } catch (InstantiationException e) {
                            errors.append("\n*** "+configName+"/index/"+indexName
                                    + ": fgsindex.operationsImpl="+operationsImpl
                                    + ": instantiation error.\n"+e.toString());
                        } catch (IllegalAccessException e) {
                            errors.append("\n*** "+configName+"/index/"+indexName
                                    + ": fgsindex.operationsImpl="+operationsImpl
                                    + ": instantiation error.\n"+e.toString());
                        } catch (InvocationTargetException e) {
                            errors.append("\n*** "+configName+"/index/"+indexName
                                    + ": fgsindex.operationsImpl="+operationsImpl
                                    + ": instantiation error.\n"+e.toString());
                        } catch (NoSuchMethodException e) {
                            errors.append("\n*** "+configName+"/index/"+indexName
                                    + ": fgsindex.operationsImpl="+operationsImpl
                                    + ": instantiation error.\n"+e.toString());
                        }
                    } catch (ClassNotFoundException e) {
                        errors.append("\n*** "+configName+"/index/" + indexName
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
                    
//                  Check indexDir
                    String indexDir = insertSystemProperties(props.getProperty("fgsindex.indexDir")); 
                    File indexDirFile = new File(indexDir);
                    if (indexDirFile == null) {
                    	errors.append("\n*** "+configName+"/index/"+indexName+" fgsindex.indexDir="
                    			+ indexDir + " must exist as a directory");
                    }

//                  	Check analyzer class for lucene
                    if (operationsImpl.indexOf("fgslucene")>-1) {
                    	String analyzer = props.getProperty("fgsindex.analyzer"); 
                    	if (analyzer == null || analyzer.equals("")) {
//                    		errors.append("\n*** "+configName+"/index/" + indexName
//                    				+": fgsindex.analyzer must be set in "+configName+"/index/ "
//                    				+ indexName + ".properties");
                    		analyzer = defaultAnalyzer;
                    	}
                    	try {
                    		Class analyzerClass = Class.forName(analyzer);
                    		try {
                    			Analyzer a = (Analyzer) analyzerClass
                    			.getConstructor(new Class[] {})
                    			.newInstance(new Object[] {});
                    		} catch (InstantiationException e) {
                    			errors.append("\n*** "+configName+"/index/"+indexName+" "+analyzer
                    					+ ": fgsindex.analyzer="+analyzer
                    					+ ": instantiation error.\n"+e.toString());
                    		} catch (IllegalAccessException e) {
                    			errors.append("\n*** "+configName+"/index/"+indexName+" "+analyzer
                    					+ ": fgsindex.analyzer="+analyzer
                    					+ ": instantiation error.\n"+e.toString());
                    		} catch (InvocationTargetException e) {
                    			errors.append("\n*** "+configName+"/index/"+indexName+" "+analyzer
                    					+ ": fgsindex.analyzer="+analyzer
                    					+ ": instantiation error.\n"+e.toString());
                    		} catch (NoSuchMethodException e) {
                    			errors.append("\n*** "+configName+"/index/"+indexName+" "+analyzer
                    					+ ": fgsindex.analyzer="+analyzer
                    					+ ": instantiation error:\n"+e.toString());
                    		}
                    	} catch (ClassNotFoundException e) {
                    		errors.append("\n*** "+configName+"/index/" + indexName
                    				+ ": fgsindex.analyzer="+analyzer
                    				+ ": class not found:\n"+e.toString());
                    	}
                    }
                    
//					Add untokenizedFields property for lucene

                    if (operationsImpl.indexOf("fgslucene")>-1) {
                    	String defaultUntokenizedFields = props.getProperty("fgsindex.untokenizedFields");
                    	if (defaultUntokenizedFields == null)
                    		props.setProperty("fgsindex.untokenizedFields", "");
                        if (indexDirFile != null) {
                        	StringBuffer untokenizedFields = new StringBuffer(props.getProperty("fgsindex.untokenizedFields"));
                        	IndexReader ir = null;
                        	try {
								ir = IndexReader.open(indexDir);
								int max = ir.numDocs();
								if (max > 10) max = 10;
								for (int i=0; i<max; i++) {
									Document doc = ir.document(i);
//									Enumeration fields = doc.fields();
//									while (fields.hasMoreElements()) {
									for (ListIterator li = doc.getFields().listIterator(); li.hasNext(); ) {
										Field f = (Field)li.next();
//										Field f = (Field)fields.nextElement();
										if (!f.isTokenized() && f.isIndexed() && untokenizedFields.indexOf(f.name())<0) {
											untokenizedFields.append(" "+f.name());
										}
									}
								}
							} catch (Exception e) {
							}
                        	props.setProperty("fgsindex.untokenizedFields", untokenizedFields.toString());
                            if (logger.isDebugEnabled())
                                logger.debug("indexName=" + indexName+ " fgsindex.untokenizedFields="+untokenizedFields);
                        }
                    }

//                  Check defaultQueryFields - how can we check this?
                    String defaultQueryFields = props.getProperty("fgsindex.defaultQueryFields");
                    
//                  Use custom URIResolver if given
                    if (operationsImpl.indexOf("fgslucene")>-1) {
                		Class uriResolverClass = null;
                        String uriResolver = props.getProperty("fgsindex.uriResolver");
                        if (!(uriResolver == null || uriResolver.equals(""))) {
                        	try {
                        		uriResolverClass = Class.forName(uriResolver);
                        		try {
                        			URIResolverImpl ur = (URIResolverImpl) uriResolverClass
                        			.getConstructor(new Class[] {})
                        			.newInstance(new Object[] {});
                                    if (ur != null) {
                                    	ur.setConfig(this);
                                    	indexNameToUriResolvers.put(indexName, ur);
                                    }
                        		} catch (InstantiationException e) {
                        			errors.append("\n*** "+configName+"/index/"+indexName+" "+uriResolver
                        					+ ": fgsindex.uriResolver="+uriResolver
                        					+ ": instantiation error.\n"+e.toString());
                        		} catch (IllegalAccessException e) {
                        			errors.append("\n*** "+configName+"/index/"+indexName+" "+uriResolver
                        					+ ": fgsindex.uriResolver="+uriResolver
                        					+ ": instantiation error.\n"+e.toString());
                        		} catch (InvocationTargetException e) {
                        			errors.append("\n*** "+configName+"/index/"+indexName+" "+uriResolver
                        					+ ": fgsindex.uriResolver="+uriResolver
                        					+ ": instantiation error.\n"+e.toString());
                        		} catch (NoSuchMethodException e) {
                        			errors.append("\n*** "+configName+"/index/"+indexName+" "+uriResolver
                        					+ ": fgsindex.uriResolver="+uriResolver
                        					+ ": instantiation error:\n"+e.toString());
                        		}
                        	} catch (ClassNotFoundException e) {
                        		errors.append("\n*** "+configName+"/index/" + indexName
                        				+ ": fgsindex.uriResolver="+uriResolver
                        				+ ": class not found:\n"+e.toString());
                        	}
                        }
                    }
//            		Check for unknown properties, indicating typos or wrong property names
                    String[] indexPropNames = {
                    		"fgsindex.indexName",
                    		"fgsindex.indexBase",
                    		"fgsindex.indexUser",
                    		"fgsindex.indexPass",
                    		"fgsindex.operationsImpl",
                    		"fgsindex.defaultUpdateIndexDocXslt",
                    		"fgsindex.defaultUpdateIndexResultXslt",
                    		"fgsindex.defaultGfindObjectsResultXslt",
                    		"fgsindex.defaultBrowseIndexResultXslt",
                    		"fgsindex.defaultGetIndexInfoResultXslt",
                    		"fgsindex.indexDir",
                    		"fgsindex.analyzer",
                    		"fgsindex.untokenizedFields",
                    		"fgsindex.defaultQueryFields",
                    		"fgsindex.snippetBegin",
                    		"fgsindex.snippetEnd",
                    		"fgsindex.maxBufferedDocs",
                    		"fgsindex.mergeFactor",
                    		"fgsindex.defaultSortFields",
                    		"fgsindex.uriResolver"
                    };
                    checkPropNames(configName+"/index/"+indexName+"/index.properties", props, indexPropNames);
                    
                    indexNameToProps.put(indexName, props);
                }
                else {
                    errors.append("\n*** "+configName+"/index/" + indexName
                            + "/index.properties not found in classpath");
                }
            } catch (IOException e) {
                errors.append("\n*** Error loading "+configName+"/index/" + indexName
                        + "/index.properties:\n"+e.toString());
            }
            if (errors.length()>0)
                throw new ConfigException(errors.toString());
        }
    }
    
    private void checkRestStylesheet(String propName) {
        String propValue = fgsProps.getProperty(propName);
        String configPath = "/"+configName+"/rest/"+propValue+".xslt";
        InputStream stylesheet = Config.class.getResourceAsStream(configPath);
        if (stylesheet==null) {
            errors.append("\n*** Rest stylesheet "+propName+"="+propValue+" not found");
        } else
            checkStylesheet(configPath, stylesheet);
    }
    
    private void checkResultStylesheet(String xsltPath, Properties props, String propName) {
        String propValue = props.getProperty(propName);
        String configPath = "/"+configName+"/"+xsltPath+"/"+propValue+".xslt";
        InputStream stylesheet = Config.class.getResourceAsStream(configPath);
        if (stylesheet==null) {
            errors.append("\n*** Result stylesheet "+configPath + ": " 
                    + propName + "=" + propValue + " not found");
        }
        else
            checkStylesheet(configPath, stylesheet);
    }
    
    private void checkStylesheet(String configPath, InputStream stylesheet) {
        if (logger.isDebugEnabled())
            logger.debug("checkStylesheet for " + configPath);
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
                errors.append("\n*** Stylesheet "+configPath+" error:\n"+e.toString());
            }
        } catch (TransformerConfigurationException e) {
            errors.append("\n*** Stylesheet "+configPath+" error:\n"+e.toString());
        } catch (TransformerFactoryConfigurationError e) {
            errors.append("\n*** Stylesheet "+configPath+" error:\n"+e.toString());
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
    
    private void checkPropNames(String propsFileName, Properties props, String[] propNames) {
//		Check for unknown properties, indicating typos or wrong property names
        Enumeration it = props.keys();
        while (it.hasMoreElements()) {
        	String propName = (String)it.nextElement();
        	for (int i=0; i<propNames.length; i++) {
        		if (propNames[i].equals(propName)) {
        			propName = null;
        		}
        	}
        	if (propName!=null) {
                errors.append("\n*** unknown config property in "+propsFileName+": " + propName);
        	}
        }
    }
    
    public String getConfigName() {
        return configName;
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
        return insertSystemProperties(fgsProps.getProperty("fedoragsearch.deployFile"));
    }
    
    public String getDefaultNoXslt() {
        return fgsProps.getProperty("fedoragsearch.defaultNoXslt");
    }
    
    public String getDefaultGfindObjectsRestXslt() {
        return fgsProps.getProperty("fedoragsearch.defaultGfindObjectsRestXslt");
    }
    
    public int getMaxPageSize() {
        try {
        	maxPageSize = Integer.parseInt(fgsProps.getProperty("fedoragsearch.maxPageSize"));
        } catch (NumberFormatException e) {
        }
        return maxPageSize;
    }
    
    public int getDefaultGfindObjectsHitPageStart() {
        return defaultGfindObjectsHitPageStart;
    }
    
    public int getDefaultGfindObjectsHitPageSize() {
        try {
            defaultGfindObjectsHitPageSize = Integer.parseInt(fgsProps.getProperty("fedoragsearch.defaultGfindObjectsHitPageSize"));
        } catch (NumberFormatException e) {
        }
        return defaultGfindObjectsHitPageSize;
    }
    
    public int getDefaultGfindObjectsSnippetsMax() {
        try {
        	defaultGfindObjectsSnippetsMax = Integer.parseInt(fgsProps.getProperty("fedoragsearch.defaultGfindObjectsSnippetsMax"));
        } catch (NumberFormatException e) {
        }
        return defaultGfindObjectsSnippetsMax;
    }
    
    public int getDefaultGfindObjectsFieldMaxLength() {
        try {
        	defaultGfindObjectsFieldMaxLength = Integer.parseInt(fgsProps.getProperty("fedoragsearch.defaultGfindObjectsFieldMaxLength"));
        } catch (NumberFormatException e) {
        }
        return defaultGfindObjectsFieldMaxLength;
    }
    
    public String getDefaultBrowseIndexRestXslt() {
        return fgsProps.getProperty("fedoragsearch.defaultBrowseIndexRestXslt");
    }
    
    public int getDefaultBrowseIndexTermPageSize() {
        try {
        	defaultBrowseIndexTermPageSize = Integer.parseInt(fgsProps.getProperty("fedoragsearch.defaultBrowseIndexTermPageSize"));
        } catch (NumberFormatException e) {
        }
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
    
    public String getRepositoryNameFromUrl(URL url) {
//    	String repositoryName = defaultRepositoryName;
    	String repositoryName = "";
    	String hostPort = url.getHost();
    	if (url.getPort()>-1)
    		hostPort += ":"+url.getPort();
        if (!(hostPort==null || hostPort.equals(""))) {
        	Enumeration propss = repositoryNameToProps.elements();
        	while (propss.hasMoreElements()) {
        		Properties props = (Properties)propss.nextElement();
        		String fedoraSoap = props.getProperty("fgsrepository.fedoraSoap");
        		if (fedoraSoap != null && fedoraSoap.indexOf(hostPort) > -1) {
        			return props.getProperty("fgsrepository.repositoryName", defaultRepositoryName);
        		}
        	}
        }
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
        String fedoraObjectDirName = insertSystemProperties(getRepositoryProps(repositoryName).getProperty("fgsrepository.fedoraObjectDir"));
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
    
    public String getTrustStorePath(String repositoryName) {
        return (getRepositoryProps(repositoryName)).getProperty("fgsrepository.trustStorePath");
    }
    
    public String getTrustStorePass(String repositoryName) {
        return (getRepositoryProps(repositoryName)).getProperty("fgsrepository.trustStorePass");
    }
    
    public String getIndexName(String indexName) {
        if (indexName==null || indexName.equals("")) 
            return defaultIndexName;
        else 
            return indexName;
    }
    
    public Properties getIndexProps(String indexName) {
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

	public String getSortFields(String indexName, String sortFields) {
        if (sortFields==null || sortFields.equals("")) 
            return (getIndexProps(indexName)).getProperty("fgsindex.defaultSortFields");
        else 
            return sortFields;
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
    
    public String getIndexBase(String indexName) {
        return insertSystemProperties(getIndexProps(indexName).getProperty("fgsindex.indexBase"));
    }
    
    public String getIndexDir(String indexName) {
        return insertSystemProperties(getIndexProps(indexName).getProperty("fgsindex.indexDir"));
    }
    
    public String getAnalyzer(String indexName) {
        return getIndexProps(indexName).getProperty("fgsindex.analyzer");
    }
    
    public URIResolver getURIResolver(String indexName) {
        return (URIResolver)indexNameToUriResolvers.get(indexName);
    }
    
    public String getUntokenizedFields(String indexName) {
        return getIndexProps(indexName).getProperty("fgsindex.untokenizedFields");
    }
    
    public void setUntokenizedFields(String indexName, String untokenizedFields) {
        getIndexProps(indexName).setProperty("fgsindex.untokenizedFields", untokenizedFields);
    }
    
    public String getDefaultQueryFields(String indexName) {
        return getIndexProps(indexName).getProperty("fgsindex.defaultQueryFields");
    }
    
    public String getSnippetBegin(String indexName) {
    	String snippetBegin = getIndexProps(indexName).getProperty("fgsindex.snippetBegin");
    	if (snippetBegin == null) return defaultSnippetBegin;
    	return snippetBegin;
    }
    
    public String getSnippetEnd(String indexName) {
    	String snippetEnd = getIndexProps(indexName).getProperty("fgsindex.snippetEnd");
    	if (snippetEnd == null) return defaultSnippetEnd;
    	return snippetEnd;
    }
    
    public int getMergeFactor(String indexName) {
    	int mergeFactor = 1;
		try {
			mergeFactor = Integer.parseInt(getIndexProps(indexName).getProperty("fgsindex.mergeFactor"));
		} catch (NumberFormatException e) {
		}
    	return mergeFactor;
    }
    
    public int getMaxBufferedDocs(String indexName) {
    	int maxBufferedDocs = 1;
		try {
			maxBufferedDocs = Integer.parseInt(getIndexProps(indexName).getProperty("fgsindex.maxBufferedDocs"));
		} catch (NumberFormatException e) {
		}
    	return maxBufferedDocs;
    }
    
    public GenericOperationsImpl getOperationsImpl(String indexNameParam)
    throws ConfigException {
        GenericOperationsImpl ops = null;
        String indexName = getIndexName(indexNameParam);
        Properties indexProps = getIndexProps(indexName);
        if (indexProps == null)
            throw new ConfigException("The indexName " + indexName
                    + " is not configured.\n");
        String operationsImpl = (String)indexProps.getProperty("fgsindex.operationsImpl");
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
        ops.init(indexName, this);
        return ops;
    }
    
    private String insertSystemProperties(String propertyValue) {
    	String result = propertyValue;
    	while (result.indexOf("${") > -1) {
            if (logger.isDebugEnabled())
                logger.debug("propertyValue="+result);
    		result = insertSystemProperty(result);
            if (logger.isDebugEnabled())
                logger.debug("propertyValue="+result);
    	}
    	return result;
    }
    
    private String insertSystemProperty(String propertyValue) {
    	String result = propertyValue;
    	int i = result.indexOf("${");
    	if (i > -1) {
    		int j = result.indexOf("}");
    		if (j > -1) {
        		String systemProperty = result.substring(i+2, j);
        		String systemPropertyValue = System.getProperty(systemProperty, "?NOTFOUND{"+systemProperty+"}");
        		result = result.substring(0, i) + systemPropertyValue + result.substring(j+1);
    		}
    	}
    	return result;
    }
    
    private Config setProperty(String propertyName, String propertyValue)
    	throws ConfigException {
        if (logger.isInfoEnabled())
            logger.info("property " + propertyName + "=" + propertyValue);
        if (!(propertyName==null || propertyName.equals(""))) {
            int i = propertyName.indexOf("/");
            String propName = propertyName;
        	Properties props = null;
            if (i>-1) {
                String propsName = propertyName.substring(0, i);
                propName = propertyName.substring(i+1);
                if (logger.isDebugEnabled())
                    logger.debug("propsName=" + propsName + " propName=" + propName);
            	if (indexNameToProps.containsKey(propsName)) {
            		props = (Properties)indexNameToProps.get(propsName);
            	}
            	else if (repositoryNameToProps.containsKey(propsName)) {
            		props = (Properties)repositoryNameToProps.get(propsName);
            	}
            } else {
            	props = fgsProps;
            }
        	if (props!=null && propName!=null && propName.length()>0) {
        		props.setProperty(propName, propertyValue);
        	} else {
                throw new ConfigException("property " + propertyName + " not found");
        	}
        }
        return this;
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
