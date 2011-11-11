//$Id:  $
package gsearch.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses( {
	gsearch.test.lucene.AllTests.class,
	gsearch.test.solr.AllTests.class,
	gsearch.test.testsonlucene.AllTests.class,
	gsearch.test.fgs23.AllTests.class,
	gsearch.test.lucene.fgs24_1010.AllTests.class,
	gsearch.test.solr.fgs24_1010.AllTests.class
	})
public class AllLuceneAndSolrTests {

    // Supports legacy tests runners
    public static junit.framework.Test suite() throws Exception {

        junit.framework.TestSuite suite =
                new junit.framework.TestSuite(AllLuceneAndSolrTests.class
                        .getName());

        suite.addTest(gsearch.test.lucene.AllTests.suite());
        suite.addTest(gsearch.test.solr.AllTests.suite());
        suite.addTest(gsearch.test.testsonlucene.AllTests.suite());
        suite.addTest(gsearch.test.fgs23.AllTests.suite());
        suite.addTest(gsearch.test.lucene.fgs24_1010.AllTests.suite());
        suite.addTest(gsearch.test.solr.fgs24_1010.AllTests.suite());

        return suite;
    }
}
