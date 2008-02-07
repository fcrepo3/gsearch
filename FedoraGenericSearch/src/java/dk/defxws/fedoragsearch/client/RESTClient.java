//$Id$
/*
 * <p><b>License and Copyright: </b>The contents of this file is subject to the
 * same open source license as the Fedora Repository System at www.fedora-commons.org
 * Copyright &copy; 2006, 2007, 2008 by The Technical University of Denmark.
 * All rights reserved.</p>
 */
package dk.defxws.fedoragsearch.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * Performs REST operations from command line with runRESTClient.
 * 
 * @author  gsp@dtv.dk
 * @version 
 */
public class RESTClient {
    
    private Object content;
    
    public String updateIndex(
            String restUrl,
            String action,
            String value,
            String repositoryName,
            String indexName,
            String indexDocXslt) {
        URL url = null;
        try {
            url =
                new URL(
                        restUrl
                        + "?operation=updateIndex"
                        + "&action=" + action
                        + "&value=" + value
                        + "&repositoryName=" + repositoryName
                        + "&indexName=" + indexName
                        + "&indexDocXslt=" + indexDocXslt
                        + "&restXslt=copyXml");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return "";
        }
        URLConnection conn = null;
        try {
            conn = url.openConnection();
            conn.connect();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        content = null;
        try {
            content = conn.getContent();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        System.out.println((InputStream) content);
        String line;
        BufferedReader br = new BufferedReader(new InputStreamReader((InputStream)content));
        try {
            while ((line = br.readLine())!=null)
                System.out.println(line);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return "";
    }
    
    public String browseIndex(
            String restUrl,
            String startTerm,
            String fieldName,
            String indexName,
            int termPageSize,
            String resultPageXslt) {
        URL url = null;
        try {
            url =
                new URL(
                        restUrl
                        + "?operation=browseIndex"
                        + "&startTerm=" + URLEncoder.encode(startTerm, "UTF-8")
                        + "&fieldName=" + fieldName
                        + "&indexName=" + indexName
                        + "&termPageSize=" + termPageSize
                        + "&restXslt=copyXml"
                        + "&resultPageXslt=" + resultPageXslt);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return "";
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
        URLConnection conn = null;
        try {
            conn = url.openConnection();
            conn.connect();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        content = null;
        try {
            content = conn.getContent();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        System.out.println((InputStream) content);
        String line;
        BufferedReader br = new BufferedReader(new InputStreamReader((InputStream)content));
        try {
            while ((line = br.readLine())!=null)
                System.out.println(line);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return "";
    }
    
    public String gfindObjects(
            String restUrl,
            String query,
            String indexName,
            int hitPageStart,
            int hitPageSize,
            int snippetsMax,
            int fieldMaxLength,
            String sortFields,
            String resultPageXslt) {
        URL url = null;
        try {
            url =
                new URL(
                        restUrl
                        + "?operation=gfindObjects"
                        + "&query=" + URLEncoder.encode(query, "UTF-8")
                        + "&indexName=" + indexName
                        + "&hitPageStart=" + hitPageStart
                        + "&hitPageSize=" + hitPageSize
                        + "&snippetsMax=" + snippetsMax
                        + "&fieldMaxLength=" + fieldMaxLength
                        + "&sortFields=" + sortFields
                        + "&restXslt=copyXml"
                        + "&resultPageXslt=" + resultPageXslt);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return "";
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
        URLConnection conn = null;
        try {
            conn = url.openConnection();
            conn.connect();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        content = null;
        try {
            content = conn.getContent();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        System.out.println((InputStream) content);
        String line;
        BufferedReader br = new BufferedReader(new InputStreamReader((InputStream)content));
        try {
            while ((line = br.readLine())!=null)
                System.out.println(line);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return "";
    }
    
    public String getRepositoryInfo(
            String restUrl,
            String repositoryName,
            String resultPageXslt) {
        URL url = null;
        try {
            url =
                new URL(
                        restUrl
                        + "?operation=getRepositoryInfo"
                        + "&repositoryName=" + repositoryName
                        + "&restXslt=copyXml"
                        + "&resultPageXslt=" + resultPageXslt);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return "";
        }
        URLConnection conn = null;
        try {
            conn = url.openConnection();
            conn.connect();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        content = null;
        try {
            content = conn.getContent();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        System.out.println((InputStream) content);
        String line;
        BufferedReader br = new BufferedReader(new InputStreamReader((InputStream)content));
        try {
            while ((line = br.readLine())!=null)
                System.out.println(line);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return "";
    }
    
    public String getIndexInfo(
            String restUrl,
            String indexName,
            String resultPageXslt) {
        URL url = null;
        try {
            url =
                new URL(
                        restUrl
                        + "?operation=getIndexInfo"
                        + "&indexName=" + indexName
                        + "&restXslt=copyXml"
                        + "&resultPageXslt=" + resultPageXslt);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return "";
        }
        URLConnection conn = null;
        try {
            conn = url.openConnection();
            conn.connect();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        content = null;
        try {
            content = conn.getContent();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        System.out.println((InputStream) content);
        String line;
        BufferedReader br = new BufferedReader(new InputStreamReader((InputStream)content));
        try {
            while ((line = br.readLine())!=null)
                System.out.println(line);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return "";
    }
    
    public String configure(
            String restUrl,
            String configName,
            String propertyName,
            String propertyValue) {
        URL url = null;
        try {
            url =
                new URL(
                        restUrl
                        + "?operation=configure"
                        + "&configName=" + configName
                        + "&propertyName=" + propertyName
                        + "&propertyValue=" + propertyValue);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return "";
        }
        URLConnection conn = null;
        try {
            conn = url.openConnection();
            conn.connect();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        content = null;
        try {
            content = conn.getContent();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        System.out.println((InputStream) content);
        String line;
        BufferedReader br = new BufferedReader(new InputStreamReader((InputStream)content));
        try {
            while ((line = br.readLine())!=null)
                System.out.println(line);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return "";
    }
    
    public String run(
            String restUrl,
            String queryString) {
        URL url = null;
        try {
            url =
                new URL(restUrl + queryString);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return "";
        }
        URLConnection conn = null;
        try {
            conn = url.openConnection();
            conn.connect();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        content = null;
        try {
            content = conn.getContent();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        System.out.println((InputStream) content);
        String line;
        BufferedReader br = new BufferedReader(new InputStreamReader((InputStream)content));
        try {
            while ((line = br.readLine())!=null)
                System.out.println(line);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return "";
    }
    
    public static void main(String[] args) {
        try {
            System.out.println("Args");
            for (int i=0; i<args.length; i++) {
                System.out.println(" "+i+"="+args[i]);
            }
            if (args.length<2) usage();
            RESTClient client = new RESTClient();
            String hostPort = args[0];
            int p = hostPort.indexOf("://");
            String restUrl = hostPort;
            if (p<0)
            	restUrl = "http://"+hostPort;
            if (restUrl.indexOf("/", restUrl.indexOf("://")+3)<0)
            	restUrl = restUrl+"/fedoragsearch/rest";
            String op = args[1];
            if ("updateIndex".equals(op) ) {
                String action = "";
                if (args.length>2) action = args[2];
                String repositoryName = "";
                String value = "";
                String indexName = "";
                if (args.length>3)
                    if (action.equals("fromFoxmlFiles"))
                        repositoryName = args[3];
                    else
                    	value = args[3];
                if (args.length>4)
                    if (action.equals("fromFoxmlFiles"))
                        value = args[4];
                    else
                        repositoryName = args[4];
                if (args.length>5)
                    indexName = args[5];
                String indexDocXslt = "";
                if (args.length>6)
                    indexDocXslt = args[6];
                String result = client.updateIndex(restUrl, action, value, repositoryName, indexName, indexDocXslt);
                System.out.println(result);
            }
            else
                if ("browseIndex".equals(op) ) {
                    if (args.length<3) usage();
                    String startTerm = args[2];
                    String fieldName = "";
                    if (args.length>3)
                        fieldName = args[3];
                    String indexName = "";
                    if (args.length>4)
                        indexName = args[4];
                    int termPageSize = 20;
                    if (args.length>5) {
                        try {
                            termPageSize = Integer.parseInt(args[5]);
                        } catch (NumberFormatException nfe) {
                        }
                    }
                    String resultPageXslt = "";
                    if (args.length>6) 
                        resultPageXslt = args[6];
                    String result = client.browseIndex(restUrl, startTerm, fieldName, indexName, termPageSize, resultPageXslt);
                    System.out.println(result);
                }
                else
                    if ("gfindObjects".equals(op) ) {
                        if (args.length<3) usage();
                        String query = args[2];
                        String indexName = "";
                        if (args.length>3)
                            indexName = args[3];
                        int hitPageStart = 1;
                        if (args.length>4) {
                            try {
                                hitPageStart = Integer.parseInt(args[4]);
                            } catch (NumberFormatException nfe) {
                            }
                        }
                        int hitPageSize = 3;
                        if (args.length>5) {
                            try {
                                hitPageSize = Integer.parseInt(args[5]);
                            } catch (NumberFormatException nfe) {
                            }
                        }
                        int snippetsMax = 3;
                        if (args.length>6) {
                            try {
                                snippetsMax = Integer.parseInt(args[6]);
                            } catch (NumberFormatException nfe) {
                            }
                        }
                        int fieldMaxLength = 50;
                        if (args.length>7) {
                            try {
                                fieldMaxLength = Integer.parseInt(args[7]);
                            } catch (NumberFormatException nfe) {
                            }
                        }
                        String sortFields = "";
                        if (args.length>8) 
                        	sortFields = args[8];
                        String resultPageXslt = "";
                        if (args.length>9) 
                            resultPageXslt = args[9];
                        String result = client.gfindObjects(restUrl, query, indexName, hitPageStart, hitPageSize, snippetsMax, fieldMaxLength, sortFields, resultPageXslt);
                        System.out.println(result);
                    }
                    else
                        if ("getRepositoryInfo".equals(op) ) {
                            String repositoryName = "";
                            if (args.length>2)
                                repositoryName = args[2];
                            String resultPageXslt = "";
                            if (args.length>3) 
                                resultPageXslt = args[3];
                            String result = client.getRepositoryInfo(restUrl, repositoryName, resultPageXslt);
                            System.out.println(result);
                        }
                        else
                            if ("getIndexInfo".equals(op) ) {
                                String indexName = "";
                                if (args.length>2)
                                    indexName = args[2];
                                String resultPageXslt = "";
                                if (args.length>3) 
                                    resultPageXslt = args[3];
                                String result = client.getIndexInfo(restUrl, indexName, resultPageXslt);
                                System.out.println(result);
                            }
                            else
                                if ("configure".equals(op) ) {
                                    String configName = "";
                                    String propertyName = "";
                                    String propertyValue = "";
                                    if (args.length>2) {
                                    	configName = args[2];
                                        if (args.length>3) {
                                        	propertyName = args[3];
                                        	propertyValue = args[4];
                                        }
                                    }
                                    String result = client.configure(restUrl, configName, propertyName, propertyValue);
                                    System.out.println(result);
                                }
                                else
                                    if ("?".equals(op.substring(0, 1)) ) {
                                        String result = client.run(restUrl, op);
                                        System.out.println(result);
                                    }
                            else {
                                System.out.println("!!! Error in operation name: "+op+" !!!");
                                usage();
                            }
        } catch (Exception e) {
            System.out.println("Exception in main: " +  e.getMessage());
            e.printStackTrace();
        }		  
    }
    
    public static void usage() {	  
        System.out.println("Usage");
        System.out.println("host:port updateIndex # shows number of index documents #");
        System.out.println("host:port updateIndex createEmpty [indexName] # index dir must exist #");
        System.out.println("host:port updateIndex fromFoxmlFiles [repositoryName [filePath [indexName [indexDocXslt]]]]");
        System.out.println("host:port updateIndex fromPid pid [repositoryName [indexName [indexDocXslt]]]");
        System.out.println("host:port updateIndex deletePid pid [indexName]");
        System.out.println("host:port updateIndex optimize [indexName]");
        System.out.println("host:port browseIndex startTerm fieldName [indexName [termPageSize [resultPageXslt]]]");
        System.out.println("host:port gfindObjects query [indexName [hitPageStart [hitPageSize [snippetsMax [fieldMaxLength [sortFields [resultPageXslt]]]]]]]");
        System.out.println("host:port getRepositoryInfo [repositoryName [resultPageXslt]]");
        System.out.println("host:port getIndexInfo [indexName [resultPageXslt]]");
        System.out.println("host:port configure [configName [propertyName propertyValue]]");
        System.out.println("host:port ?operation=...&...=...&...");
        System.out.println("host:port may be [protocol://]host:port[/webappname/restname], default is http://host:port/fedoragsearch/rest");
        System.exit(1);
    }
    
}
