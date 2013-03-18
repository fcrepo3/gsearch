//$Id:  $
package gsearch.test.fgs23;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
/**
 * Test of fgs23 config
 * 
 * assuming 
 * - all Fedora demo objects are in the repository referenced in
 *   configTestOnLuceneFgs23/repository/FgsRepos/repository.properties
 * 
 * the test suite will
 * - set configFgs23 as current config,
 * - run tests concerning GSearch 2.3. 
 */

@RunWith(Suite.class)
@Suite.SuiteClasses( {
	gsearch.test.fgs23.TestConfigFgs23.class
	} )
public class AllTests {

    // Supports legacy tests runners
    public static junit.framework.Test suite() throws Exception {
        junit.framework.TestSuite suite =
                new junit.framework.TestSuite(AllTests.class.getName());
        suite.addTest(gsearch.test.fgs23.TestConfigFgs23.suite());
        return suite;
    }
}
