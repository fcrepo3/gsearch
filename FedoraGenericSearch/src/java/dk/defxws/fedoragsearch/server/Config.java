//$Id$
/*
 * <p><b>License and Copyright: </b>The contents of this file is subject to the
 * same open source license as the Fedora Repository System at www.fedora-commons.org
 * Copyright &copy; 2006, 2007, 2008, 2009, 2010, 2011 by The Technical University of Denmark.
 * All rights reserved.</p>
 */
package dk.defxws.fedoragsearch.server;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import javax.activation.MimetypesFileTypeMap;
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
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

import dk.defxws.fedoragsearch.server.errors.ConfigException;

import fedora.client.FedoraClient;
import fedora.client.Uploader;
import fedora.server.access.FedoraAPIA;
import fedora.server.management.FedoraAPIM;
import fedora.server.types.gen.DatastreamDef;
import fedora.server.types.gen.MIMETypedStream;

/**
 * Reads and checks the configuration files,
 * sets and gets the properties,
 * generates index-specific operationsImpl object.
 * 
 * A Config object may exist for each given configuration.
 * The normal situation is that the default currentConfig is named
 * by finalConfigName = "fgsconfigFinal" ( pre 2.3 = 'config').
 * 
 * For test purposes, the configure operation may be called with the configName
 * matching other given configurations, and then the configure operation
 * with a property may be used to change property values for test purposes.
 * 
 * From 2.4 the configuration may be managed in Fedora objects, see the configureObjects method.
 * In 2.4 it is decided to keep all configuration files in one Fedora object,
 * this may change later, if there are good reasons. Therefore, the names 'configObjects'
 * and 'configureObjects' refer now to just one object.
 * 
 * @author  gsp@dtv.dk
 * @version
 */
public class Config {

    FedoraClient fgsconfigObjectsClient = null;
    
    private static Config currentConfig = null;
    
    private static Hashtable configs = new Hashtable();
    
    private static boolean wsddDeployed = false;
    
    private static String configRootObjectPid = "FgsConfig:fgsconfigFinal";
    
    private static String finalConfigName = "fgsconfigFinal";
    
    private String configName = null;
    
    private Properties fcoProps = null;
    
    private Properties fgsProps = null;
    
    private Hashtable repositoryNameToProps = null;
    
    private String defaultRepositoryName = null;
    
    private Hashtable indexNameToProps = null;
    
    private Hashtable indexNameToUriResolvers = null;
    
    private String defaultIndexName = null;
    
    private Hashtable updaterNameToProps = null;
    
    private int maxPageSize = 50;
    
    private int defaultGfindObjectsHitPageStart = 1;
    
    private int defaultGfindObjectsHitPageSize = 10;
    
    private int defaultGfindObjectsSnippetsMax = 3;
    
    private int defaultGfindObjectsFieldMaxLength = 100;
    
    private int defaultBrowseIndexTermPageSize = 20;
    
    private String defaultSnippetBegin = "<span class=\"highlight\">";
    
    private String defaultSnippetEnd = "</span>";
    
    private String defaultAnalyzer = "org.apache.lucene.analysis.standard.StandardAnalyzer";
    
    private String searchResultFilteringModuleProperty = null;
    
    private StringBuffer errors = null;
    
    private final Logger logger = Logger.getLogger(Config.class);

    /**
     * The configure operation creates a new current Config object.
     */
    public static void configure(String configNameIn) throws ConfigException {
    	String configName = configNameIn;
    	if (configName==null || configName.equals(""))
    		configName = finalConfigName;
        currentConfig = new Config(configName);
        configs.put(configName, currentConfig);
    }

    /**
     * The configure operation with configureAction allows management of GSearch configuration in Fedora objects.
     * The operation either sets or gets the GSearch configuration objects in a Fedora repository.
     * configureAction must be either 'setFgsConfigObjects' or 'getFgsConfigObjects':
     *   setFgsConfigObjects will copy from fgsfinalConfig into the configure objects (creating if non-existing).
     *   getFgsConfigObjects will copy from the configure objects into fgsfinalConfig.
     * The resulting fgsfinalConfig will become currentConfig.
     * The Fedora repository used is configured in the fgsconfigObjects.properties file.
     * Initially, the fgsAdmin creates the set of configuration files with root named 'fgsfinalConfig'.
     * The first call of the 'setFgsConfigObjects' action will create the configure objects in the configured Fedora repository.
     * Subsequent editing of the configure objects will be made currentConfig by calls of 'getFgsConfigObjects'.
     */
    public static void configureObjects(String configureAction) throws ConfigException {
    	if (configureAction.equals("setFgsConfigObjects")) {
    		(new Config()).setFgsConfigObjects();
    	} else if (configureAction.equals("getFgsConfigObjects")) {
    		(new Config()).getFgsConfigObjects();
    	} else {
    		throw new ConfigException("*** configureAction='"+configureAction+"' is invalid.");
    	}
        currentConfig = new Config(finalConfigName);
        configs.put(finalConfigName, currentConfig);
    }

