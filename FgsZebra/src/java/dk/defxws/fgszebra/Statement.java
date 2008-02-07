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

import java.io.InputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.MalformedURLException;

import org.apache.log4j.Logger;

import dk.defxws.fedoragsearch.server.errors.GenericSearchException;

/**
 * queries the Zebra index 
 * 
 * @author  gsp@dtv.dk
 * @version 
 */
public class Statement {

    private static final Logger logger =
            Logger.getLogger(Statement.class);

	private static String queryParams =
		"?operation=searchRetrieve&version=1.1";
	private static String scanParams =
		"?operation=scan&version=1.1";
	private Object content;

	/**
	 * 
	 *
	 * @param query a cql statement
	 * @return a <code>ResultSet</code> object 
	 * @exception GenericSearchException
	 */
	protected ResultSet executeQuery(
          String query, 
          int startRecord, 
          int maxResults,
          String indexBase, 
          String indexName)
		throws GenericSearchException {
		ResultSet rs = null;
		URL url = null;
		try {
			url =
				new URL(
						indexBase
						+ queryParams
						+ "&startRecord="
						+ startRecord
						+ "&maximumRecords="
						+ maxResults
						+ "&query="
						+ URLEncoder.encode(query, "UTF-8"));
		} catch (MalformedURLException e) {
			throw new GenericSearchException(e.toString());
		} catch (UnsupportedEncodingException e) {
			throw new GenericSearchException(e.toString());
		}
		if (logger.isDebugEnabled())
			logger.debug("url="+url);
		URLConnection conn = null;
		try {
			conn = url.openConnection();
			conn.connect();
		} catch (IOException e) {
			throw new GenericSearchException(e.toString());
		}
		content = null;
		try {
			content = conn.getContent();
		} catch (IOException e) {
			throw new GenericSearchException(e.toString());
		}
		rs = new ResultSet((InputStream) content);
		return rs;
	}

	/**
	 * 
	 *
	 * @param query a cql statement
	 * @return a <code>ResultSet</code> object 
	 * @exception GenericSearchException
	 */
	protected ResultSet executeScan(
          String startTerm, 
          int maxResults, 
          String fieldName,
          String indexBase, 
          String indexName)
		throws GenericSearchException {
		ResultSet rs = null;
		URL url = null;
		String st = startTerm;
		if (st==null || st.trim().equals("")) st = "!";
		try {
			url =
				new URL(
						indexBase
						+ scanParams
						+ "&maximumTerms="
						+ maxResults
						+ "&scanClause="
						+ URLEncoder.encode(fieldName+"="+st.trim(), "UTF-8"));
		} catch (MalformedURLException e) {
			throw new GenericSearchException(e.toString());
		} catch (UnsupportedEncodingException e) {
			throw new GenericSearchException(e.toString());
		}
		if (logger.isDebugEnabled())
			logger.debug("url="+url);
		URLConnection conn = null;
		try {
			conn = url.openConnection();
			conn.connect();
		} catch (IOException e) {
			throw new GenericSearchException(e.toString());
		}
		content = null;
		try {
			content = conn.getContent();
		} catch (IOException e) {
			throw new GenericSearchException(e.toString());
		}
		rs = new ResultSet((InputStream) content);
		return rs;
	}
    
	/**
	 */
	void close() throws GenericSearchException {
	}
}
