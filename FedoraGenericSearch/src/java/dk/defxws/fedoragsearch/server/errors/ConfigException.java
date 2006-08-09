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
 * an exception for configuration errors
 * 
 * @author  gsp@dtv.dk
 * @version 
 */
public class ConfigException extends GenericSearchException {
    
    /**
     *
     * @param message An informative message explaining what happened and
     *                (possibly) how to fix it.
     */
    public ConfigException(String message) {
        super(message);
    }
    
    public ConfigException(String message, Throwable cause) {
        super(message, cause);
    }
    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        
    }
    
}
