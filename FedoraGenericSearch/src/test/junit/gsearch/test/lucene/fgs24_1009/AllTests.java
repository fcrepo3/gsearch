//$Id:  $
package gsearch.test.lucene.fgs24_1009;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
/**
 * Test of fgs24 config
 * 
 * assuming 
 * - all Fedora demo objects are in the repository referenced in
 *   configTestOnLuceneFgs24_1009/repository/FgsRepos/repository.properties
 * 
 * the test suite will
 * - set configTestOnLuceneFgs24_1009 as current config,
 * - run tests concerning GSearch 2.4. 
 */

@RunWith(Suite.class)
@Suite.SuiteClasses( {
	gsearch.test.lucene.fgs24_1009.TestConfigOnLuceneFgs24_1009.class
	} )
public class AllTests {

    // Supports legacy tests runners
    public static junit.framework.Test suite() throws Exception {
        junit.framework.TestSuite suite =
                new junit.framework.TestSuite(AllTests.class.getName());
        suite.addTest(gsearch.test.lucene.fgs24_1009.TestConfigOnLuceneFgs24_1009.suite());
        return suite;
    }
}
