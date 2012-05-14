//$Id:  $
package gsearch.test.fgs242_1083;

import junit.framework.Test;

/**
 * Test of GSearch 2.4.2
 * 
 * assuming 
 * - all Fedora demo objects are in the repository referenced in
 *   configTestOnLucene/repository/FgsRepos/repository.properties
 * 
 * the test suite will
 * - set configTestOnLucene as current config,
 * - run tests concerning GSearch 2.4.2 
 */
public class TestConfigSetup
        extends junit.extensions.TestSetup {

    public TestConfigSetup(Test test) {
        super(test);
    }

    @Override
    public void setUp() throws Exception {
        System.out.println("setUp TestFgs242_1083");
        System.setProperty("fedoragsearch.fgsUserName", "fgsTester");
        System.setProperty("fedoragsearch.fgsPassword", "fgsTesterPassword");
    }

    @Override
    public void tearDown() throws Exception {
        System.out.println("tearDown TestFgs242_1083");
    }
}
