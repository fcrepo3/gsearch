//$Id:  $
package gsearch.test.fgs23;

import junit.framework.TestSuite;

import org.junit.BeforeClass;
import org.junit.Test;

import gsearch.test.FgsTestCase;

/**
 * Test of lucene plugin
 * 
 * the test suite will
 * - set configTestOnLuceneFgs23 as current config. 
 */
public class TestConfigFgs23
        extends FgsTestCase {

    // Supports legacy test runners
    public static junit.framework.Test suite() {
        TestSuite suite = new TestSuite("configTestOnLuceneFgs23 TestSuite");
        suite.addTestSuite(TestConfigFgs23.class);
        return new TestConfigSetup(suite);
    }

    @Test
    public void testSetConfigFgs23() throws Exception {
        System.setProperty("fedoragsearch.clientType", "REST");
  	    StringBuffer result = doOp("?operation=configure&configName=configTestOnLuceneFgs23&restXslt=copyXml");
//        System.out.println("result="+result.toString());
  	    assertXpathNotExists("/resultPage/error", result.toString());
    }

    @Test
    public void testGfindObjectsWildcardBeforeAllow() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=?mage&restXslt=copyXml");
  	    assertXpathExists("/resultPage/error", result.toString());
    }

    @Test
    public void testSetAllowLeadingWildcard() throws Exception {
        StringBuffer result = doOp("?operation=configure&configName=configTestOnLuceneFgs23&propertyName=FgsIndex/fgsindex.allowLeadingWildcard&propertyValue=true&restXslt=copyXml");
  	    assertXpathNotExists("/resultPage/error", result.toString());
    }

    @Test
    public void testGfindObjectsWildcardAfterAllow() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=?mage&restXslt=copyXml");
  	    assertXpathExists("/resultPage/gfindObjects/@hitTotal", result.toString());
    }
}
