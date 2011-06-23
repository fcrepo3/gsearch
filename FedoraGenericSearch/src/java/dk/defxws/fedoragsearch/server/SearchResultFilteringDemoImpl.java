//$Id:  $
/*
 * <p><b>License and Copyright: </b>The contents of this file is subject to the
 * same open source license as the Fedora Repository System at www.fedora-commons.org
 * Copyright &copy; 2006, 2007, 2008 by The Technical University of Denmark.
 * All rights reserved.</p>
 */
package dk.defxws.fedoragsearch.server;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import dk.defxws.fedoragsearch.server.errors.GenericSearchException;
import fedora.client.FedoraClient;
import fedora.common.Constants;
import fedora.server.access.FedoraAPIA;
import fedora.server.management.FedoraAPIM;

/**
 * This demo implementation of SearchResultFiltering shall reflect the XACML policies
 * for the two demo smiley roles SmileyAdmin and SmileyUser
 * 
 * @author  gsp@dtic.dtu.dk
 * @version
 */
public class SearchResultFilteringDemoImpl implements SearchResultFiltering {
    
    private static final Logger logger =
        Logger.getLogger(SearchResultFilteringDemoImpl.class);

    private static final Map fedoraClients = new HashMap();
    
    public String selectIndexNameForPresearch(String fgsUserName, String indexNameParam) throws java.rmi.RemoteException {
    	String indexName = indexNameParam;
    	String userRole = getUserRole(fgsUserName);
    	if ("SmileyAdmin".equals(userRole))
    		indexName = "SmileyAdminIndex";
    	else if ("SmileyUser".equals(userRole))
			indexName = "SmileyUserIndex";
    	else if ("administrator".equals(userRole))
			indexName = "AllObjectsIndex";
    	return indexName;
    }

    public String rewriteQueryForInsearch(String fgsUserName, String indexName, String query) throws java.rmi.RemoteException {
    	// query rewriting shall correspond to the additional index field(s) in the xslt indexing stylesheet.
    	String rewrittenQuery = query;
    	String userRole = getUserRole(fgsUserName);
    	if ("SmileyAdmin".equals(userRole))
    		rewrittenQuery = "( " + query + " ) AND smiley AND PID:demo*";
    	else if ("SmileyUser".equals(userRole))
    		rewrittenQuery = "( " + query + " ) AND smiley AND PID:demo* NOT PID:\"demo:SmileyStuff\"";
    	return rewrittenQuery;
    }
    
