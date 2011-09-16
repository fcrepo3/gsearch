//$Id:  $
package gsearch.test.zebra;

import junit.framework.Test;

/**
 * Test of zebra plugin
 * 
 * the test suite will
 * - set configDemoOnZebra as current config. 
 */
public class TestConfigSetup
        extends junit.extensions.TestSetup {

    public TestConfigSetup(Test test) {
        super(test);
    }

    @Override
    public void setUp() throws Exception {
        System.out.println("setUp configDemoOnZebra");
        System.setProperty("fedoragsearch.fgsUserName", "fgsTester");
        System.setProperty("fedoragsearch.fgsPassword", "fgsTesterPassword");
    }

    @Override
    public void tearDown() throws Exception {
        System.out.println("tearDown configDemoOnZebra");
    }
}
