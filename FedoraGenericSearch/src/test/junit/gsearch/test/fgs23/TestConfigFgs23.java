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
    public void testSetOperationsImplToSolr() throws Exception {
        StringBuffer result = doOp("?operation=configure&configName=configTestOnLuceneFgs23&propertyName=FgsIndex/fgsindex.operationsImpl&propertyValue=dk.defxws.fgssolr.OperationsImpl&restXslt=copyXml");
  	    assertXpathNotExists("/resultPage/error", result.toString());
    }

    @Test
    public void testSetUriResolverForSolr() throws Exception {
        StringBuffer result = doOp("?operation=configure&configName=configTestOnLuceneFgs23&propertyName=FgsIndex/fgsindex.uriResolver&propertyValue=dk.defxws.fedoragsearch.server.URIResolverImpl&restXslt=copyXml");
  	    assertXpathNotExists("/resultPage/error", result.toString());
    }

    @Test
    public void testSetOperationsImplToLucene() throws Exception {
        StringBuffer result = doOp("?operation=configure&configName=configTestOnLuceneFgs23&propertyName=FgsIndex/fgsindex.operationsImpl&propertyValue=dk.defxws.fgslucene.OperationsImpl&restXslt=copyXml");
  	    assertXpathNotExists("/resultPage/error", result.toString());
    }

    @Test
    public void testSetUriResolverForLucene() throws Exception {
        StringBuffer result = doOp("?operation=configure&configName=configTestOnLuceneFgs23&propertyName=FgsIndex/fgsindex.uriResolver&propertyValue=dk.defxws.fedoragsearch.server.URIResolverImpl&restXslt=copyXml");
  	    assertXpathNotExists("/resultPage/error", result.toString());
    }

    @Test
    public void testSetOperationsImplToNotvalid() throws Exception {
        StringBuffer result = doOp("?operation=configure&configName=configTestOnLuceneFgs23&propertyName=FgsIndex/fgsindex.operationsImpl&propertyValue=dk.defxws.fgsnotvalid.OperationsImpl&restXslt=copyXml");
  	    assertXpathExists("/resultPage/error", result.toString());
    }

    @Test
    public void testSetUriResolverToNotvalid() throws Exception {
        StringBuffer result = doOp("?operation=configure&configName=configTestOnLuceneFgs23&propertyName=FgsIndex/fgsindex.uriResolver&propertyValue=dk.defxws.fedoragsearch.server.URIResolverImplNotvalid&restXslt=copyXml");
  	    assertXpathExists("/resultPage/error", result.toString());
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

    @Test
    public void testSetAllowLeadingWildcardNotvalid() throws Exception {
        StringBuffer result = doOp("?operation=configure&configName=configTestOnLuceneFgs23&propertyName=FgsIndex/fgsindex.allowLeadingWildcard&propertyValue=notvalid&restXslt=copyXml");
  	    assertXpathExists("/resultPage/error", result.toString());
    }

    @Test
    public void testSetXsltProcessorToSaxon() throws Exception {
        StringBuffer result = doOp("?operation=configure&configName=configTestOnLuceneFgs23&propertyName=fedoragsearch.xsltProcessor&propertyValue=saxon&restXslt=copyXml");
  	    assertXpathNotExists("/resultPage/error", result.toString());
    }

    @Test
    public void testSetXsltProcessorToXalan() throws Exception {
        StringBuffer result = doOp("?operation=configure&configName=configTestOnLuceneFgs23&propertyName=fedoragsearch.xsltProcessor&propertyValue=xalan&restXslt=copyXml");
  	    assertXpathNotExists("/resultPage/error", result.toString());
    }

    @Test
    public void testSetXsltProcessorToNotvalid() throws Exception {
        StringBuffer result = doOp("?operation=configure&configName=configTestOnLuceneFgs23&propertyName=fedoragsearch.xsltProcessor&propertyValue=notvalid&restXslt=copyXml");
  	    assertXpathExists("/resultPage/error", result.toString());
    }
}
