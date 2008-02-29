//$Id$
/*
 * <p><b>License and Copyright: </b>The contents of this file is subject to the
 * same open source license as the Fedora Repository System at www.fedora-commons.org
 * Copyright &copy; 2006, 2007, 2008 by The Technical University of Denmark.
 * All rights reserved.</p>
 */
package dk.defxws.fedoragsearch.client;

import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

/**
 * Performs SOAP operations from command line with runSOAPClient.
 * 
 * @author  gsp@dtv.dk
 * @version 
 */
public class SOAPClient {
    
    private OperationsServiceLocator opsService = new OperationsServiceLocator();
    private dk.defxws.fedoragsearch.client.Operations ops;
    
    public SOAPClient() {
        try {
            ops = opsService.getFgsOperations();
        } catch (ServiceException e) {
            e.printStackTrace();
        }
    }
    
    public String updateIndex(
            String restUrl,
            String action,
            String value,
            String repositoryName,
            String indexName) {
        String result = "";
        try {
            opsService.setEndpointAddress("FgsOperations", restUrl);
            opsService.setFgsOperationsEndpointAddress(restUrl);
            opsService.setFgsOperationsWSDDServiceName("FgsOperations");
            ops = opsService.getFgsOperations();
        } catch (ServiceException e) {
            e.printStackTrace();
        }
        try {
            result = ops.updateIndex(action, value, repositoryName, indexName, "", "");
        } catch (RemoteException e1) {
            e1.printStackTrace();
        }
        return result;
    }
    
    public String browseIndex(
            String restUrl,
            String startTerm,
            int termPageSize,
            String fieldName,
            String indexName,
            String resultPageXslt) {
        String result = "";
        try {
            opsService.setEndpointAddress("FgsOperations", restUrl);
            opsService.setFgsOperationsEndpointAddress(restUrl);
            opsService.setFgsOperationsWSDDServiceName("FgsOperations");
            ops = opsService.getFgsOperations();
        } catch (ServiceException e) {
            e.printStackTrace();
        }
        try {
            result = ops.browseIndex(startTerm, termPageSize, fieldName, indexName, resultPageXslt);
        } catch (RemoteException e1) {
            e1.printStackTrace();
        }
        return result;
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
        String result = "";
        try {
            opsService.setEndpointAddress("FgsOperations", restUrl);
            opsService.setFgsOperationsEndpointAddress(restUrl);
            opsService.setFgsOperationsWSDDServiceName("FgsOperations");
            ops = opsService.getFgsOperations();
        } catch (ServiceException e) {
            e.printStackTrace();
        }
        try {
            result = ops.gfindObjects(query, hitPageStart, hitPageSize, snippetsMax, fieldMaxLength, indexName, sortFields, resultPageXslt);
        } catch (RemoteException e1) {
            e1.printStackTrace();
        }
        return result;
    }
    
    public String getRepositoryInfo(
            String restUrl,
            String repositoryName,
            String resultPageXslt) {
        String result = "";
        try {
            opsService.setEndpointAddress("FgsOperations", restUrl);
            opsService.setFgsOperationsEndpointAddress(restUrl);
            opsService.setFgsOperationsWSDDServiceName("FgsOperations");
            ops = opsService.getFgsOperations();
        } catch (ServiceException e) {
            e.printStackTrace();
        }
        try {
            result = ops.getRepositoryInfo(repositoryName, resultPageXslt);
        } catch (RemoteException e1) {
            e1.printStackTrace();
        }
        return result;
    }
    
    public String getIndexInfo(
            String restUrl,
            String indexName,
            String resultPageXslt) {
        String result = "";
        try {
            opsService.setEndpointAddress("FgsOperations", restUrl);
            opsService.setFgsOperationsEndpointAddress(restUrl);
            opsService.setFgsOperationsWSDDServiceName("FgsOperations");
            ops = opsService.getFgsOperations();
        } catch (ServiceException e) {
            e.printStackTrace();
        }
        try {
            result = ops.getIndexInfo(indexName, resultPageXslt);
        } catch (RemoteException e1) {
            e1.printStackTrace();
        }
        return result;
    }
    
    public static void main(String[] args) {
        try {
            System.out.println("Args");
            for (int i=0; i<args.length; i++) {
                System.out.println(" "+i+"="+args[i]);
            }
            if (args.length<2) usage();
            SOAPClient client = new SOAPClient();
            String hostPort = args[0];
            String restUrl = "http://"+hostPort;
            if (hostPort.indexOf("/")<0)
            	restUrl = "http://"+hostPort+"/fedoragsearch/services/FgsOperations";
            String op = args[1];
            if ("updateIndex".equals(op) ) {
                String action = "";
                if (args.length>2) action = args[2];
                String repositoryName = "";
                String value = "";
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
                String indexName = "";
                if (args.length>5)
                    indexName = args[5];
                String result = client.updateIndex(restUrl, action, value, repositoryName, indexName);
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
                    String result = client.browseIndex(restUrl, startTerm, termPageSize, fieldName, indexName, resultPageXslt);
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
        System.out.println("host:port updateIndex createEmpty [null indexName] # index dir must exist #");
        System.out.println("host:port updateIndex fromFoxmlFiles [repositoryName [filePath [indexName]]]");
        System.out.println("host:port updateIndex fromPid pid [repositoryName [indexName]]");
        System.out.println("host:port updateIndex deletePid pid [repositoryName [indexName]]");
        System.out.println("host:port browseIndex startTerm fieldName [indexName [termPageSize [resultPageXslt]]]");
        System.out.println("host:port gfindObjects query [indexName [hitPageStart [hitPageSize [snippetsMax [fieldMaxLength [sortFields [resultPageXslt]]]]]]]");
        System.out.println("host:port getRepositoryInfo [repositoryName [resultPageXslt]]");
        System.out.println("host:port getIndexInfo [indexName [resultPageXslt]]");
        System.out.println("host:port may be host:port/webappname/servicesname/operationsname, default is '/fedoragsearch/services/FgsOperations'");
        System.exit(1);
    }
    
}
