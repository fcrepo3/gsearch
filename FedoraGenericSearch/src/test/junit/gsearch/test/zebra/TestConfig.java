//$Id:  $
package gsearch.test.zebra;

import junit.framework.TestSuite;

import org.junit.Test;

import gsearch.test.FgsTestCase;

/**
 * Test of zebra plugin
 * 
 * the test suite will
 * - set configDemoOnZebra as current config. 
 */
public class TestConfig
        extends FgsTestCase {

    // Supports legacy test runners
    public static junit.framework.Test suite() {
        TestSuite suite = new TestSuite("configDemoOnZebra TestSuite");
        suite.addTestSuite(TestConfig.class);
        return new TestConfigSetup(suite);
    }

    @Test
    public void testSetConfig() throws Exception {
        System.setProperty("fedoragsearch.clientType", "REST");
  	    StringBuffer result = doOp("?operation=configure&configName=configDemoOnZebra&restXslt=copyXml");
  	    assertXpathNotExists("/resultPage/error", result.toString());
    }
}
