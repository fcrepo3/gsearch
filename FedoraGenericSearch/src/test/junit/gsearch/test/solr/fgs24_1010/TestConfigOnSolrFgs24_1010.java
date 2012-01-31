//$Id:  $
package gsearch.test.solr.fgs24_1010;

import java.io.File;
import java.io.FileInputStream;

import junit.framework.TestSuite;

import org.junit.Test;

import org.fcrepo.client.FedoraClient;
import org.fcrepo.server.management.FedoraAPIM;
import gsearch.test.FgsTestCase;

/**
 * Test of fcrepo-1010 : Tika extraction
 * 
 * the test suite will perform tests on Solr
 */
public class TestConfigOnSolrFgs24_1010
        extends FgsTestCase {

    // Supports legacy test runners
    public static junit.framework.Test suite() {
        TestSuite suite = new TestSuite("configTestOnSolrFgs24_1010 TestSuite");
        suite.addTestSuite(TestConfigOnSolrFgs24_1010.class);
        return new TestConfigSetup(suite);
    }

    @Test
    public void testSetConfigTestOnSolrFgs24_1010() throws Exception {
        System.setProperty("fedoragsearch.clientType", "REST");
  	    StringBuffer result = doOp("?operation=configure&configName=configTestOnSolrFgs24_1010&restXslt=copyXml");
//        System.out.println("result="+result.toString());
  	    assertXpathNotExists("/resultPage/error", result.toString());
    }

    @Test
    public void testTikaExtractionSolrBefore() throws Exception {
	    doOp("?operation=updateIndex&action=optimize&restXslt=copyXml");
	    delay(8000);
  	    StringBuffer result = doOp("?operation=gfindObjects&query=ds.testMwordX:tika&restXslt=copyXml");
  	    assertXpathEvaluatesTo("0", "/resultPage/gfindObjects/@hitTotal", result.toString());
    }

    @Test
    public void testTikaExtractionSolrBefore2() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=dsSomeMd.WordCount:22&restXslt=copyXml");
  	    assertXpathEvaluatesTo("0", "/resultPage/gfindObjects/@hitTotal", result.toString());
    }

    @Test
    public void testTikaExtractionSolrBefore3() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=ds.testMpdf:tika&restXslt=copyXml");
  	    assertXpathEvaluatesTo("0", "/resultPage/gfindObjects/@hitTotal", result.toString());
    }

    @Test
    public void testTikaExtractionSolrIngest() throws Exception {
    	FedoraClient fedoraClient = new FedoraClient("http://localhost:8080/fedora", "fgsTester", "fgsTesterPassword");
    	FedoraAPIM apim = fedoraClient.getAPIM();
    	File testfile = new File("../FgsConfig/test_fgs24_1010/test_fgs24_1010.xml");
    	FileInputStream fis = new FileInputStream(testfile);
    	byte[] testobject = new byte[(int)testfile.length()];
    	fis.read(testobject);
    	apim.ingest(testobject, "info:fedora/fedora-system:FOXML-1.1", "test ingest");
    	delay(8000);
	    doOp("?operation=updateIndex&action=optimize&restXslt=copyXml");
	    delay(8000);
  	    StringBuffer result = doOp("?operation=gfindObjects&query=ds.testMwordX:tika&restXslt=copyXml");
  	    assertXpathEvaluatesTo("1", "/resultPage/gfindObjects/@hitTotal", result.toString());
    }

    @Test
    public void testTikaExtractionSolrIngest2() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=dsSomeMd.WordCount:20&restXslt=copyXml");
  	    assertXpathEvaluatesTo("1", "/resultPage/gfindObjects/@hitTotal", result.toString());
    }

    @Test
    public void testTikaExtractionSolrIngest3() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=ds.testMpdf:tika&restXslt=copyXml");
  	    assertXpathEvaluatesTo("1", "/resultPage/gfindObjects/@hitTotal", result.toString());
    }

    @Test
    public void testTikaExtractionSolrAfter() throws Exception {
    	FedoraClient fedoraClient = new FedoraClient("http://localhost:8080/fedora", "fgsTester", "fgsTesterPassword");
    	FedoraAPIM apim = fedoraClient.getAPIM();
    	apim.purgeObject("test:fgs24_1010", "test purge", false);
 	    delay(8000);
	    doOp("?operation=updateIndex&action=optimize&restXslt=copyXml");
	    delay(8000);
  	    StringBuffer result = doOp("?operation=gfindObjects&query=ds.testMwordX:tika&restXslt=copyXml");
  	    assertXpathEvaluatesTo("0", "/resultPage/gfindObjects/@hitTotal", result.toString());
    }

    @Test
    public void testTikaExtractionSolrAfter2() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=dsSomeMd.WordCount:22&restXslt=copyXml");
  	    assertXpathEvaluatesTo("0", "/resultPage/gfindObjects/@hitTotal", result.toString());
    }

    @Test
    public void testTikaExtractionSolrAfter3() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=ds.testMpdf:tika&restXslt=copyXml");
  	    assertXpathEvaluatesTo("0", "/resultPage/gfindObjects/@hitTotal", result.toString());
    }
}
