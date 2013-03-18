//$Id:  $
package gsearch.test.searchresultfiltering;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test of search result filtering, see TestSearchResultFiltering.java 
 */

@RunWith(Suite.class)
@Suite.SuiteClasses( {gsearch.test.searchresultfiltering.TestSearchResultFiltering.class} )
public class AllTests {

    // Supports legacy tests runners
    public static junit.framework.Test suite() throws Exception {
        junit.framework.TestSuite suite =
                new junit.framework.TestSuite(AllTests.class.getName());
        suite.addTest(gsearch.test.searchresultfiltering.TestSearchResultFiltering.suite());
        return suite;
    }
}
