//$Id:  $
package gsearch.test.testsonlucene;

import junit.framework.Test;

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
public class TestConfigSetup
        extends junit.extensions.TestSetup {

    public TestConfigSetup(Test test) {
        super(test);
    }

    @Override
    public void setUp() throws Exception {
        System.out.println("setUp configTestOnLucene");
        System.setProperty("fedoragsearch.fgsUserName", "fedoraAdmin");
        System.setProperty("fedoragsearch.fgsPassword", "fedoraAdmin");
    }

    @Override
    public void tearDown() throws Exception {
//        System.out.println("tearDown configTestOnLucene");
    }
}
