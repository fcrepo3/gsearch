//$Id:  $
package gsearch.test.testsonlucene;

import junit.framework.TestSuite;

import org.junit.Test;

import gsearch.test.FgsTestCase;

/**
 * Tests on lucene plugin
 * 
 * assuming 
 * - all Fedora demo objects are in the repository referenced in
 *   configTestOnLucene/repository/DemoAtDtu/repository.properties
 * 
 * the test suite will
 * - set configTestOnLucene as current config. 
 */
public class TestSortFields
        extends FgsTestCase {

    // Supports legacy test runners
    public static junit.framework.Test suite() {
        TestSuite suite = new TestSuite("configDemoOnLucene TestSuite");
        suite.addTestSuite(TestSortFields.class);
        return new TestConfigSetup(suite);
    }

    @Test
    public void testGfindObjects() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=image&restXslt=copyXml");
  	    assertXpathEvaluatesTo("7", "/resultPage/gfindObjects/@hitTotal", result.toString());
    }

    @Test
    public void testGfindObjectsSortAUTO() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=image&sortFields=PID,AUTO&restXslt=copyXml");
  	    assertXpathEvaluatesTo("demo:10", "/resultPage/gfindObjects/objects/object[1]/field[@name='PID']/text()", result.toString());
    }

    @Test
    public void testGfindObjectsSortAUTOreverse() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=image&sortFields=PID,AUTO,reverse&restXslt=copyXml");
  	    assertXpathEvaluatesTo("demo:7", "/resultPage/gfindObjects/objects/object[1]/field[@name='PID']/text()", result.toString());
    }

    @Test
    public void testGfindObjectsSortCompClass() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=image&sortFields=dc.title,dk.defxws.fedoragsearch.test.ComparatorSourceTest&restXslt=copyXml");
  	    assertXpathEvaluatesTo("demo:30", "/resultPage/gfindObjects/objects/object[1]/field[@name='PID']/text()", result.toString());
    }

    @Test
    public void testGfindObjectsSortCompClassWithParams() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=image&sortFields=dc.title,dk.defxws.fedoragsearch.test.ComparatorSourceTest(1-9),reverse&restXslt=copyXml");
  	    assertXpathEvaluatesTo("demo:11", "/resultPage/gfindObjects/objects/object[1]/field[@name='PID']/text()", result.toString());
    }

    @Test
    public void testGfindObjectsSortNonExistingField() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=image&sortFields=nonexistingfield&restXslt=copyXml");
		assertTrue(result.indexOf("not found as index field")>-1);
    }

    @Test
    public void testGfindObjectsSortEmptySortField() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=image&sortFields=PID;+;+&restXslt=copyXml");
		assertTrue(result.indexOf("empty sortField")>-1);
    }

    @Test
    public void testGfindObjectsSortEmptySortFieldName() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=image&sortFields=+,+&restXslt=copyXml");
		assertTrue(result.indexOf("empty sortFieldName")>-1);
    }

    @Test
    public void testGfindObjectsSortEmptySortTypeOrLocale() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=image&sortFields=PID,+,+&restXslt=copyXml");
		assertTrue(result.indexOf("empty sortType or locale")>-1);
    }

    @Test
    public void testGfindObjectsSortUnknownSortType() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=image&sortFields=PID,UNKNOWN&restXslt=copyXml");
		assertTrue(result.indexOf("unknown sortType")>-1);
    }

    @Test
    public void testGfindObjectsSortUnknownLocale() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=image&sortFields=PID,aa-BB-var-xyz&restXslt=copyXml");
		assertTrue(result.indexOf("unknown locale")>-1);
    }

    @Test
    public void testGfindObjectsSortEmptyLanguage() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=image&sortFields=PID,-+-GB&restXslt=copyXml");
		assertTrue(result.indexOf("empty language")>-1);
    }

    @Test
    public void testGfindObjectsSortUnknownLanguage() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=image&sortFields=PID,unknown&restXslt=copyXml");
		assertTrue(result.indexOf("unknown language")>-1);
    }

    @Test
    public void testGfindObjectsSortEmptyCountry() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=image&sortFields=PID,cy-+-+&restXslt=copyXml");
		assertTrue(result.indexOf("empty country")>-1);
    }

    @Test
    public void testGfindObjectsSortEmptyVariant() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=image&sortFields=PID,cy-GB-+-&restXslt=copyXml");
		assertTrue(result.indexOf("empty variant")>-1);
    }

    @Test
    public void testGfindObjectsSortUnknownReverse() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=image&sortFields=PID,cy,noreverse&restXslt=copyXml");
		assertTrue(result.indexOf("unknown reverse")>-1);
    }

    @Test
    public void testGfindObjectsSortOnTokenizedField() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=image&sortFields=dc.description&restXslt=copyXml");
		assertTrue(result.indexOf("impossible to sort on tokenized fields")>-1);
    }

    @Test
    public void testGfindObjectsSortClassNotFound() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=image&sortFields=dc.title,dk.defxws.fedoragsearch.test.ClassNotFound&restXslt=copyXml");
		assertTrue(result.indexOf("class not found")>-1);
    }

    @Test
    public void testGfindObjectsSortParamsMalformed() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=image&sortFields=dc.title,dk.defxws.fedoragsearch.test.ComparatorSourceTest(1-9&restXslt=copyXml");
		assertTrue(result.indexOf("comparatorClass parameters malformed")>-1);
    }

    @Test
    public void testGfindObjectsSortWrongNoArgs() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=image&sortFields=dc.title,dk.defxws.fedoragsearch.test.ComparatorSourceTest(5)&restXslt=copyXml");
		assertTrue(result.indexOf("wrong number of arguments")>-1);
    }
}
