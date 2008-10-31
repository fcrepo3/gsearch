//$Id:  $
package gsearch.test.testsonlucene;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
/**
 * Tests on lucene plugin
 * 
 * assuming 
 * - all Fedora demo objects are in the repository referenced in
 *   configTestOnLucene/repository/DemoAtDtu/repository.properties
 * 
 * the test suite will
 * - set configTestOnLucene as current config. 
 */

@RunWith(Suite.class)
@Suite.SuiteClasses( {
	gsearch.test.testsonlucene.TestConfigTestOnLucene.class,
	gsearch.test.testsonlucene.TestSortFields.class,
	gsearch.test.testsonlucene.TestUpdates.class
	} )
public class AllTests {

    // Supports legacy tests runners
    public static junit.framework.Test suite() throws Exception {
        junit.framework.TestSuite suite =
                new junit.framework.TestSuite(AllTests.class.getName());
        suite.addTest(gsearch.test.testsonlucene.TestConfigTestOnLucene.suite());
        suite.addTest(gsearch.test.testsonlucene.TestSortFields.suite());
        suite.addTest(gsearch.test.testsonlucene.TestUpdates.suite());
        return suite;
    }
}
