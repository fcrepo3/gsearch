//$Id:  $
package gsearch.test.solrremote;

import junit.framework.TestSuite;

import org.junit.Test;

import gsearch.test.FgsTestCase;

/**
 * Test of solrremote plugin
 * 
 * the test suite will
 * - set configDemoOnSolrRemote as current config.
 * - test updateIndex
 */
public class TestConfigDemoOnSolrRemote
        extends FgsTestCase {

    // Supports legacy test runners
    public static junit.framework.Test suite() {
        TestSuite suite = new TestSuite("configDemoOnSolrRemote TestSuite");
        suite.addTestSuite(TestConfigDemoOnSolrRemote.class);
        return new TestConfigSetup(suite);
    }

    @Test
    public void testSetConfigDemoOnSolrRemote() throws Exception {
        System.setProperty("fedoragsearch.clientType", "REST");
  	    StringBuffer result = doOp("?operation=configure&configName=configDemoOnSolrRemote&restXslt=copyXml");
  	    assertXpathNotExists("/resultPage/error", result.toString());
    }

    @Test
    public void testUpdateIndexDocCount() throws Exception {
  	    StringBuffer result = doOp("?operation=updateIndex&restXslt=copyXml");
  	    assertXpathEvaluatesTo("20", "/resultPage/updateIndex/@docCount", result.toString());
    }

    @Test
    public void testUpdateIndexFromFoxmlFiles() throws Exception {
  	    StringBuffer result = doOp("?operation=updateIndex&action=fromFoxmlFiles&restXslt=copyXml");
  	  assertXpathExists("/resultPage/updateIndex/@docCount", result.toString());
    }

    @Test
    public void testUpdateIndexDocCount2() throws Exception {
  	    StringBuffer result = doOp("?operation=updateIndex&restXslt=copyXml");
  	    assertXpathEvaluatesTo("20", "/resultPage/updateIndex/@docCount", result.toString());
    }

    @Test
    public void testUpdateIndexDeletePid() throws Exception {
  	    StringBuffer result = doOp("?operation=updateIndex&action=deletePid&value=demo:14&restXslt=copyXml");
  	    assertXpathEvaluatesTo("1", "/resultPage/updateIndex/@deleteTotal", result.toString());
    }

    @Test
    public void testUpdateIndexDocCount3() throws Exception {
	    delay(5000);
  	    doIndexOp("http://localhost:8983/solr/collection1/update?commit=true&expungeDeletes=true");
	    delay(5000);
  	    StringBuffer result = doOp("?operation=updateIndex&restXslt=copyXml");
  	    assertXpathEvaluatesTo("19", "/resultPage/updateIndex/@docCount", result.toString());
    }

    @Test
    public void testUpdateIndexInsertPid() throws Exception {
	    delay(5000);
  	    doIndexOp("http://localhost:8983/solr/collection1/update?commit=true&expungeDeletes=true");
	    delay(5000);
  	    StringBuffer result = doOp("?operation=updateIndex&action=fromPid&value=demo:14&restXslt=copyXml");
  	    assertXpathEvaluatesTo("1", "/resultPage/updateIndex/@insertTotal", result.toString());
    }

    @Test
    public void testUpdateIndexDocCount4() throws Exception {
	    delay(5000);
  	    doIndexOp("http://localhost:8983/solr/collection1/update?commit=true&expungeDeletes=true");
	    delay(5000);
  	    StringBuffer result = doOp("?operation=updateIndex&restXslt=copyXml");
  	    assertXpathEvaluatesTo("20", "/resultPage/updateIndex/@docCount", result.toString());
    }
}
