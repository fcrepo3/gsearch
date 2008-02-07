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
package dk.defxws.fedoragsearch.server.errors;

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
        super(message);
    }
    
    public GenericSearchException(String message, Throwable cause) {
        super(message, cause);
    }
    
}