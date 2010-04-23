//$Id:  $
package gsearch.test.fgs23;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
/**
 * Test of fgs23 config
 * 
 * assuming 
 * - all Fedora demo objects are in the repository referenced in
 *   configFgs23/repository/Fgs23Repos/repository.properties
 * 
 * the test suite will
 * - set configFgs23 as current config,
 * - run common operations as a REST client
 * - run common operations as a SOAP client. 
 */

@RunWith(Suite.class)
@Suite.SuiteClasses( {
	gsearch.test.fgs23.TestConfigFgs23.class,
	gsearch.test.common.TestREST.class,
	gsearch.test.common.TestSOAP.class
	} )
public class AllTests {

    // Supports legacy tests runners
    public static junit.framework.Test suite() throws Exception {
        junit.framework.TestSuite suite =
                new junit.framework.TestSuite(AllTests.class.getName());
        suite.addTest(gsearch.test.fgs23.TestConfigFgs23.suite());
        suite.addTest(gsearch.test.common.TestREST.suite());
        suite.addTest(gsearch.test.common.TestSOAP.suite());
        return suite;
    }
}
