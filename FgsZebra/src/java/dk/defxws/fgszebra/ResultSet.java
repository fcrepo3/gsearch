// $Id$
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
package dk.defxws.fgszebra;

import dk.defxws.fedoragsearch.server.errors.GenericSearchException;

import java.io.InputStream;

import org.apache.log4j.Logger;

/**
 * operates on the result set from an operation on the Zebra index 
 * 
 * @author  gsp@dtv.dk
 * @version 
 */
public class ResultSet {
	
	private static final Logger logger =
		Logger.getLogger(ResultSet.class);
	
	private InputStream resultXml;
	
	public ResultSet() {
	}
	
	protected ResultSet(InputStream in)
	throws GenericSearchException {
		resultXml = in;
	}
	
	/**
	 * 
	 */
	protected void close() throws GenericSearchException {
	}
	
	protected InputStream getResultXml() {
		return this.resultXml;
	}
	
}
