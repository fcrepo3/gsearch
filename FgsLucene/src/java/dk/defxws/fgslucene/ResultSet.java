//$Id$
/*
 * <p><b>License and Copyright: </b>The contents of this file is subject to the
 * same open source license as the Fedora Repository System at www.fedora-commons.org
 * Copyright &copy; 2006, 2007, 2008, 2009, 2010, 2011 by The Technical University of Denmark.
 * All rights reserved.</p>
 */
package dk.defxws.fgslucene;

import dk.defxws.fedoragsearch.server.errors.GenericSearchException;

import org.apache.log4j.Logger;

/**
 * operates on the result set from an operation on the Lucene index 
 * 
 * @author  gsp@dtv.dk
 * @version 
 */
public class ResultSet {
    
    private static final Logger logger =
        Logger.getLogger(ResultSet.class);
    
    private StringBuffer resultXml;
    
    public ResultSet() {
    }
    
    protected ResultSet(StringBuffer in)
    throws GenericSearchException {
        resultXml = in;
    }
    
    /**
     * 
     */
    protected void close() throws GenericSearchException {
    }
    
    protected StringBuffer getResultXml() {
        return this.resultXml;
    }
    
}
