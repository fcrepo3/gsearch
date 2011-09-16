//$Id$
/*
 * <p><b>License and Copyright: </b>The contents of this file is subject to the
 * same open source license as the Fedora Repository System at www.fedora-commons.org
 * Copyright &copy; 2006, 2007, 2008, 2009, 2010, 2011 by The Technical University of Denmark.
 * All rights reserved.</p>
 */
package dk.defxws.fedoragsearch.server.errors;

import java.util.Date;

/**
 * the most general exception for the search service
 * 
 * @author  gsp@dtv.dk
 * @version 
 */
public class GenericSearchException extends java.rmi.RemoteException {

	private static final long serialVersionUID = 1L;

    /**
     *
     * @param message An informative message explaining what happened and
     *                (possibly) how to fix it.
     */
    public GenericSearchException(String message) {
        super(new Date()+" "+message);
    }
    
    public GenericSearchException(String message, Throwable cause) {
        super(new Date()+" "+message, cause);
    }
    
}