    /**
     * The configure operation with a property 
     * - creates a new Config object with the configName, if it does not exist,
     * - and sets that property, if it does not give error.
     */
    public static void configure(String configName, String propertyName, String propertyValue) throws ConfigException {
    	Config config = (Config)configs.get(configName);
    	if (config==null) {
    		config = new Config(configName);
            configs.put(configName, config);
    	}
    	String beforeValue = config.getProperty(propertyName);
    	config.setProperty(propertyName, propertyValue);
    	config.errors = new StringBuffer();
    	try {
			config.checkConfig();
		} catch (ConfigException e) {
	    	config.setProperty(propertyName, beforeValue);
    		throw new ConfigException(config.errors.toString());
		}
    }
    
    public static Config getCurrentConfig() throws ConfigException {
        if (currentConfig == null)
            currentConfig = new Config(finalConfigName);
        return currentConfig;
    }
    
    public static Config getConfig(String configName) throws ConfigException {
    	Config config = (Config)configs.get(configName);
        if (config == null) {
        	config = new Config(configName);
            configs.put(configName, config);
        }
        return config;
    }
    
    public Config() {
    }
    
    public Config(String configNameIn) throws ConfigException {
    	configName = configNameIn;
    	if (configName==null || configName.equals(""))
    		configName = finalConfigName;
        errors = new StringBuffer();
        
//      Get fedoragsearch properties
    	fgsProps = getFgsConfigProps("/"+configName+"/fedoragsearch.properties");

//      Get updater properties
    	String updaterProperty = fgsProps.getProperty("fedoragsearch.updaterNames");
    	if(updaterProperty == null) {
    		updaterNameToProps = null; // No updaters will be created
    	} else {           
    		updaterNameToProps = new Hashtable();
    		StringTokenizer updaterNames = new StringTokenizer(updaterProperty);
    		while (updaterNames.hasMoreTokens()) {
    			String updaterName = updaterNames.nextToken();
				try {
					updaterNameToProps.put(updaterName, getFgsConfigProps("/"+configName+"/updater/"+updaterName+"/updater.properties"));
				} catch (Exception e) {
	            	errors.append("\n*** " + e.toString());
				}
    		}
    	}
    	
//      Get repository properties
        repositoryNameToProps = new Hashtable();
        defaultRepositoryName = null;
        StringTokenizer repositoryNames = new StringTokenizer(fgsProps.getProperty("fedoragsearch.repositoryNames"));
        while (repositoryNames.hasMoreTokens()) {
            String repositoryName = repositoryNames.nextToken();
            if (defaultRepositoryName == null)
                defaultRepositoryName = repositoryName;
            try {
				repositoryNameToProps.put(repositoryName, getFgsConfigProps("/"+configName+"/repository/"+repositoryName+"/repository.properties"));
			} catch (Exception e) {
            	errors.append("\n*** " + e.toString());
			}
        }
        
//      Get index properties
        indexNameToProps = new Hashtable();
        indexNameToUriResolvers = new Hashtable();
        defaultIndexName = null;
        StringTokenizer indexNames = new StringTokenizer(fgsProps.getProperty("fedoragsearch.indexNames"));
        while (indexNames.hasMoreTokens()) {
            String indexName = indexNames.nextToken();
            if (defaultIndexName == null)
                defaultIndexName = indexName;
            try {
				indexNameToProps.put(indexName, getFgsConfigProps("/"+configName+"/index/" + indexName + "/index.properties"));
			} catch (Exception e) {
            	errors.append("\n*** " + e.toString());
			}
        }
        
        if (logger.isDebugEnabled())
            logger.debug("config created configName="+configName+" errors="+errors.toString());
    	checkConfig();
    }
  
