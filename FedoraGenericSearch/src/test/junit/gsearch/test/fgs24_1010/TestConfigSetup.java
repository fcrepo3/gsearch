//$Id:  $
package gsearch.test.fgs24_1010;

import junit.extensions.TestSetup;
import junit.framework.Test;

/**
 * Test of lucene plugin
 * 
 * the test suite will
 * - set configTestFgs24_1010 as current config. 
 */
public class TestConfigSetup
        extends junit.extensions.TestSetup {

    public TestConfigSetup(Test test) {
        super(test);
    }

    @Override
    public void setUp() throws Exception {
        System.out.println("setUp configTestFgs24_1010");
        System.setProperty("fedoragsearch.fgsUserName", "fgsTester");
        System.setProperty("fedoragsearch.fgsPassword", "fgsTesterPassword");
    }

    @Override
    public void tearDown() throws Exception {
        System.out.println("tearDown configTestFgs24_1010");
    }
}
