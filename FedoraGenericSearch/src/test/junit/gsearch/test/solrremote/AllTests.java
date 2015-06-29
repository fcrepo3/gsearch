//$Id:  $
package gsearch.test.solrremote;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
/**
 * Test of solrremote plugin
 * 
 * assuming 
 * - all Fedora demo objects are in the repository referenced in
 *   configDemoOnSolrRemote/repository/DemoAtDtu/repository.properties
 * - the solr server is started:
 *     cd $FEDORA_HOME/gsearch/DemoOnSolr/example
 *     java -jar start.jar
 * 
 * the test suite will
 * - set configDemoOnSolrRemote as current config,
 * - run common operations as a REST client
 * - run common operations as a SOAP client. 
 */

@RunWith(Suite.class)
@Suite.SuiteClasses( {
	gsearch.test.solrremote.TestConfigDemoOnSolrRemote.class,
	gsearch.test.common.TestREST.class,
	gsearch.test.common.TestSOAP.class
	} )
public class AllTests {

    // Supports legacy tests runners
    public static junit.framework.Test suite() throws Exception {
        junit.framework.TestSuite suite =
                new junit.framework.TestSuite(AllTests.class.getName());
        suite.addTest(gsearch.test.solrremote.TestConfigDemoOnSolrRemote.suite());
//        suite.addTest(gsearch.test.common.TestREST.suite());
//        suite.addTest(gsearch.test.common.TestSOAP.suite()); 
        return suite;
    }
}