    private void checkConfig() throws ConfigException {
        
//  	Check for unknown properties, indicating typos or wrong property names
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
    			"fedoragsearch.indexNames",
    			"fedoragsearch.updaterNames",
    			"fedoragsearch.searchResultFilteringModule",
    			"fedoragsearch.searchResultFilteringType",
    			"fedoragsearch.xsltProcessor"
    	};
    	checkPropNames("fedoragsearch.properties", fgsProps, propNames);

//  	Check fedoragsearch.xsltProcessor
    	String xsltProcessor = getXsltProcessor();
        if (!(xsltProcessor==null || xsltProcessor.equals("") || xsltProcessor.equals("xalan") || xsltProcessor.equals("saxon"))) {
            errors.append("\n*** fedoragsearch.xsltProcessor value="+xsltProcessor+", must be xalan or saxon");
          }

//  	Check rest stylesheets
    	checkRestStylesheet("fedoragsearch.defaultNoXslt");
    	checkRestStylesheet("fedoragsearch.defaultGfindObjectsRestXslt");
    	checkRestStylesheet("fedoragsearch.defaultUpdateIndexRestXslt");
    	checkRestStylesheet("fedoragsearch.defaultBrowseIndexRestXslt");
    	checkRestStylesheet("fedoragsearch.defaultGetRepositoryInfoRestXslt");
    	checkRestStylesheet("fedoragsearch.defaultGetIndexInfoRestXslt");

//  	Check mimeTypes  
    	checkMimeTypes("fedoragsearch", fgsProps, "fedoragsearch.mimeTypes");

//  	Check resultPage properties
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

//		Check updater properties
    	Enumeration updaterNames = updaterNameToProps.keys();
    	while (updaterNames.hasMoreElements()) {
    		String updaterName = (String)updaterNames.nextElement();
    		Properties props = (Properties)updaterNameToProps.get(updaterName);  
			String updaterFilePath = configName+"/updater/"+updaterName+"/updater.properties";
			if(props.getProperty("java.naming.factory.initial") == null) {
				errors.append("\n*** java.naming.factory.initial not provided in "+updaterFilePath);
			}
			if(props.getProperty("java.naming.provider.url") == null) {
				errors.append("\n*** java.naming.provider.url not provided in "+updaterFilePath);
			}
			if(props.getProperty("connection.factory.name") == null) {
				errors.append("\n*** connection.factory.name not provided in "+updaterFilePath);
			}
			if(props.getProperty("client.id") == null) {
				errors.append("\n*** client.id not provided in "+updaterFilePath);
			}  
    	}
    	
//		Check searchResultFilteringModule property
    	searchResultFilteringModuleProperty = fgsProps.getProperty("fedoragsearch.searchResultFilteringModule");
    	if(searchResultFilteringModuleProperty != null && searchResultFilteringModuleProperty.length()>0) {
    		try {
    			getSearchResultFiltering();
    		} catch (ConfigException e) {
    			errors.append(e.getMessage());
    		}
    		String searchResultFilteringTypeProperty = fgsProps.getProperty("fedoragsearch.searchResultFilteringType");
    		StringTokenizer srft = new StringTokenizer("");
    		if (searchResultFilteringTypeProperty != null) {
    			srft = new StringTokenizer(searchResultFilteringTypeProperty);
    		}
    		int countTokens = srft.countTokens();
    		if (searchResultFilteringTypeProperty==null || countTokens==0 || countTokens>1) {
    			errors.append("\n*** "+configName
    					+ ": fedoragsearch.searchResultFilteringType="+searchResultFilteringTypeProperty
    					+ ": one and only one of 'presearch', 'insearch', 'postsearch' must be stated.\n");
    		} 
    		else {
    			for (int i=0; i<countTokens; i++) {
    				String token = srft.nextToken();
    				if (!("presearch".equals(token) || "insearch".equals(token) || "postsearch".equals(token))) {
    					errors.append("\n*** "+configName
    							+ ": fedoragsearch.searchResultFilteringType="+searchResultFilteringTypeProperty
    							+ ": only 'presearch', 'insearch', 'postsearch' may be stated, not '"+token+"'.\n");
    				}
    			}
    		}
    	}

//  	Check repository properties
    	Enumeration repositoryNames = repositoryNameToProps.keys();
    	while (repositoryNames.hasMoreElements()) {
    		String repositoryName = (String)repositoryNames.nextElement();
    		Properties props = (Properties)repositoryNameToProps.get(repositoryName);    		
//  		Check for unknown properties, indicating typos or wrong property names
    		String[] reposPropNames = {
    				"fgsrepository.repositoryName",
    				"fgsrepository.fedoraSoap",
    				"fgsrepository.fedoraUser",
    				"fgsrepository.fedoraPass",
    				"fgsrepository.fedoraObjectDir",
    				"fgsrepository.fedoraVersion",
    				"fgsrepository.defaultGetRepositoryInfoResultXslt",
    				"fgsrepository.trustStorePath",
    				"fgsrepository.trustStorePass"
    		};
    		checkPropNames(configName+"/repository/"+repositoryName+"/repository.properties", props, reposPropNames);

//  		Check repositoryName
    		String propsRepositoryName = props.getProperty("fgsrepository.repositoryName");
    		if (!repositoryName.equals(propsRepositoryName)) {
    			errors.append("\n*** "+configName+"/repository/" + repositoryName +
    					": fgsrepository.repositoryName must be=" + repositoryName);
    		}

//  		Check fedoraObjectDir
    		String fedoraObjectDirName = insertSystemProperties(props.getProperty("fgsrepository.fedoraObjectDir"));
    		File fedoraObjectDir = new File(fedoraObjectDirName);
    		if (fedoraObjectDir == null) {
    			errors.append("\n*** "+configName+"/repository/" + repositoryName
    					+ ": fgsrepository.fedoraObjectDir="
    					+ fedoraObjectDirName + " not found");
    		}

//  		Check result stylesheets
    		checkResultStylesheet("repository/"+repositoryName, props, "fgsrepository.defaultGetRepositoryInfoResultXslt");
    	}

//  	Check index properties
    	Enumeration indexNames = indexNameToProps.keys();
    	while (indexNames.hasMoreElements()) {
    		String indexName = (String)indexNames.nextElement();
    		Properties props = (Properties)indexNameToProps.get(indexName);
//  		Check for unknown properties, indicating typos or wrong property names
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
    				"fgsindex.stopwordsLocation",
    				"fgsindex.untokenizedFields",
    				"fgsindex.defaultQueryFields",
    				"fgsindex.allowLeadingWildcard",
    				"fgsindex.snippetBegin",
    				"fgsindex.snippetEnd",
    				"fgsindex.maxBufferedDocs",
    				"fgsindex.mergeFactor",
    				"fgsindex.defaultWriteLockTimeout",
    				"fgsindex.defaultSortFields",
    				"fgsindex.uriResolver"
    		};
    		checkPropNames(configName+"/index/"+indexName+"/index.properties", props, indexPropNames);
    		
//  		Check indexName
    		String propsIndexName = props.getProperty("fgsindex.indexName");
    		if (!indexName.equals(propsIndexName)) {
    			errors.append("\n*** "+configName+"/index/" + indexName
    					+ ": fgsindex.indexName must be=" + indexName);
    		}

//  		Check operationsImpl class
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

//  		Check result stylesheets
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

//  		Check indexDir
    		String indexDir = insertSystemProperties(props.getProperty("fgsindex.indexDir")); 
    		File indexDirFile = new File(indexDir);
    		if (indexDirFile == null) {
    			errors.append("\n*** "+configName+"/index/"+indexName+" fgsindex.indexDir="
    					+ indexDir + " must exist as a directory");
    		}

//  		Check analyzer class for lucene and solr
    		if (operationsImpl.indexOf("fgslucene")>-1 || operationsImpl.indexOf("fgssolr")>-1) {
    			String analyzerClassName = props.getProperty("fgsindex.analyzer"); 
    			if (analyzerClassName == null || analyzerClassName.equals("")) {
    				analyzerClassName = defaultAnalyzer;
    			}
    			String stopwordsLocation = props.getProperty("fgsindex.stopwordsLocation"); 
    			try {
    				Version version = Version.LUCENE_33;
    				Class analyzerClass = Class.forName(analyzerClassName);
        			if (stopwordsLocation == null || stopwordsLocation.equals("")) {
    					analyzerClass.getConstructor(new Class[] { Version.class})
    					.newInstance(new Object[] { version });
        			} else {
    					analyzerClass.getConstructor(new Class[] { Version.class, File.class})
    					.newInstance(new Object[] { version, new File(stopwordsLocation) });
        			}
    			} catch (Exception e) {
    				errors.append("\n*** "+configName+"/index/" + indexName
    						+ ": fgsindex.analyzer="+analyzerClassName
    						+ ": class not found:\n"+e.toString());
				}
    		}

//  		Add untokenizedFields property for lucene
    		if (operationsImpl.indexOf("fgslucene")>-1) {
    			String defaultUntokenizedFields = props.getProperty("fgsindex.untokenizedFields");
    			if (defaultUntokenizedFields == null)
    				props.setProperty("fgsindex.untokenizedFields", "");
    			if (indexDirFile != null) {
    				StringBuffer untokenizedFields = new StringBuffer(props.getProperty("fgsindex.untokenizedFields"));
    				IndexReader ir = null;
    				try {
    					Directory dir = new SimpleFSDirectory(indexDirFile);
    					ir = IndexReader.open(dir, true);
    					int max = ir.numDocs();
    					if (max > 10) max = 10;
    					for (int i=0; i<max; i++) {
    						Document doc = ir.document(i);
    						for (ListIterator li = doc.getFields().listIterator(); li.hasNext(); ) {
    							Field f = (Field)li.next();
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

//  		Check defaultQueryFields - how can we check this?
    		String defaultQueryFields = props.getProperty("fgsindex.defaultQueryFields");

//      	Check allowLeadingWildcard
    		if (operationsImpl.indexOf("fgslucene")>-1) {
            	String allowLeadingWildcard = getIndexProps(indexName).getProperty("fgsindex.allowLeadingWildcard");
                if (!(allowLeadingWildcard==null || allowLeadingWildcard.equals("") || allowLeadingWildcard.equals("false") || allowLeadingWildcard.equals("true"))) {
                    errors.append("\n*** fedoragsearch.allowLeadingWildcard value="+allowLeadingWildcard+", must be false or true");
                  }
    		}
    		
//  		Use custom URIResolver if given
    		if (operationsImpl.indexOf("fgslucene")>-1 ||
    		    operationsImpl.indexOf("fgssolr")>-1) {
    			if (indexNameToUriResolvers != null)
    				indexNameToUriResolvers.remove(indexName);
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
    	}
        if (logger.isDebugEnabled())
            logger.debug("configCheck configName="+configName+" errors="+errors.toString());
    	if (errors.length()>0)
    		throw new ConfigException(errors.toString());
    }
    
    //  Read soap deployment parameters and try to deploy the wsdd file    
    public void deployWSDD() {
      String [] params = new String[4];
      params[0] = "-l"+getSoapBase();
      params[1] =      insertSystemProperties(getDeployFile());
      params[2] = "-u"+getSoapUser();
      params[3] = "-w"+getSoapPass();
      if (logger.isDebugEnabled())
          logger.debug("AdminClient()).process(soapBase="+params[0]+" soapUser="+params[2]+" soapPass="+params[3]+" deployFile="+params[1]+")");
      try {
          (new AdminClient()).process(params);
      } catch (Exception e) {
          errors.append("\n*** Unable to deploy\n"+e.toString());
          logger.warn("Unable to deploy: " + e.getMessage());
      }
      wsddDeployed = true;
    }
    
    public boolean wsddDeployed() {
        return wsddDeployed;
    }
    
    private void checkRestStylesheet(String propName) {
        String propValue = fgsProps.getProperty(propName);
        String configPath = "/"+configName+"/rest/"+propValue+".xslt";
        checkStylesheet(configPath);
    }
    
    private void checkResultStylesheet(String xsltPath, Properties props, String propName) {
        String propValue = props.getProperty(propName);
        String configPath = "/"+configName+"/"+xsltPath+"/"+propValue+".xslt";
        checkStylesheet(configPath);
    }
    
    private void checkStylesheet(String configPath) {
        if (logger.isDebugEnabled())
            logger.debug("checkStylesheet for " + configPath);
        URL stylesheet = Config.class.getResource(configPath);
        if (stylesheet==null) {
          errors.append("\n*** Stylesheet "+configPath+" not found");
          return;
        }
        TransformerFactory tfactory = null;
		try {
			tfactory = TransformerFactory.newInstance();
		} catch (TransformerFactoryConfigurationError e) {
            errors.append("\n*** Stylesheet "+configPath+" error:\n"+e.toString());
		}
        StreamSource xslt = null;
		try {
			xslt = new StreamSource(stylesheet.openStream(), stylesheet.toString());
		} catch (IOException e) {
            errors.append("\n*** Stylesheet "+configPath+" error:\n"+e.toString());
		}
        Transformer transformer = null;
        try {
			transformer = tfactory.newTransformer(xslt);
		} catch (TransformerConfigurationException e) {
            errors.append("\n*** Stylesheet "+configPath+" error:\n"+e.toString());
		}
        String testSource = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
        "<emptyTestDocumentRoot/>";
        StringReader sr = new StringReader(testSource);
        StreamResult destStream = new StreamResult(new StringWriter());
        try {
            transformer.transform(new StreamSource(sr), destStream);
        } catch (TransformerException e) {
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
        return fgsProps.getProperty("fedoragsearch.soapBase");
    }
    
    public String getSoapUser() {
        return fgsProps.getProperty("fedoragsearch.soapUser");
    }
    
    public String getSoapPass() {
        return fgsProps.getProperty("fedoragsearch.soapPass");
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
    
    public File getFedoraObjectDir(String repositoryName) throws ConfigException {
        String fedoraObjectDirName = insertSystemProperties(getRepositoryProps(repositoryName).getProperty("fgsrepository.fedoraObjectDir"));
        File fedoraObjectDir = new File(fedoraObjectDirName);
        if (fedoraObjectDir == null) {
            throw new ConfigException(repositoryName+": fgsrepository.fedoraObjectDir="
                    + fedoraObjectDirName + " not found");
        }
        return fedoraObjectDir;
    }
    
    public String getFedoraVersion(String repositoryName) {
        return (getRepositoryProps(repositoryName)).getProperty("fgsrepository.fedoraVersion");
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

    public Hashtable getUpdaterProps() {
        return updaterNameToProps;
    }    
    
    public String getIndexName(String indexName) {
        if (indexName==null || indexName.equals("")) 
            return defaultIndexName;
        else {
        	int i = indexName.indexOf("/");
        	if (i<0)
                return indexName;
        	else
        		return indexName.substring(0, i);
        }
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
        String indexDir = getIndexProps(indexName).getProperty("fgsindex.indexDir");
        if (indexName.indexOf("/")>-1)
        	indexDir += indexName.substring(indexName.indexOf("/"));
        return insertSystemProperties(getIndexProps(indexName).getProperty("fgsindex.indexDir"));
    }
    
    public String getAnalyzer(String indexName) {
        return getIndexProps(indexName).getProperty("fgsindex.analyzer");
    }
    
    public String getStopwordsLocation(String indexName) {
        return getIndexProps(indexName).getProperty("fgsindex.stopwordsLocation");
    }
    
    public URIResolver getURIResolver(String indexName) {
        return (URIResolver)indexNameToUriResolvers.get(getIndexName(indexName));
    }
    
    public String getUntokenizedFields(String indexName) {
        String untokenizedFields = getIndexProps(indexName).getProperty("fgsindex.untokenizedFields");
        if (untokenizedFields == null)
        	untokenizedFields = "";
        return untokenizedFields;
    }
    
    public void setUntokenizedFields(String indexName, String untokenizedFields) {
        getIndexProps(indexName).setProperty("fgsindex.untokenizedFields", untokenizedFields);
    }
    
    public String getDefaultQueryFields(String indexName) {
        return getIndexProps(indexName).getProperty("fgsindex.defaultQueryFields");
    }
    
    public boolean getAllowLeadingWildcard(String indexName) {
    	String allowLeadingWildcard = getIndexProps(indexName).getProperty("fgsindex.allowLeadingWildcard");
        if (allowLeadingWildcard==null || allowLeadingWildcard.equals("")) {
        	allowLeadingWildcard = "false";
          }
        return new Boolean( allowLeadingWildcard );
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
    
    public long getDefaultWriteLockTimeout(String indexName) {
    	long defaultWriteLockTimeout = 1;
		try {
			defaultWriteLockTimeout = Integer.parseInt(getIndexProps(indexName).getProperty("fgsindex.defaultWriteLockTimeout"));
		} catch (NumberFormatException e) {
		}
    	return defaultWriteLockTimeout;
    }
    
    public SearchResultFiltering getSearchResultFiltering() throws ConfigException {
    	SearchResultFiltering srfInstance = null;
        if(searchResultFilteringModuleProperty != null && searchResultFilteringModuleProperty.length()>0) {
            try {
                Class srfClass = Class.forName(searchResultFilteringModuleProperty);
                try {
                	srfInstance = (SearchResultFiltering) srfClass
                    .getConstructor(new Class[] {})
                    .newInstance(new Object[] {});
                } catch (InstantiationException e) {
                    throw new ConfigException("\n*** "+configName
                            + ": fedoragsearch.searchResultFilteringModule="+searchResultFilteringModuleProperty
                            + ": instantiation error.\n"+e.toString());
                } catch (IllegalAccessException e) {
                    throw new ConfigException("\n*** "+configName
                            + ": fedoragsearch.searchResultFilteringModule="+searchResultFilteringModuleProperty
                            + ": instantiation error.\n"+e.toString());
                } catch (InvocationTargetException e) {
                    throw new ConfigException("\n*** "+configName
                            + ": fedoragsearch.searchResultFilteringModule="+searchResultFilteringModuleProperty
                            + ": instantiation error.\n"+e.toString());
                } catch (NoSuchMethodException e) {
                    throw new ConfigException("\n*** "+configName
                            + ": fedoragsearch.searchResultFilteringModule="+searchResultFilteringModuleProperty
                            + ": instantiation error.\n"+e.toString());
                }
            } catch (ClassNotFoundException e) {
                throw new ConfigException("\n*** "+configName
                        + ": fedoragsearch.searchResultFilteringModule="+searchResultFilteringModuleProperty
                        + ": class not found.\n"+e);
            }
        }
        return srfInstance;
    }
    
    public String getSearchResultFilteringType() {
        return fgsProps.getProperty("fedoragsearch.searchResultFilteringType");
    }
    
    public boolean isSearchResultFilteringActive(String type) {
        if (type.equals(fgsProps.getProperty("fedoragsearch.searchResultFilteringType")))
        	return true;
    	else
    		return false;
    }
    
    public String getXsltProcessor() {
        return fgsProps.getProperty("fedoragsearch.xsltProcessor");
    }
    
    public GenericOperationsImpl getOperationsImpl(String indexNameParam) throws ConfigException {
        return getOperationsImpl(null, indexNameParam);
    }
    
    public GenericOperationsImpl getOperationsImpl(String fgsUserNameParam, String indexNameParam)
    throws ConfigException {
        return getOperationsImpl(fgsUserNameParam, indexNameParam, null);
    }
    
    public GenericOperationsImpl getOperationsImpl(String fgsUserNameParam, String indexNameParam, Map<String, Set<String>> fgsUserAttributes)
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
        ops.init(fgsUserNameParam, indexName, this, fgsUserAttributes);
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
    
    public String getProperty(String propertyName) throws ConfigException {
    	String propertyValue = null;
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
        		propertyValue = props.getProperty(propName);
        	} else {
                throw new ConfigException("property " + propertyName + " not found");
        	}
        }
//        if (logger.isDebugEnabled())
//            logger.debug("getProperty " + propertyName + "=" + propertyValue);
    	return propertyValue;
    }
    
    private Config setProperty(String propertyName, String propertyValue) throws ConfigException {
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
        
    private Properties getFgsConfigProps(String propFilePath) throws ConfigException {
    	Properties props = null;
        try {
            InputStream propStream = Config.class.getResourceAsStream(propFilePath);
            if (propStream != null) {
            	props = new Properties();
            	props.load(propStream);
            	propStream.close();
                if (logger.isInfoEnabled())
                    logger.info("getFgsConfigProps "+propFilePath+"=" + props.toString());
            } else {
                if (logger.isInfoEnabled())
                    logger.info("getFgsConfigProps "+propFilePath+" not found in classpath");
                throw new ConfigException(
                        "*** getFgsConfigProps "+propFilePath+" not found in classpath");
            }
        } catch (Exception e) {
            if (logger.isInfoEnabled())
                logger.info("getFgsConfigProps "+propFilePath+":\n" + e.toString());
            throw new ConfigException(
                    "*** getFgsConfigProps "+propFilePath+":\n" + e.toString());
        }
        return props;
    }
    
    private boolean objectExists(String pid) throws ConfigException {
        if (logger.isDebugEnabled())
            logger.debug("objectExists? pid="+pid);
		boolean exists = true;
		FedoraAPIA apia;
		try {
			apia = getAPIA();
		} catch (Exception e) {
            throw new ConfigException(
                    "*** objectExists :\n" + e.toString());
		}
		try {
			apia.getObjectProfile(pid, null);
		} catch (Exception e) {
            exists = false;
	        if (logger.isDebugEnabled())
	            logger.debug("getObjectProfile :\n" + e.toString());
		}
        if (logger.isDebugEnabled())
            logger.debug("objectExists "+exists+" pid="+pid);
        return exists;
    }
    
    private FedoraClient getFgsConfigObjectsClient() throws ConfigException {
        if (logger.isDebugEnabled())
            logger.debug("getFgsConfigObjectsClient");
        if (fgsconfigObjectsClient != null) {
        	return fgsconfigObjectsClient;
        }
        if (logger.isDebugEnabled())
            logger.debug("getFgsConfigObjectsClient new");
		try {
			fgsconfigObjectsClient = new FedoraClient(
					fcoProps.getProperty("fgsconfigObjects.fedoraSoap"),
					fcoProps.getProperty("fgsconfigObjects.fedoraUser"),
					fcoProps.getProperty("fgsconfigObjects.fedoraPass")
					);
		} catch (Exception e) {
            throw new ConfigException("getFgsConfigObjectsClient exception="+e.toString());
		}
        if (logger.isDebugEnabled())
            logger.debug("getFgsConfigObjectsClient new="+fcoProps.getProperty("fgsconfigObjects.fedoraSoap"));
    	String trustStorePath = fcoProps.getProperty("fgsconfigObjects.trustStorePath");
    	String trustStorePass = fcoProps.getProperty("fgsconfigObjects.trustStorePass");
    	if (trustStorePath!=null && trustStorePath.length()>0)
    		System.setProperty("javax.net.ssl.trustStore", trustStorePath);
    	if (trustStorePass!=null && trustStorePass.length()>0)
    		System.setProperty("javax.net.ssl.trustStorePassword", trustStorePass);
        return fgsconfigObjectsClient;
    }
    
    private FedoraAPIA getAPIA() throws ConfigException {
    	FedoraClient fedoraClient = getFgsConfigObjectsClient();
        if (logger.isDebugEnabled())
            logger.debug("getAPIA getFgsConfigObjectsClient ="+fedoraClient);
        FedoraAPIA apia = null;
		try {
			apia = fedoraClient.getAPIA();
		} catch (Exception e) {
            throw new ConfigException("getAPIA exception="+e.toString());
		}
        return apia;
    }
    
    private FedoraAPIM getAPIM() throws ConfigException {
        FedoraAPIM apim = null;
		try {
			apim = getFgsConfigObjectsClient().getAPIM();
		} catch (Exception e) {
            throw new ConfigException("getAPIM exception="+e.toString());
		}
        return apim;
    }
    
    private void createObject(String pid, String label) throws ConfigException {
		StringBuffer objectXML = new StringBuffer();
		objectXML.append("<foxml:digitalObject PID=\""+pid+"\" VERSION=\"1.1\"");
		objectXML.append("\nxmlns:foxml=\"info:fedora/fedora-system:def/foxml#\"");
		objectXML.append("\nxmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
		objectXML.append("\nxsi:schemaLocation=\"info:fedora/fedora-system:def/foxml# http://www.fedora.info/definitions/1/0/foxml1-1.xsd\"");
		objectXML.append("\n>");
		objectXML.append("\n<foxml:objectProperties>");
		objectXML.append("\n<foxml:property NAME=\"info:fedora/fedora-system:def/model#state\" VALUE=\"Active\"/>");
		objectXML.append("\n<foxml:property NAME=\"info:fedora/fedora-system:def/model#label\" VALUE=\""+label+"\"/>");
		objectXML.append("\n<foxml:property NAME=\"info:fedora/fedora-system:def/model#ownerId\" VALUE=\"fgsAdmin\"/>");
		objectXML.append("\n</foxml:objectProperties>");
		objectXML.append("\n</foxml:digitalObject>");
	    try {
	    	getAPIM().ingest(objectXML.toString().getBytes(), "info:fedora/fedora-system:FOXML-1.1", label);
		} catch (Exception e) {
	        throw new ConfigException("ingest exception="+e.toString());
		}
	    if (logger.isDebugEnabled())
	        logger.debug("ok createObject pid="+pid);
    }
        
    private void setFgsConfigObjects() throws ConfigException {
    	fcoProps = getFgsConfigProps("/"+finalConfigName+"/fgsconfigObjects.properties");
    	String finalConfigPath = fcoProps.getProperty("fgsconfigObjects.finalConfigPath");
    	File finalConfigRoot = new File(finalConfigPath);
    	if (!finalConfigRoot.isDirectory()) {
            throw new ConfigException("setFgsConfigObjects finalConfigPath='"+finalConfigPath+"' is not a directory.");
    	}
        File[] rootFiles = finalConfigRoot.listFiles();
        if (rootFiles == null) {
            throw new ConfigException("setFgsConfigObjects finalConfigPath='"+finalConfigPath+"' has no files.");
    	}
    	if (!objectExists(configRootObjectPid)) {
        	createObject(configRootObjectPid, "FgsConfig root object");
    	}
    	setConfigFilesAsDatastreams(rootFiles);
    }
    
    private void setConfigFilesAsDatastreams(File[] files) throws ConfigException {
        for (File file : files) {
        	if (file.isFile()) {
        		if (!file.getName().startsWith(".")) {
                    setConfigFileAsDatastream(file);
        		}
        	} else if (file.isDirectory()) {
        		setConfigFilesAsDatastreams(file.listFiles());
        	}
        }
    }
    
    private void setConfigFileAsDatastream(File file) throws ConfigException {
        String baseUrl = fcoProps.getProperty("fgsconfigObjects.fedoraSoap");
	    int i = baseUrl.indexOf("://");
	    String protocol = baseUrl.substring(0, i);
	    int port = 80;
	    int j = baseUrl.indexOf(":",i+3);
	    if (j<0) {
	    	j = baseUrl.indexOf("/",i+3);
	    } else {
	    	port = Integer.parseInt(baseUrl.substring(j+1, baseUrl.indexOf("/", j+1)));
	    }
	    String host = baseUrl.substring(i+3, j);
//	    String context = baseUrl.substring(baseUrl.indexOf("/", j));
		String user = fcoProps.getProperty("fgsconfigObjects.fedoraUser");
		String pwd = fcoProps.getProperty("fgsconfigObjects.fedoraPass");
		String dsid;
		try {
			dsid = file.getCanonicalPath();
			int k = dsid.indexOf("/"+finalConfigName);
			dsid = dsid.substring(k+1).replaceAll("/", "_");
		} catch (IOException e) {
            throw new ConfigException("setConfigFileAsDatastream file.getCanonicalPath() exception="+e.toString());
		}
		InputStream fileContent;
		try {
			fileContent = new FileInputStream(file);
		} catch (FileNotFoundException e) {
            throw new ConfigException("setConfigFileAsDatastream upload exception="+e.toString());
		}
	    if (logger.isDebugEnabled())
	        logger.debug("setConfigFileAsDatastream pid="+configRootObjectPid+" dsid="+dsid+" protocol="+protocol+" host="+host+" port="+port+" user="+user+" pwd="+pwd);
        String dsLocation = "";
		try {
//			Uploader uploader = new Uploader(protocol, host, port, context, user, pwd);
			Uploader uploader = new Uploader(protocol, host, port, user, pwd);
			dsLocation = uploader.upload(fileContent);
		} catch (Exception e) {
            throw new ConfigException("setConfigFileAsDatastream upload exception="+e.toString());
		}
		MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
		String mimeType = mimeTypesMap.getContentType(file);
		String fileExtension = file.getName().substring(file.getName().lastIndexOf(".")+1);
		if (fileExtension.equals("txt")) mimeType = "text/plain";
		if (fileExtension.equals("properties")) mimeType = "text/plain";
		if (fileExtension.equals("xml")) mimeType = "text/xml";
		if (fileExtension.equals("xslt")) mimeType = "text/xml";
		if (fileExtension.equals("wsdd")) mimeType = "text/xml";
	    if (logger.isDebugEnabled())
	        logger.debug("setConfigFileAsDatastream pid="+configRootObjectPid+" dsid="+dsid+" mimeType="+mimeType+" dsLocation="+dsLocation);
        try {
	        getAPIM().modifyDatastreamByReference(configRootObjectPid, dsid, null, "contents of configuration file", mimeType, null, dsLocation, "DISABLED", null, "add datastream", false);
		} catch (Exception e) {
	        try {
		        getAPIM().addDatastream(configRootObjectPid, dsid, null, "contents of configuration file", true, mimeType, null, dsLocation, "M", "A", "DISABLED", null, "add datastream");
			} catch (Exception e1) {
		        throw new ConfigException("modifyDatastream exception=\n"+e.toString()+"\naddDatastream exception=\n"+e1.toString());
			}
		}
	    if (logger.isDebugEnabled())
	        logger.debug("ok setConfigFileAsDatastream pid="+configRootObjectPid+" dsid="+dsid);
    }
        
    private void getFgsConfigObjects() throws ConfigException {
//    	writes all the configuration datastreams to the configuration files
    	fcoProps = getFgsConfigProps("/"+finalConfigName+"/fgsconfigObjects.properties");
    	if (!objectExists(configRootObjectPid)) {
            throw new ConfigException("getFgsConfigObjects: the object '"+configRootObjectPid+"' does not exist.");
    	}
    	String finalConfigPath = fcoProps.getProperty("fgsconfigObjects.finalConfigPath");
    	File finalConfigRoot = new File(finalConfigPath);
    	if (!finalConfigRoot.isDirectory()) {
            throw new ConfigException("setFgsConfigObjects finalConfigPath='"+finalConfigPath+"' is not a directory.");
    	}
		DatastreamDef[] dsds = null;
        try {
	        dsds = getAPIA().listDatastreams(configRootObjectPid, null);
		} catch (Exception e) {
            throw new ConfigException("getFgsConfigObjects listDatastreams exception="+e.toString());
		}
		for (int i=0; i<dsds.length; i++) {
			String dsid = dsds[i].getID();
	        if (logger.isDebugEnabled())
	            logger.debug("getFgsConfigObjects dsid="+dsid);
	        int j = dsid.indexOf("_");
	        if (j>-1 && finalConfigName.equals(dsid.substring(0,j))) {
				File configFile = new File(finalConfigRoot, dsid.substring(j).replaceAll("_", "/"));
		        StringBuffer fileContent = new StringBuffer();
		        MIMETypedStream mts = null;
				try {
					mts = getAPIA().getDatastreamDissemination(configRootObjectPid, dsid, null);
				} catch (Exception e) {
		            throw new ConfigException("getFgsConfigObjects getDatastreamDissemination exception="+e.toString());
				}
		        byte[] ds = null;
		        if (mts!=null) {
		            ds = mts.getStream();
		            InputStreamReader isr = new InputStreamReader(new ByteArrayInputStream(ds));
		            try {
		                int c = isr.read();
		                while (c>-1) {
		                	fileContent.append((char)c);
		                    c=isr.read();
		                }
		            } catch (Exception e) {
		                throw new ConfigException("getFgsConfigObjects datastream read exception="+e.toString());
		            }
		        }
		        BufferedWriter bufferedWriter = null;
	            try {
					bufferedWriter = new BufferedWriter(new FileWriter(configFile));
				} catch (IOException e) {
		            throw new ConfigException("getFgsConfigObjects bufferedWriter exception="+e.toString());
				}
                if (bufferedWriter == null) {
		            throw new ConfigException("getFgsConfigObjects bufferedWriter = null");
                }
		        try {
		            bufferedWriter.write(fileContent.toString());
		        } catch (Exception e) {
		            throw new ConfigException("getFgsConfigObjects datastream write exception="+e.toString());
		        } finally {
		            try {
	                    bufferedWriter.flush();
	                    bufferedWriter.close();
		            } catch (IOException e) {
			            throw new ConfigException("getFgsConfigObjects datastream close exception="+e.toString());
		            }
		        }
		        if (logger.isDebugEnabled())
		            logger.debug("getFgsConfigObjects ok dsid="+dsid);
	        }
		}
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
