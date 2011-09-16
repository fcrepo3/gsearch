//$Id:  $
package gsearch.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses( {
	gsearch.test.lucene.AllTests.class,
	gsearch.test.solr.AllTests.class,
	gsearch.test.zebra.AllTests.class,
	gsearch.test.testsonlucene.AllTests.class
	})
public class AllTests {

    // Supports legacy tests runners
    public static junit.framework.Test suite() throws Exception {

        junit.framework.TestSuite suite =
                new junit.framework.TestSuite(AllTests.class
                        .getName());

        suite.addTest(gsearch.test.lucene.AllTests.suite());
        suite.addTest(gsearch.test.solr.AllTests.suite());
        suite.addTest(gsearch.test.zebra.AllTests.suite());
        suite.addTest(gsearch.test.testsonlucene.AllTests.suite());
//        suite.addTest(gsearch.test.searchresultfiltering.AllTests.suite());

        return suite;
    }
}
