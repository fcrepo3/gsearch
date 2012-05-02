//$Id:  $
package gsearch.test.fgs242;

import junit.framework.TestSuite;

import org.junit.Test;

import gsearch.test.FgsTestCase;

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
public class TestFgs242
        extends FgsTestCase {

    // Supports legacy test runners
    public static junit.framework.Test suite() {
        TestSuite suite = new TestSuite("TestFgs242 TestSuite");
        suite.addTestSuite(TestFgs242.class);
        return new TestConfigSetup(suite);
    }

    @Test
    public void testSetConfig() throws Exception {
        System.setProperty("fedoragsearch.clientType", "REST");
  	    StringBuffer result = doOp("?operation=configure&configName=configTestOnLucene&restXslt=copyXml");
  	    assertXpathNotExists("/resultPage/error", result.toString());
    }
    
    // assuming fgsindex.lowercaseExpandedTerms = true

    @Test
    public void testSetLowercaseExpandedTermsBefore() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=REPOSITORYNAME:FgsRepo*&restXslt=copyXml");
  	    assertXpathEvaluatesTo("0", "/resultPage/gfindObjects/@hitTotal", result.toString());
    }

    @Test
    public void testSetLowercaseExpandedTerms() throws Exception {
        StringBuffer result = doOp("?operation=configure&configName=configTestOnLucene&propertyName=FgsIndex/fgsindex.lowercaseExpandedTerms&propertyValue=false&restXslt=copyXml");
  	    assertXpathNotExists("/resultPage/error", result.toString());
    }

    @Test
    public void testSetLowercaseExpandedTermsAfter() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=REPOSITORYNAME:FgsRepo*&restXslt=copyXml");
  	    assertXpathEvaluatesTo("20", "/resultPage/gfindObjects/@hitTotal", result.toString());
    }

    @Test
    public void testSetLowercaseExpandedTermsNotvalid() throws Exception {
        StringBuffer result = doOp("?operation=configure&configName=configTestOnLucene&propertyName=FgsIndex/fgsindex.lowercaseExpandedTerms&propertyValue=notvalid&restXslt=copyXml");
  	    assertXpathExists("/resultPage/error", result.toString());
    }
}
