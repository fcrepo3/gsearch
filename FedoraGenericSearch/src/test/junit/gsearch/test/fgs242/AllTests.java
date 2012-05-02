//$Id:  $
package gsearch.test.fgs242;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
/**
 * Test of GSearch 2.4.2
 * 
 * assuming 
 * - all Fedora demo objects are in the repository referenced in
 *   configTestOnLucene/repository/FgsRepos/repository.properties
 * 
 * the test suite will
 * - set configTestOnLucene as current config,
 * - run tests concerning GSearch 2.4.2 
 */

@RunWith(Suite.class)
@Suite.SuiteClasses( {
	gsearch.test.fgs242.TestFgs242.class
	} )
public class AllTests {

    // Supports legacy tests runners
    public static junit.framework.Test suite() throws Exception {
        junit.framework.TestSuite suite =
                new junit.framework.TestSuite(AllTests.class.getName());
        suite.addTest(gsearch.test.fgs242.TestFgs242.suite());
        return suite;
    }
}
