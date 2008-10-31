//$Id:  $
package gsearch.test.common;

import org.junit.Test;

import junit.framework.TestSuite;

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
public class TestREST
        extends gsearch.test.common.Operations {

    // Supports legacy test runners
    public static junit.framework.Test suite() {
        TestSuite suite = new TestSuite("Common REST TestSuite");
        suite.addTestSuite(TestREST.class);
        return new TestRESTSetup(suite);
    }

    @Test 
    public void testUnknownOperation() throws Exception {
  	    StringBuffer result = doOp("?operation=unknownOperation&restXslt=copyXml");
		assertTrue(result.indexOf("ERROR: operation unknownOperation is unknown")>-1);
    }
}
