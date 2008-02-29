//$Id$
/*
 * <p><b>License and Copyright: </b>The contents of this file is subject to the
 * same open source license as the Fedora Repository System at www.fedora-commons.org
 * Copyright &copy; 2006, 2007, 2008 by The Technical University of Denmark.
 * All rights reserved.</p>
 */
package dk.defxws.fgslucene;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import dk.defxws.fedoragsearch.server.errors.GenericSearchException;

/**
 * parses the IndexDocument and generates the Lucene document 
 * 
 * @author  gsp@dtv.dk
 * @version 
 */
public class IndexDocumentHandler extends DefaultHandler {
    
    private static final Logger logger = Logger.getLogger(IndexDocumentHandler.class);

    private Document indexDocument;
    
    private OperationsImpl owner;
    private String repositoryName;
    private StringBuffer elementBuffer;
    private String pid;
    private String fieldName;
    private Field.Index index;
    private Field.Store store;
    private Field.TermVector termVector;
    private float docboost;
    private float boost;
    private String dsId;
    private String dsMimetypes;
    private String bDefPid;
    private String methodName;
    private String parameters;
    private String asOfDateTime;
    
    public IndexDocumentHandler(
            OperationsImpl owner, 
            String repositoryName, 
            String pidOrFilename,
            StringBuffer indexDoc)
    throws GenericSearchException {
        this.owner = owner;
        this.repositoryName = repositoryName;
        elementBuffer = new StringBuffer();
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        SAXParser parser;
        try {
            parser = spf.newSAXParser();
        } catch (ParserConfigurationException e) {
            throw new GenericSearchException("IndexDocument parser error pidOrFilename="+pidOrFilename, e);
        } catch (SAXException e) {
            throw new GenericSearchException("IndexDocument parser error pidOrFilename="+pidOrFilename, e);
        }
        try {
            parser.parse(new InputSource(new StringReader(indexDoc.toString())), this);
        } catch (IOException e) {
            throw new GenericSearchException("IndexDocument parse error pidOrFilename="+pidOrFilename, e);
        } catch (org.xml.sax.SAXParseException e) {
            throw new GenericSearchException("IndexDocument parse error pidOrFilename="+pidOrFilename+" at line: " +
                    e.getLineNumber() + " column " +
                    e.getColumnNumber(), e);
        } catch (SAXException e) {
            throw new GenericSearchException("IndexDocument parse error pidOrFilename="+pidOrFilename, e);
        }
    }
    
    public void startDocument() throws SAXException {
        indexDocument = new Document();
    }
    
    public void startElement(String namespaceURI, String localName,
            String qualifiedName, Attributes attrs) throws SAXException {
        fieldName = "NoFieldName";
        dsId = null;
        dsMimetypes = null;
        bDefPid = null;
        methodName = "";
        parameters = "";
        asOfDateTime = "";
        index = Field.Index.TOKENIZED;
        store = Field.Store.YES;
        termVector = Field.TermVector.NO;
        boost = 1;
        docboost = 1;
        if ("IndexDocument".equals(localName) && attrs != null) {
            for (int i = 0; i < attrs.getLength(); i++) {
                String aName = attrs.getLocalName(i);
                if ("".equals(aName)) { aName = attrs.getQName(i); }
                String val = attrs.getValue(i);
                if (aName=="PID")
                	pid = val.trim();
                if (aName=="boost")
                    try {
                        docboost = Float.parseFloat(val);
                    } catch (NumberFormatException e) {
                        docboost = Float.parseFloat("3");
                    }
            }
            indexDocument.setBoost(docboost);
        }
        if ("IndexField".equals(localName) && attrs != null) {
            for (int i = 0; i < attrs.getLength(); i++) {
                String aName = attrs.getLocalName(i);
                if ("".equals(aName)) { aName = attrs.getQName(i); }
                String val = attrs.getValue(i);
                if (aName=="IFname") fieldName = val;
                if (aName=="dsId") dsId = val;
                if (aName=="dsMimetypes") dsMimetypes = val;
                if (aName=="bDefPid") bDefPid = val;
                if (aName=="methodName") methodName = val;
                if (aName=="parameters") parameters = val;
                if (aName=="asOfDateTime") asOfDateTime = val;
                if (aName=="index") 
                    if ("TOKENIZED".equals(val)) index = Field.Index.TOKENIZED;
                    else if ("UN_TOKENIZED".equals(val)) index = Field.Index.UN_TOKENIZED;
                    else if ("NO".equals(val)) index = Field.Index.NO;
                    else if ("NO_NORMS".equals(val)) index = Field.Index.NO_NORMS;
                if (aName=="store") 
                    if ("YES".equals(val)) store = Field.Store.YES;
                    else if ("NO".equals(val)) store = Field.Store.NO;
                    else if ("COMPRESS".equals(val)) store = Field.Store.COMPRESS;
                if (aName=="termVector") 
                    if ("NO".equals(val)) termVector = Field.TermVector.NO;
                    else if ("YES".equals(val)) termVector = Field.TermVector.YES;
                    else if ("WITH_OFFSETS".equals(val)) termVector = Field.TermVector.WITH_OFFSETS;
                    else if ("WITH_POSITIONS".equals(val)) termVector = Field.TermVector.WITH_POSITIONS;
                    else if ("WITH_POSITIONS_OFFSETS".equals(val)) termVector = Field.TermVector.WITH_POSITIONS_OFFSETS;
                if (aName=="boost")
                    try {
                        boost = Float.parseFloat(val);
                    } catch (NumberFormatException e) {
                        boost = Float.parseFloat("1");
                    }
            }
        }
        elementBuffer.setLength(0);
    }
    
    public void characters(char[] text, int start, int length)
    throws SAXException {
        elementBuffer.append(text, start, length);
    }
    
    public void endElement(String namespaceURI, String simpleName,
            String qualifiedName)  throws SAXException {
        String ebs = elementBuffer.toString().trim();
        if ("IndexField".equals(simpleName)) {
			if (dsId != null) {
				try {
					ebs = owner.getDatastreamText(pid, repositoryName, dsId)
							.toString();
				} catch (GenericSearchException e) {
					throw new SAXException(e);
				}
			} else if (dsMimetypes != null) {
				try {
					ebs = owner.getFirstDatastreamText(pid, repositoryName,
							dsMimetypes).toString();
				} catch (GenericSearchException e) {
					throw new SAXException(e);
				}
			} else if (bDefPid != null) {
				try {
					ebs = owner.getDisseminationText(pid, repositoryName,
							bDefPid, methodName, parameters, asOfDateTime)
							.toString();
				} catch (GenericSearchException e) {
					throw new SAXException(e);
				}
			}
			if (ebs.length() > 0) {
				if (logger.isDebugEnabled())
					logger.debug(fieldName + "=" + ebs);
				Field f = new Field(fieldName, ebs, store, index, termVector);
				if (boost > Float.MIN_VALUE)
					f.setBoost(boost);
				indexDocument.add(f);
			}
		}
    }
    
    protected Document getIndexDocument() {
        return indexDocument;
    }
    
    protected String getPid() {
        return pid;
    }

}

