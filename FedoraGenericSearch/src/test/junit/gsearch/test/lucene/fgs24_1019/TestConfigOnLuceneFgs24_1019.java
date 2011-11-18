//$Id:  $
package gsearch.test.lucene.fgs24_1019;

import java.io.File;
import java.io.FileInputStream;

import junit.framework.TestSuite;

import org.junit.BeforeClass;
import org.junit.Test;

import org.fcrepo.client.FedoraClient;
import org.fcrepo.server.management.FedoraAPIM;
import gsearch.test.FgsTestCase;

/**
 * Test of fcrepo-1019 : Exploration of complex GSearch use cases
 */
public class TestConfigOnLuceneFgs24_1019
        extends FgsTestCase {

    // Supports legacy test runners
    public static junit.framework.Test suite() {
        TestSuite suite = new TestSuite("configTestOnLuceneFgs24_1019 TestSuite");
        suite.addTestSuite(TestConfigOnLuceneFgs24_1019.class);
        return new TestConfigSetup(suite);
    }

    @Test
    public void testSetConfigDemoIndexPerDS_fgs24_1019() throws Exception {
        System.setProperty("fedoragsearch.clientType", "REST");
  	    StringBuffer result = doOp("?operation=configure&configName=configDemoIndexPerDS_fgs24_1019&restXslt=copyXml");
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
    public void testFindPDFs() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=dsmd.Content-Type:\"application/pdf\"&restXslt=copyXml");
  	    assertXpathEvaluatesTo("1", "/resultPage/gfindObjects/@hitTotal", result.toString());
    }

    @Test
    public void testFindXML_SOURCEs() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=dsmd.Content-Type:\"application/xml\"&restXslt=copyXml");
  	    assertXpathEvaluatesTo("2", "/resultPage/gfindObjects/@hitTotal", result.toString());
    }

    @Test
    public void testFindXSLTs() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=dsmd.Content-Type:\"application/xslt\"&restXslt=copyXml");
  	    assertXpathEvaluatesTo("1", "/resultPage/gfindObjects/@hitTotal", result.toString());
    }

    @Test
    public void testFindHasModels() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=RELS-EXT.hasModel:\"info:fedora/demo:DualResImage\"&restXslt=copyXml");
  	    assertXpathEvaluatesTo("12", "/resultPage/gfindObjects/@hitTotal", result.toString());
    }

    @Test
    public void testFindIsMemberOfs() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=RELS-EXT.isMemberOf:\"info:fedora/demo:SmileyStuff\"&restXslt=copyXml");
  	    assertXpathEvaluatesTo("12", "/resultPage/gfindObjects/@hitTotal", result.toString());
    }

    @Test
    public void testFindHasCoMembers() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=RELS-EXT.hasCoMember:\"info:fedora/demo:SmileyStuff/info:fedora/demo:SmileyBeerGlass\"&restXslt=copyXml");
  	    assertXpathEvaluatesTo("12", "/resultPage/gfindObjects/@hitTotal", result.toString());
    }
}
