//$Id:  $
package gsearch.test.common;

import junit.framework.Test;

/**
 * Test of common operations from a REST client
 * 
 * assuming 
 * - all Fedora demo objects are in the repository referenced in the config
 * 
 * sets up the REST client type
 * 
 * the tests are
 * - the common.Operations tests
 */
public class TestRESTSetup
        extends junit.extensions.TestSetup {

    public TestRESTSetup(Test test) {
        super(test);
    }

    @Override
    public void setUp() throws Exception {
        System.out.println("setUp common REST");
        System.setProperty("fedoragsearch.clientType", "REST");
        System.setProperty("fedoragsearch.fgsUserName", "fgsTester");
        System.setProperty("fedoragsearch.fgsPassword", "fgsTesterPassword");
    }

    @Override
    public void tearDown() throws Exception {
        System.out.println("tearDown common REST");
    }
}
