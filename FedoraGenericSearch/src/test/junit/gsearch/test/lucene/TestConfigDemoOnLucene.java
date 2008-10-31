//$Id:  $
package gsearch.test.lucene;

import junit.framework.TestSuite;

import org.junit.BeforeClass;
import org.junit.Test;

import gsearch.test.FgsTestCase;

/**
 * Test of lucene plugin
 * 
 * the test suite will
 * - set configDemoOnLucene as current config. 
 */
public class TestConfigDemoOnLucene
        extends FgsTestCase {

    // Supports legacy test runners
    public static junit.framework.Test suite() {
        TestSuite suite = new TestSuite("configDemoOnLucene TestSuite");
        suite.addTestSuite(TestConfigDemoOnLucene.class);
        return new TestConfigSetup(suite);
    }

    @Test
    public void testSetConfigDemoOnLucene() throws Exception {
        System.setProperty("fedoragsearch.clientType", "REST");
  	    StringBuffer result = doOp("?operation=configure&configName=configDemoOnLucene&restXslt=copyXml");
//        System.out.println("result="+result.toString());
  	    assertXpathNotExists("/resultPage/error", result.toString());
    }

    @Test
    public void testCreateEmpty() throws Exception {
  	    StringBuffer result = doOp("?operation=updateIndex&action=createEmpty&restXslt=copyXml");
  	    assertXpathEvaluatesTo("0", "/resultPage/updateIndex/@docCount", result.toString());
    }

//  @BeforeClass
//  public void setConfig() throws Exception {
//      System.setProperty("fedoragsearch.fgsUserName", "fedoraAdmin");
//      System.setProperty("fedoragsearch.fgsPassword", "fedoraAdmin");
//  }
}
