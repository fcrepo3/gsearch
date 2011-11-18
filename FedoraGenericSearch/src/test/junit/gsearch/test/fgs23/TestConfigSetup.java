//$Id:  $
package gsearch.test.fgs23;

import junit.framework.Test;

/**
 * Test of lucene plugin
 * 
 * the test suite will
 * - set configFgs23 as current config. 
 */
public class TestConfigSetup
        extends junit.extensions.TestSetup {

    public TestConfigSetup(Test test) {
        super(test);
    }

    @Override
    public void setUp() throws Exception {
        System.out.println("setUp configTestOnLuceneFgs23");
        System.setProperty("fedoragsearch.fgsUserName", "fgsTester");
        System.setProperty("fedoragsearch.fgsPassword", "fgsTesterPassword");
    }

    @Override
    public void tearDown() throws Exception {
        System.out.println("tearDown configTestOnLuceneFgs23");
    }
}
