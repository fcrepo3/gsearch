//$Id:  $
package gsearch.test.lucene.fgs24_1010;

import java.io.File;
import java.io.FileInputStream;

import junit.framework.TestSuite;

import org.junit.BeforeClass;
import org.junit.Test;

import fedora.client.FedoraClient;
import fedora.server.management.FedoraAPIM;
import gsearch.test.FgsTestCase;

/**
 * Test of fcrepo-1010 : Tika extraction on the lucene plugin
 */
public class TestConfigOnLuceneFgs24_1010
        extends FgsTestCase {

    // Supports legacy test runners
    public static junit.framework.Test suite() {
        TestSuite suite = new TestSuite("configTestOnLuceneFgs24_1010 TestSuite");
        suite.addTestSuite(TestConfigOnLuceneFgs24_1010.class);
        return new TestConfigSetup(suite);
    }

    @Test
    public void testSetConfigTestOnLuceneFgs24_1010() throws Exception {
        System.setProperty("fedoragsearch.clientType", "REST");
  	    StringBuffer result = doOp("?operation=configure&configName=configTestOnLuceneFgs24_1010&restXslt=copyXml");
//        System.out.println("result="+result.toString());
  	    assertXpathNotExists("/resultPage/error", result.toString());
    }

    @Test
    public void testTikaExtractionLuceneBefore() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=ds.testMwordX:tika&restXslt=copyXml");
  	    assertXpathEvaluatesTo("0", "/resultPage/gfindObjects/@hitTotal", result.toString());
    }

    @Test
    public void testTikaExtractionLuceneBefore2() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=dsSomeMd.Word-Count:22&restXslt=copyXml");
  	    assertXpathEvaluatesTo("0", "/resultPage/gfindObjects/@hitTotal", result.toString());
    }

    @Test
    public void testTikaExtractionLuceneBefore3() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=ds.testMpdf:tika&restXslt=copyXml");
  	    assertXpathEvaluatesTo("0", "/resultPage/gfindObjects/@hitTotal", result.toString());
    }

    @Test
    public void testTikaExtractionLuceneBefore4() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=dsAllMd.Content-Type:\"application/pdf\"&restXslt=copyXml");
  	    assertXpathEvaluatesTo("0", "/resultPage/gfindObjects/@hitTotal", result.toString());
    }

    @Test
    public void testTikaExtractionLuceneBefore5() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=dsUntok.Content-Type:\"application/pdf\"&restXslt=copyXml");
  	    assertXpathEvaluatesTo("0", "/resultPage/gfindObjects/@hitTotal", result.toString());
    }

    @Test
    public void testTikaExtractionLuceneIngest() throws Exception {
    	FedoraClient fedoraClient = new FedoraClient("http://localhost:8080/fedora", "fgsTester", "fgsTesterPassword");
    	FedoraAPIM apim = fedoraClient.getAPIM();
    	File testfile = new File("../FgsConfig/test_fgs24_1010/test_fgs24_1010.xml");
    	FileInputStream fis = new FileInputStream(testfile);
    	byte[] testobject = new byte[(int)testfile.length()];
    	fis.read(testobject);
    	apim.ingest(testobject, "info:fedora/fedora-system:FOXML-1.1", "test ingest");
  	    delay(5000);
  	    StringBuffer result = doOp("?operation=gfindObjects&query=ds.testMwordX:tika&restXslt=copyXml");
  	    assertXpathEvaluatesTo("1", "/resultPage/gfindObjects/@hitTotal", result.toString());
    }

    @Test
    public void testTikaExtractionLuceneIngest2() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=dsSomeMd.Word-Count:22&restXslt=copyXml");
  	    assertXpathEvaluatesTo("1", "/resultPage/gfindObjects/@hitTotal", result.toString());
    }

    @Test
    public void testTikaExtractionLuceneIngest3() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=ds.testMpdf:tika&restXslt=copyXml");
  	    assertXpathEvaluatesTo("1", "/resultPage/gfindObjects/@hitTotal", result.toString());
    }

    @Test
    public void testTikaExtractionLuceneIngest4() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=dsAllMd.Content-Type:\"application/pdf\"&restXslt=copyXml");
  	    assertXpathEvaluatesTo("1", "/resultPage/gfindObjects/@hitTotal", result.toString());
    }

    @Test
    public void testTikaExtractionLuceneIngest5() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=dsUntok.Content-Type:\"application/pdf\"&restXslt=copyXml");
  	    assertXpathEvaluatesTo("1", "/resultPage/gfindObjects/@hitTotal", result.toString());
    }

    @Test
    public void testTikaExtractionLuceneAfter() throws Exception {
    	FedoraClient fedoraClient = new FedoraClient("http://localhost:8080/fedora", "fgsTester", "fgsTesterPassword");
    	FedoraAPIM apim = fedoraClient.getAPIM();
    	apim.purgeObject("test:fgs24_1010", "test purge", false);
 	    delay(5000);
  	    StringBuffer result = doOp("?operation=gfindObjects&query=ds.testMwordX:tika&restXslt=copyXml");
  	    assertXpathEvaluatesTo("0", "/resultPage/gfindObjects/@hitTotal", result.toString());
    }

    @Test
    public void testTikaExtractionLuceneAfter2() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=dsSomeMd.Word-Count:22&restXslt=copyXml");
  	    assertXpathEvaluatesTo("0", "/resultPage/gfindObjects/@hitTotal", result.toString());
    }

    @Test
    public void testTikaExtractionLuceneAfter3() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=ds.testMpdf:tika&restXslt=copyXml");
  	    assertXpathEvaluatesTo("0", "/resultPage/gfindObjects/@hitTotal", result.toString());
    }

    @Test
    public void testTikaExtractionLuceneAfter4() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=dsAllMd.Content-Type:\"application/pdf\"&restXslt=copyXml");
  	    assertXpathEvaluatesTo("0", "/resultPage/gfindObjects/@hitTotal", result.toString());
    }

    @Test
    public void testTikaExtractionLuceneAfter5() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=dsUntok.Content-Type:\"application/pdf\"&restXslt=copyXml");
  	    assertXpathEvaluatesTo("0", "/resultPage/gfindObjects/@hitTotal", result.toString());
    }
}
