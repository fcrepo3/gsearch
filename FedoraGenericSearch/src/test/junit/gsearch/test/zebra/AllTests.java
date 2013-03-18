//$Id:  $
package gsearch.test.zebra;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
/**
 * Test of zebra plugin
 * 
 * assuming 
 * - all Fedora demo objects are in the repository referenced in
 *   configDemoOnZebra/repository/FgsRepos/repository.properties
 * - the zebra server is configured and started, 
 *   see $FEDORA_HOME/tomcat/webapps/fedoragsearch/WEB-INF/classes/configDemoOnZebra/index/DemoOnZebra/zebraconfig/README
 * 
 * the test suite will
 * - set configDemoOnZebra as current config. 
 */

@RunWith(Suite.class)
@Suite.SuiteClasses( {
	gsearch.test.zebra.TestConfigDemoOnZebra.class
	} )
public class AllTests {

    // Supports legacy tests runners
    public static junit.framework.Test suite() throws Exception {
        junit.framework.TestSuite suite =
                new junit.framework.TestSuite(AllTests.class.getName());
        suite.addTest(gsearch.test.zebra.TestConfigDemoOnZebra.suite());
        return suite;
    }
}
