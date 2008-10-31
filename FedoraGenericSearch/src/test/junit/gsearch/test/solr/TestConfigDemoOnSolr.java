//$Id:  $
package gsearch.test.solr;

import junit.framework.TestSuite;

import org.junit.Test;

import gsearch.test.FgsTestCase;

/**
 * Test of solr plugin
 * 
 * the test suite will
 * - set configDemoOnSolr as current config.
 */
public class TestConfigDemoOnSolr
        extends FgsTestCase {

    // Supports legacy test runners
    public static junit.framework.Test suite() {
        TestSuite suite = new TestSuite("configDemoOnSolr TestSuite");
        suite.addTestSuite(TestConfigDemoOnSolr.class);
        return new TestConfigSetup(suite);
    }

    @Test
    public void testSetConfigDemoOnSolr() throws Exception {
        System.setProperty("fedoragsearch.clientType", "REST");
  	    StringBuffer result = doOp("?operation=configure&configName=configDemoOnSolr&restXslt=copyXml");
  	    assertXpathNotExists("/resultPage/error", result.toString());
    }
}
