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

import java.util.Date;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;

import dk.defxws.fedoragsearch.server.errors.GenericSearchException;

/**
 * performs the stylesheet transformations
 * 
 * @author  gsp@dtv.dk
 * @version 
 */
public class GTransformer {
    
    private static final Logger logger =
        Logger.getLogger(GTransformer.class);
    
    public GTransformer() {
    }
    
    /**
     * 
     *
     * @throws TransformerConfigurationException, TransformerException.
     */
    public Transformer getTransformer(String xsltName) 
    throws GenericSearchException {
        Transformer transformer = null;
        String xsltPathName = "/config/"+xsltName+".xslt";
        if (!"rest/".equals(xsltName.substring(0, 5)))
            xsltPathName = "/config/index/"+xsltName+".xslt";
        try {
            InputStream is = null;
            is = GTransformer.class.getResourceAsStream(xsltPathName);
            if (is==null) {
                xsltPathName = "/config/index/common"+xsltName.substring(xsltName.indexOf("/"))+".xslt";
                is = GTransformer.class.getResourceAsStream(xsltPathName);
            }
            TransformerFactory tfactory = TransformerFactory.newInstance();
            StreamSource xslt = new StreamSource(is);
            transformer = tfactory.newTransformer(xslt);
        } catch (TransformerConfigurationException e) {
            throw new GenericSearchException("getTransformer "+xsltPathName+":\n", e);
        } catch (TransformerFactoryConfigurationError e) {
            throw new GenericSearchException("getTransformerFactory "+xsltPathName+":\n", e);
        }
        return transformer;
    }
    
    /**
     * 
     *
     * @throws TransformerConfigurationException, TransformerException.
     */
    public void transform(String xsltName, StreamSource sourceStream, StreamResult destStream) 
    throws GenericSearchException {
        Transformer transformer = getTransformer(xsltName);
        try {
            transformer.transform(sourceStream, destStream);
        } catch (TransformerException e) {
            throw new GenericSearchException("transform "+xsltName+".xslt:\n", e);
        }
    }
    
    /**
     * 
     *
     * @throws TransformerConfigurationException, TransformerException.
     */
    public StringBuffer transform(String xsltName, StreamSource sourceStream, String[] params) 
    throws GenericSearchException {
        if (logger.isDebugEnabled())
            logger.debug("xsltName="+xsltName);
        Transformer transformer = getTransformer(xsltName);
        for (int i=0; i<params.length; i=i+2) {
            Object value = params[i+1];
            if (value==null) value = "";
            transformer.setParameter(params[i], value);
        }
        transformer.setParameter("DATETIME", new Date());
        StreamResult destStream = new StreamResult(new StringWriter());
        try {
            transformer.transform(sourceStream, destStream);
        } catch (TransformerException e) {
            throw new GenericSearchException("transform "+xsltName+".xslt:\n", e);
        }
        StringWriter sw = (StringWriter)destStream.getWriter();
//      if (logger.isDebugEnabled())
//      logger.debug("sw="+sw.getBuffer().toString());
        return sw.getBuffer();
    }
    
    /**
     * 
     *
     * @throws TransformerConfigurationException, TransformerException.
     */
    public StringBuffer transform(String xsltName, StreamSource sourceStream) 
    throws GenericSearchException {
        return transform(xsltName, sourceStream, new String[]{});
    }
    
    /**
     * 
     *
     * @throws TransformerConfigurationException, TransformerException.
     */
    public StringBuffer transform(String xsltName, StringBuffer sb, String[] params) 
    throws GenericSearchException {
//      if (logger.isDebugEnabled())
//      logger.debug("sb="+sb);
        StringReader sr = new StringReader(sb.toString());
        StringBuffer result = transform(xsltName, new StreamSource(sr), params);
//      if (logger.isDebugEnabled())
//      logger.debug("xsltName="+xsltName+" result="+result);
        return result;
    }
    
    /**
     * 
     *
     * @throws TransformerConfigurationException, TransformerException.
     */
    public StringBuffer transform(String xsltName, StringBuffer sb) 
    throws GenericSearchException {
        return transform(xsltName, sb, new String[]{});
    }
    
    public static void main(String[] args) {
        int argCount=2;
        try {
            if (args.length==argCount) {
                File f = new File(args[1]);
                StreamSource ss = new StreamSource(new File(args[1]));
                GTransformer gt = new GTransformer();
                StreamResult destStream = new StreamResult(new StringWriter());
                gt.transform(args[0], ss, destStream);
                StringWriter sw = (StringWriter)destStream.getWriter();
                System.out.print(sw.getBuffer().toString());
            } else {
                throw new IOException("Must supply " + argCount + " arguments.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(
            "Usage: GTransformer xsltName xmlFileName");
        }
    }
    
}
