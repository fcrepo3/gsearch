//$Id:  $
package gsearch.test.solr;

import java.io.File;
import java.io.FileInputStream;

import junit.framework.TestSuite;

import org.junit.Test;

import fedora.client.FedoraClient;
import fedora.server.management.FedoraAPIM;
import gsearch.test.FgsTestCase;

/**
 * Test of solr plugin
 * 
 * the test suite will
 * - set configDemoOnSolr as current config.
 */
public class TestConfigDemoOnSolr
        extends FgsTestCase {

    // Supports legacy test runners
    public static junit.framework.Test suite() {
        TestSuite suite = new TestSuite("configDemoOnSolr TestSuite");
        suite.addTestSuite(TestConfigDemoOnSolr.class);
        return new TestConfigSetup(suite);
    }

    @Test
    public void testSetConfigDemoOnSolr() throws Exception {
        System.setProperty("fedoragsearch.clientType", "REST");
  	    StringBuffer result = doOp("?operation=configure&configName=configDemoOnSolr&restXslt=copyXml");
  	    assertXpathNotExists("/resultPage/error", result.toString());
    }

    @Test
    public void testGfindObjectsSortSTRING() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=imag&sortFields=PID,STRING&restXslt=copyXml");
  	    assertXpathEvaluatesTo("demo:29", "/resultPage/gfindObjects/objects/object[1]/field[@name='PID']/text()", result.toString());
    }

    @Test
    public void testGfindObjectsSortSTRINGreverse() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=imag&sortFields=PID,STRING,reverse&restXslt=copyXml");
  	    assertXpathEvaluatesTo("demo:5", "/resultPage/gfindObjects/objects/object[1]/field[@name='PID']/text()", result.toString());
    }

    @Test
    public void testGfindObjectsSortNonExistingField() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=imag&sortFields=nonexistingfield&restXslt=copyXml");
		assertTrue(result.indexOf("not found as index field")>-1);
    }

    @Test
    public void testGfindObjectsSortEmptySortField() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=imag&sortFields=PID;+;+&restXslt=copyXml");
		assertTrue(result.indexOf("empty sortField")>-1);
    }

    @Test
    public void testGfindObjectsSortEmptySortFieldName() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=imag&sortFields=+,+&restXslt=copyXml");
		assertTrue(result.indexOf("empty sortFieldName")>-1);
    }

    @Test
    public void testGfindObjectsSortEmptySortTypeOrLocale() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=imag&sortFields=PID,+,+&restXslt=copyXml");
		assertTrue(result.indexOf("empty sortType or locale")>-1);
    }

    @Test
    public void testGfindObjectsSortUnknownSortType() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=imag&sortFields=PID,UNKNOWN&restXslt=copyXml");
		assertTrue(result.indexOf("unknown sortType")>-1);
    }

    @Test
    public void testGfindObjectsSortUnknownLocale() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=imag&sortFields=PID,aa-BB-var-xyz&restXslt=copyXml");
		assertTrue(result.indexOf("unknown locale")>-1);
    }

    @Test
    public void testGfindObjectsSortEmptyLanguage() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=imag&sortFields=PID,-+-GB&restXslt=copyXml");
		assertTrue(result.indexOf("empty language")>-1);
    }

    @Test
    public void testGfindObjectsSortUnknownLanguage() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=imag&sortFields=PID,unknown&restXslt=copyXml");
		assertTrue(result.indexOf("unknown language")>-1);
    }

    @Test
    public void testGfindObjectsSortEmptyCountry() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=imag&sortFields=PID,cy-+-+&restXslt=copyXml");
		assertTrue(result.indexOf("empty country")>-1);
    }

    @Test
    public void testGfindObjectsSortEmptyVariant() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=imag&sortFields=PID,cy-GB-+-&restXslt=copyXml");
		assertTrue(result.indexOf("empty variant")>-1);
    }

    @Test
    public void testGfindObjectsSortUnknownReverse() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=imag&sortFields=PID,cy,noreverse&restXslt=copyXml");
		assertTrue(result.indexOf("unknown reverse")>-1);
    }
    
  @Test
  public void testManagedXmlDatastreamBefore() throws Exception {
  	FedoraClient fedoraClient = new FedoraClient("http://localhost:8080/fedora", "fedoraAdmin", "fedoraAdmin");
  	FedoraAPIM apim = fedoraClient.getAPIM();
  	apim.purgeObject("test:fgs23", "test purge", false);
	delay(5000);
	StringBuffer result = doOp("?operation=gfindObjects&query=testMapplXml.meta.title:gsearch&restXslt=copyXml");
	assertXpathEvaluatesTo("0", "/resultPage/gfindObjects/@hitTotal", result.toString());
  }

  @Test
  public void testManagedXmlDatastreamIngestAndAfter() throws Exception {
  	FedoraClient fedoraClient = new FedoraClient("http://localhost:8080/fedora", "fedoraAdmin", "fedoraAdmin");
  	FedoraAPIM apim = fedoraClient.getAPIM();
  	File testfile = new File("../FgsConfig/test/test_fgs23.xml");
  	FileInputStream fis = new FileInputStream(testfile);
  	byte[] testobject = new byte[(int)testfile.length()];
  	fis.read(testobject);
  	apim.ingest(testobject, "info:fedora/fedora-system:FOXML-1.1", "test ingest");
    delay(8000);
	    doOp("?operation=updateIndex&action=optimize&restXslt=copyXml");
    delay(8000);
	StringBuffer result = doOp("?operation=gfindObjects&query=testMapplXml.meta.title:gsearch&restXslt=copyXml");
	assertXpathEvaluatesTo("1", "/resultPage/gfindObjects/@hitTotal", result.toString());
  	apim.purgeObject("test:fgs23", "test purge", false);
    delay(8000);
    doOp("?operation=updateIndex&action=optimize&restXslt=copyXml");
    delay(8000);
  }

    @Test
    public void testUpdateIndexDocCount() throws Exception {
  	    StringBuffer result = doOp("?operation=updateIndex&restXslt=copyXml");
  	    assertXpathEvaluatesTo("20", "/resultPage/updateIndex/@docCount", result.toString());
    }

    @Test
    public void testUpdateIndexDeletePid() throws Exception {
  	    StringBuffer result = doOp("?operation=updateIndex&action=deletePid&value=demo:14&restXslt=copyXml");
  	    assertXpathEvaluatesTo("1", "/resultPage/updateIndex/@deleteTotal", result.toString());
    }

    @Test
    public void testUpdateIndexInsertPid() throws Exception {
  	    StringBuffer result = doOp("?operation=updateIndex&action=fromPid&value=demo:14&restXslt=copyXml");
  	    assertXpathEvaluatesTo("1", "/resultPage/updateIndex/@insertTotal", result.toString());
    }

    @Test
    public void testUpdateIndexFromPid() throws Exception {
	    delay(5000);
  	    doOp("?operation=updateIndex&action=optimize&restXslt=copyXml");
	    delay(5000);
  	    StringBuffer result = doOp("?operation=updateIndex&action=fromPid&value=demo:14&restXslt=copyXml");
  	    assertXpathEvaluatesTo("1", "/resultPage/updateIndex/@updateTotal", result.toString());
    }
