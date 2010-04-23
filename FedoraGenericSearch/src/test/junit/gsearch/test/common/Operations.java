//$Id:  $
package gsearch.test.common;

import org.junit.Test;

import gsearch.test.FgsTestCase;

/**
 * Test of common operations for the current config
 * 
 * assuming 
 * - all Fedora demo objects are in the repository referenced in the config
 * - the system property fedoragsearch.clientType is either 'REST' or 'SOAP'
 * 
 * the tests are
 * - create an empty index as configured and 
 * - update it with the data demo objects
 * - do a getRepositoryInfo operation 
 * - do a getIndexInfo operation 
 * - do a gfindObjects operation 
 * - do a browseIndex operation.
 */
public class Operations
        extends FgsTestCase {

//    @Test
//    public void testCreateEmpty() throws Exception {
//  	    StringBuffer result = doOp("?operation=updateIndex&action=createEmpty&restXslt=copyXml");
//  	    assertXpathEvaluatesTo("0", "/resultPage/updateIndex/@docCount", result.toString());
//    }

    @Test
    public void testUpdateIndexFromFoxmlFiles() throws Exception {
  	    StringBuffer result = doOp("?operation=updateIndex&action=fromFoxmlFiles&restXslt=copyXml");
  	  assertXpathExists("/resultPage/updateIndex/@docCount", result.toString());
    }

    @Test
    public void testGetRepositoryInfo() throws Exception {
  	    StringBuffer result = doOp("?operation=getRepositoryInfo&restXslt=copyXml");
  	  assertXpathExists("/resultPage/repositoryInfo", result.toString());
    }

    @Test
    public void testGetIndexInfo() throws Exception {
  	    StringBuffer result = doOp("?operation=getIndexInfo&restXslt=copyXml");
  	    assertXpathExists("/resultPage/indexInfo", result.toString());
    }

    @Test
    public void testGfindObjects() throws Exception {
  	    StringBuffer result = doOp("?operation=gfindObjects&query=image+OR+smiley&restXslt=copyXml");
  	  assertXpathExists("/resultPage/gfindObjects/@hitTotal", result.toString());
    }

    @Test
    public void testBrowseIndex() throws Exception {
  	    StringBuffer result = doOp("?operation=browseIndex&startTerm=&fieldName=PID&restXslt=copyXml");
  	  assertXpathExists("/resultPage/browseIndex/@termTotal", result.toString());
    }
}
