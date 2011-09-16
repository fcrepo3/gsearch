//$Id:  $
package gsearch.test.solr;

import junit.framework.Test; 

/**
 * Test of solr plugin
 * 
 * the test suite will
 * - set configDemoOnSolr as current config. 
 */
public class TestConfigSetup
        extends junit.extensions.TestSetup {

    public TestConfigSetup(Test test) {
        super(test);
    }

    @Override
    public void setUp() throws Exception {
        System.out.println("setUp configDemoOnSolr");
        System.setProperty("fedoragsearch.fgsUserName", "fgsTester");
        System.setProperty("fedoragsearch.fgsPassword", "fgsTesterPassword");
    }

    @Override
    public void tearDown() throws Exception {
        System.out.println("tearDown configDemoOnSolr");
    }
}
