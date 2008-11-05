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
public class TestConfigDemoOnZebra
        extends FgsTestCase {

    // Supports legacy test runners
    public static junit.framework.Test suite() {
        TestSuite suite = new TestSuite("configDemoOnZebra TestSuite");
        suite.addTestSuite(TestConfigDemoOnZebra.class);
        return new TestConfigSetup(suite);
    }

    @Test
    public void testSetConfigDemoOnZebra() throws Exception {
        System.setProperty("fedoragsearch.clientType", "REST");
  	    StringBuffer result = doOp("?operation=configure&configName=configDemoOnZebra&restXslt=copyXml");
  	    assertXpathNotExists("/resultPage/error", result.toString());
    }

    @Test
    public void testCreateEmpty() throws Exception {
  	    StringBuffer result = doOp("?operation=updateIndex&action=createEmpty&restXslt=copyXml");
  	    assertXpathEvaluatesTo("0", "/resultPage/updateIndex/@docCount", result.toString());
    }

    @Test
    public void testUpdateIndexFromFoxmlFiles() throws Exception {
  	    StringBuffer result = doOp("?operation=updateIndex&action=fromFoxmlFiles&restXslt=copyXml");
  	    assertXpathEvaluatesTo("24", "/resultPage/updateIndex/@docCount", result.toString());
    }

    @Test
    public void testGetRepositoryInfo() throws Exception {
  	    StringBuffer result = doOp("?operation=getRepositoryInfo&restXslt=copyXml");
  	    assertXpathEvaluatesTo("DemoAtDtu", "/resultPage/repositoryInfo/RepositoryShortName", result.toString());
    }

    @Test
    public void testGetIndexInfo() throws Exception {
  	    StringBuffer result = doOp("?operation=getIndexInfo&restXslt=copyXml");
  	    assertXpathExists("/resultPage/indexInfo/IndexShortName", result.toString());
    }

    @Test
    public void testGfindObjects() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=image&restXslt=copyXml");
  	    assertXpathEvaluatesTo("8", "/resultPage/gfindObjects/@hitTotal", result.toString());
    }

    @Test
    public void testBrowseIndex() throws Exception {
  	    StringBuffer result = doOp("?operation=browseIndex&startTerm=0&fieldName=fgs.PID&restXslt=copyXml");
  	    assertXpathExists("/resultPage/browseIndex/@termTotal", result.toString());
    }
}
