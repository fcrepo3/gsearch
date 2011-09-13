//$Id$
/*
* <p><b>License and Copyright: </b>The contents of this file is subject to the
* same open source license as the Fedora Repository System at www.fedora-commons.org
* Copyright &copy; 2006, 2007, 2008, 2009, 2010, 2011 by The Technical University of Denmark.
* All rights reserved.</p>
*/
package dk.defxws.fedoragsearch.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;

import java.util.Date;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

import org.apache.log4j.Logger;

import dk.defxws.fedoragsearch.server.errors.ConfigException;
import dk.defxws.fedoragsearch.server.errors.GenericSearchException;

/**
* performs the stylesheet transformations
*
* @author gsp@dtv.dk
* @version
*/
public class GTransformer {
    
    private static final Logger logger =
        Logger.getLogger(GTransformer.class);
    int debuglength = 500;
    
    public GTransformer() {
    }
    
    public Transformer getTransformer(String xsltPath)
    throws ConfigException {
        return getTransformer(xsltPath, null);
    }
    
    public Transformer getTransformer(String xsltPath, URIResolver uriResolver)
    throws ConfigException {
        Transformer transformer = null;
        String xsltPathName = "/"+xsltPath+".xslt";
        try {
            InputStream stylesheet = Config.class.getResourceAsStream(xsltPathName);
            if (stylesheet==null) {
                throw new ConfigException(xsltPathName+" not found");
            }
			TransformerFactory tfactory;
			if ("saxon".equals(Config.getCurrentConfig().getXsltProcessor())) {
				tfactory = new net.sf.saxon.TransformerFactoryImpl();

				if(tfactory instanceof net.sf.saxon.TransformerFactoryImpl) {
					Configuration conf = ((net.sf.saxon.TransformerFactoryImpl)tfactory).getConfiguration();
					try {
						conf.registerExtensionFunction(new GenericOperationsImplDefinition());
					} catch (XPathException e) {
			            throw new ConfigException("getTransformer registerExtensionFunction "+xsltPathName+":\n", e);
					}
					((net.sf.saxon.TransformerFactoryImpl)tfactory).setConfiguration(conf);
				}
			} else {
				tfactory = new org.apache.xalan.processor.TransformerFactoryImpl();
			}
            StreamSource xslt = new StreamSource(stylesheet);
            transformer = tfactory.newTransformer(xslt);
            if (uriResolver!=null)
             transformer.setURIResolver(uriResolver);
        } catch (TransformerConfigurationException e) {
            throw new ConfigException("getTransformer "+xsltPathName+":\n", e);
        } catch (TransformerFactoryConfigurationError e) {
            throw new ConfigException("getTransformerFactory "+xsltPathName+":\n", e);
        }
        if (logger.isDebugEnabled())
            logger.debug("getTransformer transformer="+transformer+" uriResolver="+uriResolver);
        return transformer;
    }

    public StringBuffer transform(String xsltPath, StringBuffer sb, String[] params, String systemId)
    throws ConfigException {
    	if (logger.isDebugEnabled())
    		logger.debug("transform xsltPath="+xsltPath+" sb="+getDebugString(sb.toString())+" systemId="+systemId);
        StringReader sr = new StringReader(sb.toString());
        StreamSource sourceStream = new StreamSource(sr, systemId);
        String xsltPathName = "/"+xsltPath+".xslt";
        URL stylesheet = GTransformer.class.getResource(xsltPathName);
        if (stylesheet==null) {
            throw new ConfigException("transform "+xsltPathName+" not found");
        }
        TransformerFactory tfactory = null;
        try {
        	tfactory = TransformerFactory.newInstance();
        } catch (TransformerFactoryConfigurationError e) {
        	throw new ConfigException("transform "+xsltPathName+":\n", e);
        }
        StreamSource xslt = null;
        try {
        	xslt = new StreamSource(stylesheet.openStream(), stylesheet.toString());
        } catch (IOException e) {
        	throw new ConfigException("transform "+xsltPathName+":\n", e);
        }
        Transformer transformer = null;
        try {
        	transformer = tfactory.newTransformer(xslt);
        } catch (TransformerConfigurationException e) {
        	throw new ConfigException("transform "+xsltPathName+":\n", e);
        }
        for (int i=0; i<params.length; i=i+2) {
            Object value = params[i+1];
            if (value==null) value = "";
            transformer.setParameter((String)params[i], value);
        }
        transformer.setParameter("DATETIME", new Date());
        StreamResult destStream = new StreamResult(new StringWriter());
        try {
            transformer.transform(sourceStream, destStream);
        } catch (TransformerException e) {
            throw new ConfigException("transform "+xsltPathName+":\n", e);
        }
        StringWriter sw = (StringWriter)destStream.getWriter();
        StringBuffer result = sw.getBuffer();
        if (logger.isDebugEnabled())
         logger.debug("transform result=\n"+getDebugString(result.toString()));
        return result;
    }
    
