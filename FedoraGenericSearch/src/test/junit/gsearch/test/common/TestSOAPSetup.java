//$Id:  $
package gsearch.test.common;

import junit.framework.Test;

/**
 * Test of common operations from a SOAP client
 * 
 * assuming 
 * - all Fedora demo objects are in the repository referenced in the config
 * 
 * sets up the SOAP client type
 * 
 * the tests are
 * - the common.Operations tests
 */
public class TestSOAPSetup
        extends junit.extensions.TestSetup {

    public TestSOAPSetup(Test test) {
        super(test);
    }

    @Override
    public void setUp() throws Exception {
        System.out.println("setUp common SOAP");
        System.setProperty("fedoragsearch.clientType", "SOAP");
        System.setProperty("fedoragsearch.fgsUserName", "fgsTester");
        System.setProperty("fedoragsearch.fgsPassword", "fgsTesterPassword");
    }

    @Override
    public void tearDown() throws Exception {
        System.out.println("tearDown common SOAP");
    }
}
