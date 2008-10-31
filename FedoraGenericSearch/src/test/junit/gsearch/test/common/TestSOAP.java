//$Id:  $
package gsearch.test.common;

import junit.framework.TestSuite;

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
public class TestSOAP
        extends gsearch.test.common.Operations {

    // Supports legacy test runners
    public static junit.framework.Test suite() {
        TestSuite suite = new TestSuite("Common SOAP TestSuite");
        suite.addTestSuite(TestSOAP.class);
        return new TestSOAPSetup(suite);
    }
}