    public void transform(String xsltName, StreamSource sourceStream, StreamResult destStream)
    throws GenericSearchException {
        Transformer transformer = getTransformer(xsltName);
        try {
            transformer.transform(sourceStream, destStream);
        } catch (TransformerException e) {
            throw new GenericSearchException("transform "+xsltName+".xslt:\n", e);
        }
    }

    public StringBuffer transform(String xsltName, Source sourceStream, Object[] params)
    throws GenericSearchException {
        return transform (xsltName, sourceStream, null, params);
    }

    public StringBuffer transform(String xsltName, Source sourceStream, URIResolver uriResolver, Object[] params)
    throws GenericSearchException {
        if (logger.isDebugEnabled())
            logger.debug("xsltName="+xsltName);
        Transformer transformer = getTransformer(xsltName, uriResolver);
        for (int i=0; i<params.length; i=i+2) {
            Object value = params[i+1];
            if (value==null) value = "";
            transformer.setParameter((String)params[i], value);
        }
        transformer.setParameter("DATETIME", new Date());
        StreamResult destStream = new StreamResult(new StringWriter());
        try {
            transformer.transform(sourceStream, destStream);
        } catch (TransformerException e) {
            throw new GenericSearchException("transform "+xsltName+".xslt:\n", e);
        }
        StringWriter sw = (StringWriter)destStream.getWriter();
        return sw.getBuffer();
    }
    
    public void transformToFile(String xsltName, StreamSource sourceStream, Object[] params, String filePath)
    throws GenericSearchException {
        if (logger.isDebugEnabled())
            logger.debug("xsltName="+xsltName);
        Transformer transformer = getTransformer(xsltName);
        for (int i=0; i<params.length; i=i+2) {
            Object value = params[i+1];
            if (value==null) value = "";
            transformer.setParameter((String)params[i], value);
        }
        transformer.setParameter("DATETIME", new Date());
        StreamResult destStream = new StreamResult(new File(filePath));
        try {
            transformer.transform(sourceStream, destStream);
        } catch (TransformerException e) {
            throw new GenericSearchException("transform "+xsltName+".xslt:\n", e);
        }
    }
    
    public StringBuffer transform(String xsltName, StreamSource sourceStream)
    throws GenericSearchException {
        return transform(xsltName, sourceStream, new String[]{});
    }
    
    public StringBuffer transform(String xsltName, StringBuffer sb, String[] params)
    throws GenericSearchException {
        StringReader sr = new StringReader(sb.toString());
        StringBuffer result = transform(xsltName, new StreamSource(sr), params);
        return result;
    }
    
    public StringBuffer transform(String xsltName, StringBuffer sb)
    throws GenericSearchException {
        return transform(xsltName, sb, new String[]{});
    }
    
    private String getDebugString(String debugString) {
     String result = debugString;
     if (debugString.length()>debuglength) {
     result = result.substring(0,debuglength)+"...\n...";
     }
     return result;
    }

	public class GenericOperationsImplDefinition extends ExtensionFunctionDefinition {

		private static final long serialVersionUID = -4369478473099836359L;

		@Override
		public SequenceType[] getArgumentTypes() {
			SequenceType args[] = new SequenceType[8];
			for(int i = 0; i < 8; i++) {
				args[i] = SequenceType.SINGLE_STRING;
			}
			return args;
		}

		@Override
		public StructuredQName getFunctionQName() {
			return new StructuredQName("exts", "java:dk.defxws.fedoragsearch.server.GenericOperationsImpl", "getDatastreamText");
		}

		@Override
		public SequenceType getResultType(SequenceType[] arg0) {
			return SequenceType.SINGLE_STRING;
		}

		@Override
		public ExtensionFunctionCall makeCallExpression() {
			return new GenericOperationsImplCall();
		}

	}
	
	public class GenericOperationsImplCall extends ExtensionFunctionCall {

		private static final long serialVersionUID = -3144688173848090437L;

		@Override
		public SequenceIterator call(SequenceIterator[] arg0,
				XPathContext arg1) throws XPathException {
			Item item = StringValue.makeStringValue(new GenericOperationsImpl().getDatastreamText(arg0[0].next().getStringValue(), arg0[1].next().getStringValue(), arg0[2].next().getStringValue(), arg0[3].next().getStringValue(), arg0[4].next().getStringValue(), arg0[5].next().getStringValue(), arg0[6].next().getStringValue(), arg0[7].next().getStringValue()));
			return SingletonIterator.makeIterator(item);
		}
		
	}
    
}