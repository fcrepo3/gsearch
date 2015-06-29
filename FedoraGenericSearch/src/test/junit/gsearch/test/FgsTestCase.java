//$Id:  $
package gsearch.test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;

import org.custommonkey.xmlunit.XMLTestCase;

import dk.defxws.fedoragsearch.client.SOAPClient;

import sun.misc.BASE64Encoder;

/**
 * Base class for GSearch Test Cases 
 */
public abstract class FgsTestCase
        extends XMLTestCase {
    
    private static Object content;
    
    protected static StringBuffer doOp(String urlString) throws Exception {
    	StringBuffer result = new StringBuffer("<resultPage><error><message>System property fedoragsearch.clientType must be 'REST' or 'SOAP'</error></message></resultPage>");
    	if ("REST".equals(System.getProperty("fedoragsearch.clientType")))
    		result = doRESTOp(urlString);
    	else if ("SOAP".equals(System.getProperty("fedoragsearch.clientType")))
    		result = doSOAPOp(urlString);
    	else 
        	throw new Exception(result.toString());
        return result;
    }
    
    protected static StringBuffer doRESTOp(String urlString) throws Exception {
    	StringBuffer result = new StringBuffer();
        String restUrl = urlString;
        int p = restUrl.indexOf("://");
        if (p<0)
        	restUrl = System.getProperty("fedoragsearch.protocol")+"://"
        				+System.getProperty("fedoragsearch.hostport")+"/"
        				+System.getProperty("fedoragsearch.path")
        				+restUrl;
        URL url = null;
        url = new URL(restUrl);
        URLConnection conn = null;
        conn = url.openConnection();
        conn.setRequestProperty("Authorization", 
        		"Basic "+(new BASE64Encoder()).encode((System.getProperty("fedoragsearch.fgsUserName")+":"+System.getProperty("fedoragsearch.fgsPassword")).getBytes()));
        conn.connect();
        content = null;
        content = conn.getContent();
        String line;
        BufferedReader br = new BufferedReader(new InputStreamReader((InputStream)content));
        while ((line = br.readLine())!=null)
            result.append(line);
//        if (result.indexOf("<error><message>") > -1)
//        	throw new Exception(result.toString());
        return result;
    }
    
    protected static StringBuffer doSOAPOp(String urlString) throws Exception {
    	StringBuffer result = new StringBuffer();
        String params = urlString;
        SOAPClient client = new SOAPClient();
        int p = params.indexOf("?");
        if (p>0) 
        	params = urlString.substring(p);
        String op = getParamValue(params, "operation");
        String restUrl = System.getProperty("fedoragsearch.protocol")+"://"
						+System.getProperty("fedoragsearch.hostport")
						+"/fedoragsearch/services/FgsOperations";
        if ("getRepositoryInfo".equals(op)) {
        	result = new StringBuffer(
        			client.getRepositoryInfo(
        			restUrl, 
        			getParamValue(params, "repositoryName"), 
        			getParamValue(params, "resultPageXslt")));
        }
        if ("getIndexInfo".equals(op)) {
        	result = new StringBuffer(
        			client.getIndexInfo(
        			restUrl, 
        			getParamValue(params, "indexName"), 
        			getParamValue(params, "resultPageXslt")));
        }
        if ("browseIndex".equals(op)) {
        	result = new StringBuffer(
        			client.browseIndex(
        			restUrl, 
        			getParamValue(params, "startTerm"), 
        			getParamInt(params, "termPageSize", 20), 
        			getParamValue(params, "fieldName"), 
        			getParamValue(params, "indexName"), 
        			getParamValue(params, "resultPageXslt")));
        }
        if ("gfindObjects".equals(op)) {
        	result = new StringBuffer(
        			client.gfindObjects(
        			restUrl, 
        			URLDecoder.decode(getParamValue(params, "query"), "UTF-8"), 
        			getParamValue(params, "indexName"), 
        			getParamInt(params, "hitPageStart", 1), 
        			getParamInt(params, "hitPageSize", 10), 
        			getParamInt(params, "snippetsMax", 3), 
        			getParamInt(params, "fieldMaxLength", 100), 
        			getParamValue(params, "sortFields"), 
        			getParamValue(params, "resultPageXslt")));
        }
        if ("updateIndex".equals(op)) {
        	result = new StringBuffer(
        			client.updateIndex(
        			restUrl, 
        			getParamValue(params, "action"), 
        			getParamValue(params, "value"), 
        			getParamValue(params, "repositoryName"), 
        			getParamValue(params, "indexName")));
        }
        return result;
    }
    
    protected StringBuffer doIndexOp(String urlString) throws Exception {
//        Config config = Config.getCurrentConfig();    
    	StringBuffer result = new StringBuffer();
        String restUrl = urlString;
//        int p = restUrl.indexOf("://");
//        if (p<0)
//        	restUrl = config.getIndexBase("")+"/"
//        				+restUrl;
        URL url = null;
        url = new URL(restUrl);
        URLConnection conn = null;
        conn = url.openConnection();
        conn.connect();
        content = null;
        content = conn.getContent();
        String line;
        BufferedReader br = new BufferedReader(new InputStreamReader((InputStream)content));
        while ((line = br.readLine())!=null)
            result.append(line);
//        if (result.indexOf("<error><message>") > -1)
//        	throw new Exception(result.toString());
        return result;
    }
    
    protected static String getParamValue(String params, String param) throws Exception {
    	String result = "";
        int p = params.indexOf(param+"=");
        if (p>=0) {
        	int q = params.indexOf("&", p+param.length()+1);
        	if (q<0)
            	result = params.substring(p+param.length()+1);
        	else
            	result = params.substring(p+param.length()+1, q);
        }
    	return result;
    }
    
    protected static int getParamInt(String params, String param, int defaultInt) throws Exception {
    	int result = defaultInt;
    	String stringValue = getParamValue(params, param);
        if (stringValue.length()>0) {
            try {
            	result = Integer.parseInt(stringValue);
            } catch (NumberFormatException nfe) {
            }
        }
    	return result;
    }
    
    protected void delay(int ms) {
    	try { Thread.sleep(ms); }
    	catch ( Exception e ) { }
    }
}