//
//    @Test
//    public void testUpdateIndexFromPidWithParamBefore() throws Exception {
//  	    StringBuffer result = doOp("?operation=gfindObjects&query=EXPLICITPARAM1:explicitvalue1+and+EXPLICITPARAM2:explicitvalue2&restXslt=copyXml");
//  	    assertXpathEvaluatesTo("0", "/resultPage/gfindObjects/@hitTotal", result.toString());
//    }
//
//    @Test
//    public void testUpdateIndexFromPidWithParam() throws Exception {
//  	    StringBuffer result = doOp("?operation=updateIndex&action=fromPid&value=demo:14&indexDocXslt=testFoxmlToLuceneWithExplicitParams(EXPLICITPARAM1=explicitvalue1,EXPLICITPARAM2=explicitvalue2)&restXslt=copyXml");
//  	    assertXpathEvaluatesTo("1", "/resultPage/updateIndex/@updateTotal", result.toString());
//    }
//
//    @Test
//    public void testUpdateIndexFromPidWithParamAfter() throws Exception {
//  	    StringBuffer result = doOp("?operation=gfindObjects&query=EXPLICITPARAM1:explicitvalue1+and+EXPLICITPARAM2:explicitvalue2&restXslt=copyXml");
//  	    assertXpathEvaluatesTo("1", "/resultPage/gfindObjects/@hitTotal", result.toString());
//    }
//
//    @Test
//    public void testFedoraObjectLabelModifyBefore() throws Exception {
//  	    StringBuffer result = doOp("?operation=gfindObjects&query=fgs.label:labelmodifiedforindextest&restXslt=copyXml");
//  	    assertXpathEvaluatesTo("0", "/resultPage/gfindObjects/@hitTotal", result.toString());
//    }
//
//    @Test
//    public void testFedoraObjectLabelModifyAndAfter() throws Exception {
//    	FedoraClient fedoraClient = new FedoraClient("http://localhost:8080/fedora", "fedoraAdmin", "fedoraAdmin");
//    	FedoraAPIM apim = fedoraClient.getAPIM();
//    	apim.modifyObject("demo:14", null, "labelmodifiedforindextest", "fedoraAdmin", "test label modify");
//  	    delay(5000);
//  	    StringBuffer result = doOp("?operation=gfindObjects&query=fgs.label:labelmodifiedforindextest&restXslt=copyXml");
//  	    assertXpathEvaluatesTo("1", "/resultPage/gfindObjects/@hitTotal", result.toString());
//    	apim.modifyObject("demo:14", null, "labelremodifiedforindextest", "fedoraAdmin", "test label modify");
//    }
//    
//    @Test
//    public void testFedoraObjectStateModifyBefore() throws Exception {
//  	    delay(5000);
//  	    StringBuffer result = doOp("?operation=updateIndex&restXslt=copyXml");
//  	    assertXpathEvaluatesTo("20", "/resultPage/updateIndex/@docCount", result.toString());
//    }
//
//    @Test
//    public void testFedoraObjectStateDelete() throws Exception {
//    	FedoraClient fedoraClient = new FedoraClient("http://localhost:8080/fedora", "fedoraAdmin", "fedoraAdmin");
//    	FedoraAPIM apim = fedoraClient.getAPIM();
//    	apim.modifyObject("demo:21", "D", null, "fedoraAdmin", "test state delete");
//  	    delay(10000);
//  	    StringBuffer result = doOp("?operation=updateIndex&restXslt=copyXml");
//  	    assertXpathEvaluatesTo("19", "/resultPage/updateIndex/@docCount", result.toString());
//    }
//
//    @Test
//    public void testFedoraObjectStateActivate() throws Exception {
//    	FedoraClient fedoraClient = new FedoraClient("http://localhost:8080/fedora", "fedoraAdmin", "fedoraAdmin");
//    	FedoraAPIM apim = fedoraClient.getAPIM();
//    	apim.modifyObject("demo:21", "A", null, "fedoraAdmin", "test state activate");
//  	    delay(10000);
//  	    StringBuffer result = doOp("?operation=updateIndex&restXslt=copyXml");
//  	    assertXpathEvaluatesTo("20", "/resultPage/updateIndex/@docCount", result.toString());
//    }
//
//    @Test
//    public void testSetOperationsImplToSolr() throws Exception {
//        StringBuffer result = doOp("?operation=configure&configName=configDemoOnSolr&propertyName=FgsIndex/fgsindex.operationsImpl&propertyValue=dk.defxws.fgssolr.OperationsImpl&restXslt=copyXml");
//  	    assertXpathNotExists("/resultPage/error", result.toString());
//    }
//
//    @Test
//    public void testSetUriResolverForSolr() throws Exception {
//        StringBuffer result = doOp("?operation=configure&configName=configDemoOnSolr&propertyName=FgsIndex/fgsindex.uriResolver&propertyValue=dk.defxws.fedoragsearch.server.URIResolverImpl&restXslt=copyXml");
//  	    assertXpathNotExists("/resultPage/error", result.toString());
//    }
//
//    @Test
//    public void testSetOperationsImplToLucene() throws Exception {
//        StringBuffer result = doOp("?operation=configure&configName=configDemoOnSolr&propertyName=FgsIndex/fgsindex.operationsImpl&propertyValue=dk.defxws.fgslucene.OperationsImpl&restXslt=copyXml");
//  	    assertXpathNotExists("/resultPage/error", result.toString());
//    }
//
//    @Test
//    public void testSetUriResolverForLucene() throws Exception {
//        StringBuffer result = doOp("?operation=configure&configName=configDemoOnSolr&propertyName=FgsIndex/fgsindex.uriResolver&propertyValue=dk.defxws.fedoragsearch.server.URIResolverImpl&restXslt=copyXml");
//  	    assertXpathNotExists("/resultPage/error", result.toString());
//    }
//
//    @Test
//    public void testSetOperationsImplToNotvalid() throws Exception {
//        StringBuffer result = doOp("?operation=configure&configName=configDemoOnSolr&propertyName=FgsIndex/fgsindex.operationsImpl&propertyValue=dk.defxws.fgsnotvalid.OperationsImpl&restXslt=copyXml");
//  	    assertXpathExists("/resultPage/error", result.toString());
//    }
//
//    @Test
//    public void testSetUriResolverToNotvalid() throws Exception {
//        StringBuffer result = doOp("?operation=configure&configName=configDemoOnSolr&propertyName=FgsIndex/fgsindex.uriResolver&propertyValue=dk.defxws.fedoragsearch.server.URIResolverImplNotvalid&restXslt=copyXml");
//  	    assertXpathExists("/resultPage/error", result.toString());
//    }
//
//    @Test
//    public void testSetUriResolverToNone() throws Exception {
//        StringBuffer result = doOp("?operation=configure&configName=configDemoOnSolr&propertyName=FgsIndex/fgsindex.uriResolver&propertyValue=&restXslt=copyXml");
//  	    assertXpathNotExists("/resultPage/error", result.toString());
//    }
//
//    @Test
//    public void testGfindObjectsWildcardBeforeAllow() throws Exception {
//  	    StringBuffer result = doOp("?operation=gfindObjects&query=?mage&restXslt=copyXml");
//  	    assertXpathExists("/resultPage/error", result.toString());
//    }
//
//    @Test
//    public void testSetAllowLeadingWildcard() throws Exception {
//        StringBuffer result = doOp("?operation=configure&configName=configDemoOnSolr&propertyName=FgsIndex/fgsindex.allowLeadingWildcard&propertyValue=true&restXslt=copyXml");
//  	    assertXpathNotExists("/resultPage/error", result.toString());
//    }
//
//    @Test
//    public void testGfindObjectsWildcardAfterAllow() throws Exception {
//  	    StringBuffer result = doOp("?operation=gfindObjects&query=?mage&restXslt=copyXml");
//  	    assertXpathExists("/resultPage/gfindObjects/@hitTotal", result.toString());
//    }
//
//    @Test
//    public void testSetAllowLeadingWildcardNotvalid() throws Exception {
//        StringBuffer result = doOp("?operation=configure&configName=configDemoOnSolr&propertyName=FgsIndex/fgsindex.allowLeadingWildcard&propertyValue=notvalid&restXslt=copyXml");
//  	    assertXpathExists("/resultPage/error", result.toString());
//    }
//
//    @Test
//    public void testSetXsltProcessorToSaxon() throws Exception {
//        StringBuffer result = doOp("?operation=configure&configName=configDemoOnSolr&propertyName=fedoragsearch.xsltProcessor&propertyValue=saxon&restXslt=copyXml");
//  	    assertXpathNotExists("/resultPage/error", result.toString());
//    }
//
//    @Test
//    public void testSetXsltProcessorToXalan() throws Exception {
//        StringBuffer result = doOp("?operation=configure&configName=configDemoOnSolr&propertyName=fedoragsearch.xsltProcessor&propertyValue=xalan&restXslt=copyXml");
//  	    assertXpathNotExists("/resultPage/error", result.toString());
//    }
//
//    @Test
//    public void testSetXsltProcessorToNotvalid() throws Exception {
//        StringBuffer result = doOp("?operation=configure&configName=configDemoOnSolr&propertyName=fedoragsearch.xsltProcessor&propertyValue=notvalid&restXslt=copyXml");
//  	    assertXpathExists("/resultPage/error", result.toString());
//    }
}
