//$Id:  $
package gsearch.test.searchresultfiltering;

import junit.framework.Test;

/**
 * Test of search result filtering, see TestSearchResultFiltering.java 
 */

public class TestSearchResultFilteringSetup
        extends junit.extensions.TestSetup {

    public TestSearchResultFilteringSetup(Test test) {
        super(test);
    }

    @Override
    public void setUp() throws Exception {
        System.setProperty("fedoragsearch.fgsUserName", "fgsTester");
        System.setProperty("fedoragsearch.fgsPassword", "fgsTesterPassword");
        System.setProperty("fedoragsearch.clientType", "REST");
        System.out.println("setUp configDemoSearchResultFiltering");
    }

    @Override
    public void tearDown() throws Exception {
        System.out.println("tearDown configDemoSearchResultFiltering");
    }
}
