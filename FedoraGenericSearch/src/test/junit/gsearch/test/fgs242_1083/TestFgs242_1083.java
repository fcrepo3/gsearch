//$Id:  $
package gsearch.test.fgs242_1083;

import java.io.File;
import java.io.FileInputStream;

import junit.framework.TestSuite;

import org.fcrepo.client.FedoraClient;
import org.fcrepo.server.management.FedoraAPIM;
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
 * - set configTestOnLuceneFgs242_1083 as current config,
 * - run tests concerning GSearch 2.4.2 
 */
public class TestFgs242_1083
        extends FgsTestCase {

    // Supports legacy test runners
    public static junit.framework.Test suite() {
        TestSuite suite = new TestSuite("TestFgs242_1083 TestSuite");
        suite.addTestSuite(TestFgs242_1083.class);
        return new TestConfigSetup(suite);
    }

    @Test
    public void testSetConfig() throws Exception {
        System.setProperty("fedoragsearch.clientType", "REST");
  	    StringBuffer result = doOp("?operation=configure&configName=configTestOnLuceneFgs242_1083&restXslt=copyXml");
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
  	    assertXpathEvaluatesTo("20", "/resultPage/updateIndex/@docCount", result.toString());
    }

    @Test
    public void testSetUriResolverImpl() throws Exception {
        StringBuffer result = doOp("?operation=configure&configName=configTestOnLuceneFgs242_1083&propertyName=FgsIndex/fgsindex.uriResolver&propertyValue=dk.defxws.fedoragsearch.server.URIResolverImpl&restXslt=copyXml");
  	    assertXpathNotExists("/resultPage/error", result.toString());
    }

    @Test
    public void testGfindObjectsBefore() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=indexInfo.AdminInfo:getIndexInfo&restXslt=copyXml");
  	    assertXpathEvaluatesTo("0", "/resultPage/gfindObjects/@hitTotal", result.toString());
    }

    @Test
    public void testGfindObjectsBeforeFile() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=indexInfoFile.AdminInfo:getIndexInfo&restXslt=copyXml");
  	    assertXpathEvaluatesTo("0", "/resultPage/gfindObjects/@hitTotal", result.toString());
    }

    @Test
    public void testIngest() throws Exception {
    	FedoraClient fedoraClient = new FedoraClient("http://localhost:8080/fedora", "fedoraAdmin", "fedoraAdminPassword");
    	FedoraAPIM apim = fedoraClient.getAPIM();
    	File testfile = new File("../FgsConfig/test_fgs242_1083/test_fgs242_1083.xml");
    	FileInputStream fis = new FileInputStream(testfile);
    	byte[] testobject = new byte[(int)testfile.length()];
    	fis.read(testobject);
    	apim.ingest(testobject, "info:fedora/fedora-system:FOXML-1.1", "test ingest");
  	    delay(5000);
  	    doOp("?operation=updateIndex&action=fromPid&value=test:fgs242_1083&indexDocXslt=foxmlToLucene_1083&restXslt=copyXml");
  	    delay(2000);
  	    StringBuffer result = doOp("?operation=gfindObjects&query=indexInfo.AdminInfo:getIndexInfo&restXslt=copyXml");
  	    assertXpathEvaluatesTo("1", "/resultPage/gfindObjects/@hitTotal", result.toString());
    }

    @Test
    public void testGfindObjectsIngestedFile() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=indexInfoFile.AdminInfo:getIndexInfo&restXslt=copyXml");
  	    assertXpathEvaluatesTo("1", "/resultPage/gfindObjects/@hitTotal", result.toString());
    }

    @Test
    public void testPurge() throws Exception {
    	FedoraClient fedoraClient = new FedoraClient("http://localhost:8080/fedora", "fedoraAdmin", "fedoraAdminPassword");
    	FedoraAPIM apim = fedoraClient.getAPIM();
    	apim.purgeObject("test:fgs242_1083", "test purge", false);
 	    delay(5000);
  	    StringBuffer result = doOp("?operation=gfindObjects&query=indexInfo.AdminInfo:getIndexInfo&restXslt=copyXml");
  	    assertXpathEvaluatesTo("0", "/resultPage/gfindObjects/@hitTotal", result.toString());
    }

    @Test
    public void testGfindObjectsFileAfter() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=indexInfoFile.AdminInfo:getIndexInfo&restXslt=copyXml");
  	    assertXpathEvaluatesTo("0", "/resultPage/gfindObjects/@hitTotal", result.toString());
    }
}