    public StringBuffer filterResultsetForPostsearch(
    		String fgsUserName, 
    		StringBuffer resultSetXml,
    		Config config) 
    throws java.rmi.RemoteException {
    	StringBuffer result = resultSetXml;
    	//foreach hit in resultset, evaluate XACML policies, if not deny (~permit) then include in result
    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    	DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
            throw new GenericSearchException("filterResultsetForPostsearch:\n" + e.toString());
		}
        StringReader sr = new StringReader(resultSetXml.toString());
		Document document = null;
    	try {
			document = builder.parse(new InputSource(sr));
		} catch (SAXException e) {
            throw new GenericSearchException("filterResultsetForPostsearch:\n" + e.toString());
		} catch (IOException e) {
            throw new GenericSearchException("filterResultsetForPostsearch:\n" + e.toString());
		}
		Element gfindObjectsElement = (Element) document.getElementsByTagName("gfindObjects").item(0);
		NodeList objects = document.getElementsByTagName("object");
		int hitsDenied = 0;
		for (int i=0; i<objects.getLength(); i++) {
			Element objectElement = (Element) objects.item(i);
			NodeList fieldNodes = objectElement.getElementsByTagName("field"); 
			Element fieldElement = null;
			String pid = null;
			String repositoryName = config.getRepositoryName(null);
			for (int j=0; j<fieldNodes.getLength(); j++) {
				fieldElement = (Element) fieldNodes.item(j);
				if ("PID".equals(fieldElement.getAttribute("name"))) {
					pid = fieldElement.getTextContent();
				}
				if ("repositoryName".equals(fieldElement.getAttribute("name"))) {
					repositoryName = fieldElement.getTextContent();
				}
			}
			if (pid!=null) {
//	            FedoraAPIA apia = getAPIA(repositoryName, 
//	            		config.getFedoraSoap(repositoryName), 
//	            		fgsUserName, 
//	            		fgsUserName, 
//	            		config.getTrustStorePath(repositoryName), 
//	            		config.getTrustStorePass(repositoryName) );
//	        	try {
//					ObjectProfile profile = apia.getObjectProfile(pid, null);
//			        if (logger.isDebugEnabled())
//			            logger.debug("filterResultsetForPostsearch pid="+pid+" profile.label="+profile.getObjLabel());
		            FedoraAPIM apim = getAPIM(repositoryName, 
		            		config.getFedoraSoap(repositoryName), 
		            		fgsUserName, 
		            		fgsUserName, 
		            		config.getTrustStorePath(repositoryName), 
		            		config.getTrustStorePass(repositoryName) );
		            String fedoraVersion = config.getFedoraVersion(repositoryName);
		            String format = Constants.FOXML1_0.uri;
		            if(fedoraVersion != null && fedoraVersion.startsWith("2.")) {
		                format = "foxml1.0";
		            }
		            try {
		            	byte[] foxmlRecord = apim.export(pid, format, "public");
				        if (logger.isDebugEnabled())
				            logger.debug("filterResultsetForPostsearch pid="+pid+" foxmlRecord="+foxmlRecord);
				} catch (java.rmi.RemoteException e) {
					hitsDenied++;
			        if (logger.isDebugEnabled())
		            logger.debug("filterResultsetForPostsearch hitsDenied="+hitsDenied+" pid="+pid+"\nexception="+e.getMessage());
			        objectElement.setAttribute("hitDeniedNo", Integer.toString(hitsDenied));
				}
			} else {
				hitsDenied++;
		        if (logger.isDebugEnabled())
		            logger.debug("filterResultsetForPostsearch hitsDenied="+hitsDenied+" pid="+pid);
			}
		}
		gfindObjectsElement.setAttribute("hitsDenied", Integer.toString(hitsDenied));
        String xsltPath = config.getConfigName()+"/index/"+config.getIndexName(null)+"/copyXml";
        result = (new GTransformer()).transform(
        		xsltPath,
        		new DOMSource(document),
        		new String[0]);
    	return result;
    }
    
    private String getUserRole(String fgsUserName) {
    	//fetch user role from ldap or ... shortcut for this demo:
    	//assuming that the user name implies the role
    	String userRole = "";
    	if (fgsUserName.startsWith("smileyAdmin"))
    		userRole = "SmileyAdmin";
    	else if (fgsUserName.startsWith("smileyUser"))
    		userRole = "SmileyUser";
    	else if (fgsUserName.equals("fedoraAdmin"))
    		userRole = "administrator";
    	return userRole;
    }

    private static FedoraClient getFedoraClient(
    		String repositoryName,
    		String fedoraSoap,
    		String fedoraUser,
    		String fedoraPass)
            throws GenericSearchException {
        try {
            String baseURL = getBaseURL(fedoraSoap);
            String user = fedoraUser; 
            String clientId = user + "@" + baseURL;
            synchronized (fedoraClients) {
                if (fedoraClients.containsKey(clientId)) {
                    return (FedoraClient) fedoraClients.get(clientId);
                } else {
                    FedoraClient client = new FedoraClient(baseURL,
                            user, fedoraPass);
                    fedoraClients.put(clientId, client);
                    return client;
                }
            }
        } catch (Exception e) {
            throw new GenericSearchException("Error getting FedoraClient"
                    + " for repository: " + repositoryName, e);
        }
    }

    private static String getBaseURL(String fedoraSoap)
            throws Exception {
        final String end = "/services";
        String baseURL = fedoraSoap;
        if (fedoraSoap.endsWith(end)) {
            return fedoraSoap.substring(0, fedoraSoap.length() - end.length());
        } else {
            throw new Exception("Unable to determine baseURL from fedoraSoap"
                    + " value (expected it to end with '" + end + "'): "
                    + fedoraSoap);
        }
    }

    private static FedoraAPIA getAPIA(
    		String repositoryName,
    		String fedoraSoap,
    		String fedoraUser,
    		String fedoraPass,
    		String trustStorePath,
    		String trustStorePass)
    throws GenericSearchException {
    	if (trustStorePath!=null)
    		System.setProperty("javax.net.ssl.trustStore", trustStorePath);
    	if (trustStorePass!=null)
    		System.setProperty("javax.net.ssl.trustStorePassword", trustStorePass);
    	FedoraClient client = getFedoraClient(repositoryName, fedoraSoap, fedoraUser, fedoraPass);
    	try {
    		return client.getAPIA();
    	} catch (Exception e) {
    		throw new GenericSearchException("Error getting API-A stub"
    				+ " for repository: " + repositoryName, e);
    	}
    }
    
    private static FedoraAPIM getAPIM(
    		String repositoryName,
    		String fedoraSoap,
    		String fedoraUser,
    		String fedoraPass,
    		String trustStorePath,
    		String trustStorePass)
    throws GenericSearchException {
    	if (trustStorePath!=null)
    		System.setProperty("javax.net.ssl.trustStore", trustStorePath);
    	if (trustStorePass!=null)
    		System.setProperty("javax.net.ssl.trustStorePassword", trustStorePass);
    	FedoraClient client = getFedoraClient(repositoryName, fedoraSoap, fedoraUser, fedoraPass);
    	try {
    		return client.getAPIM();
    	} catch (Exception e) {
    		throw new GenericSearchException("Error getting API-M stub"
    				+ " for repository: " + repositoryName, e);
    	}
    }

}
