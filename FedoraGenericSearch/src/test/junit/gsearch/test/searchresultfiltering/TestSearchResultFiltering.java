//$Id:  $
package gsearch.test.searchresultfiltering;

import junit.framework.TestSuite;

import org.junit.Test;

import gsearch.test.FgsTestCase;

/**
 * Test of search result filtering
 * 
 * assuming 
 * - configDemoSearchResultFiltering is current config, see TestSearchResultFilteringSetup.java,
 * - all Fedora demo objects are in the repository referenced in
 *   configDemoSearchResultFiltering/repository/DemoRepos/repository.properties
 * - the file configDemoSearchResultFiltering/fedora-users.xml
 *   is copied into $FEDORA_HOME/server/config
 * - the two XACML policy files in configDemoSearchResultFiltering/fgs-policies
 *   are copied into $FEDORA_HOME/data/fedora-xacml-policies/repository-policies/fgs-policies
 * - the searchResultFilteringModule, see configDemoSearchResultFiltering/fedoragsearch.properties, is 
 *   dk.defxws.fedoragsearch.server.SearchResultFilteringDemoImpl, where methods are implemented
 *   that filter searches for presearch and insearch in the same way as the policies do for postsearch.
 * 
 * the test suite will
 * - create three empty indexes as configured and 
 * - update them with the respective indexing stylesheets
 * - configure the searchResultFilteringType to presearch, insearch, and postsearch and
 *   - for each type and for each user (fedoraAdmin, smileyAdmin1 and smileyUser1)
 *     do a gfindObjects operation. 
 * - do a gfindObjects operation for unknownUser1, in order to get an unauthorized reply. 
 * - do gfindObjects operations for other errors. 
 */
public class TestSearchResultFiltering
        extends FgsTestCase {

    // Supports legacy test runners
    public static junit.framework.Test suite() {
        TestSuite suite = new TestSuite("Search result filtering TestSuite");
        suite.addTestSuite(TestSearchResultFiltering.class);
        return new TestSearchResultFilteringSetup(suite);
    }

    @Test
    public void testSetConfigDemoSearchResultFiltering() throws Exception {
        System.setProperty("fedoragsearch.clientType", "REST");
  	    StringBuffer result = doOp("?operation=configure&configName=configDemoSearchResultFiltering&restXslt=copyXml");
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
    	assertXpathExists("/resultPage/updateIndex/@docCount", result.toString());
    }
    
    @Test
    public void testSetPresearch() throws Exception {
        StringBuffer result = doOp("?operation=configure&configName=configDemoSearchResultFiltering&propertyName=fedoragsearch.searchResultFilteringType&propertyValue=presearch&restXslt=copyXml");
  	    assertXpathNotExists("/resultPage/error", result.toString());
    }

    @Test
    public void testSetSrfModule() throws Exception {
        StringBuffer result = doOp("?operation=configure&configName=configDemoSearchResultFiltering&propertyName=fedoragsearch.searchResultFilteringModule&propertyValue=dk.defxws.fedoragsearch.server.SearchResultFilteringDemoImpl&restXslt=copyXml");
  	    assertXpathNotExists("/resultPage/error", result.toString());
    }

    @Test
    public void testPresearchFedoraAdmin() throws Exception {
        System.setProperty("fedoragsearch.fgsUserName", "fedoraAdmin");
        System.setProperty("fedoragsearch.fgsPassword", "fedoraAdminPassword");
  	    StringBuffer result = doOp("?operation=gfindObjects&query=image+OR+smiley&restXslt=copyXml");
  	    assertXpathEvaluatesTo("15", "/resultPage/gfindObjects/@hitTotal", result.toString());
    }

    @Test
    public void testPresearchSmileyAdmin() throws Exception {
        System.setProperty("fedoragsearch.fgsUserName", "smileyAdmin1");
        System.setProperty("fedoragsearch.fgsPassword", "smileyAdmin1");
  	    StringBuffer result = doOp("?operation=gfindObjects&query=image+OR+smiley&restXslt=copyXml");
  	    assertXpathEvaluatesTo("13", "/resultPage/gfindObjects/@hitTotal", result.toString());
    }

    @Test
    public void testPresearchSmileyUser() throws Exception {
        System.setProperty("fedoragsearch.fgsUserName", "smileyUser1");
        System.setProperty("fedoragsearch.fgsPassword", "smileyUser1");
  	    StringBuffer result = doOp("?operation=gfindObjects&query=image+OR+smiley&restXslt=copyXml");
  	    assertXpathEvaluatesTo("12", "/resultPage/gfindObjects/@hitTotal", result.toString());
    }

    @Test
    public void testSetInsearch() throws Exception {
        System.setProperty("fedoragsearch.fgsUserName", "fgsTester");
        System.setProperty("fedoragsearch.fgsPassword", "fgsTesterPassword");
        StringBuffer result = doOp("?operation=configure&configName=configDemoSearchResultFiltering&propertyName=fedoragsearch.searchResultFilteringType&propertyValue=insearch&restXslt=copyXml");
  	    assertXpathNotExists("/resultPage/error", result.toString());
    }

    @Test
    public void testInsearchFedoraAdmin() throws Exception {
        System.setProperty("fedoragsearch.fgsUserName", "fedoraAdmin");
        System.setProperty("fedoragsearch.fgsPassword", "fedoraAdminPassword");
  	    StringBuffer result = doOp("?operation=gfindObjects&query=image+OR+smiley&restXslt=copyXml");
  	    assertXpathEvaluatesTo("15", "/resultPage/gfindObjects/@hitTotal", result.toString());
    }

    @Test
    public void testInsearchSmileyAdmin() throws Exception {
        System.setProperty("fedoragsearch.fgsUserName", "smileyAdmin1");
        System.setProperty("fedoragsearch.fgsPassword", "smileyAdmin1");
  	    StringBuffer result = doOp("?operation=gfindObjects&query=image+OR+smiley&restXslt=copyXml");
  	    assertXpathEvaluatesTo("13", "/resultPage/gfindObjects/@hitTotal", result.toString());
    }

    @Test
    public void testInsearchSmileyUser() throws Exception {
        System.setProperty("fedoragsearch.fgsUserName", "smileyUser1");
        System.setProperty("fedoragsearch.fgsPassword", "smileyUser1");
  	    StringBuffer result = doOp("?operation=gfindObjects&query=image+OR+smiley&restXslt=copyXml");
  	    assertXpathEvaluatesTo("12", "/resultPage/gfindObjects/@hitTotal", result.toString());
    }    

// For postsearch policies to work, modify value in fedora.fcfg: 
//    module role="org.fcrepo.server.security.Authorization"
//    param name="ENFORCE-MODE" value="enforce-policies"

    @Test
    public void testSetPostsearch() throws Exception {
        System.setProperty("fedoragsearch.fgsUserName", "fgsTester");
        System.setProperty("fedoragsearch.fgsPassword", "fgsTesterPassword");
        StringBuffer result = doOp("?operation=configure&configName=configDemoSearchResultFiltering&propertyName=fedoragsearch.searchResultFilteringType&propertyValue=postsearch&restXslt=copyXml");
  	    assertXpathNotExists("/resultPage/error", result.toString());
    }

    @Test
    public void testPostsearchFedoraAdmin() throws Exception {
        System.setProperty("fedoragsearch.fgsUserName", "fgsAdmin2");
        System.setProperty("fedoragsearch.fgsPassword", "fgsAdmin2");
  	    StringBuffer result = doOp("?operation=gfindObjects&query=image+OR+smiley&indexName=AllObjectsIndex&hitPageSize=100&restXslt=copyXml");
  	    assertXpathEvaluatesTo("0", "/resultPage/gfindObjects/@hitsDenied", result.toString());
    }

    @Test
    public void testPostsearchSmileyAdmin() throws Exception {
        System.setProperty("fedoragsearch.fgsUserName", "smileyAdmin1");
        System.setProperty("fedoragsearch.fgsPassword", "smileyAdmin1");
  	    StringBuffer result = doOp("?operation=gfindObjects&query=image+OR+smiley&indexName=AllObjectsIndex&hitPageSize=100&restXslt=copyXml");
  	    assertXpathEvaluatesTo("2", "/resultPage/gfindObjects/@hitsDenied", result.toString());
    }

    @Test
    public void testPostsearchSmileyUser() throws Exception {
        System.setProperty("fedoragsearch.fgsUserName", "smileyUser1");
        System.setProperty("fedoragsearch.fgsPassword", "smileyUser1");
  	    StringBuffer result = doOp("?operation=gfindObjects&query=image+OR+smiley&indexName=AllObjectsIndex&hitPageSize=100&restXslt=copyXml");
  	    assertXpathEvaluatesTo("3", "/resultPage/gfindObjects/@hitsDenied", result.toString());
    }

    @Test
    public void testUnknownUser() throws Exception {
        System.setProperty("fedoragsearch.fgsUserName", "unknownUser1");
        StringBuffer result = null;
  	    try {
			result = doOp("?operation=gfindObjects&query=image+OR+smiley&indexName=AllObjectsIndex&hitPageSize=100&restXslt=copyXml");
//			System.out.println("UnknownUser: "+result.toString());
			assertTrue(result.indexOf("HTTP response code: 401")>-1);
		} catch (Exception e) {
//			System.out.println("UnknownUser: "+e.getMessage());
			assertTrue(e.getMessage().indexOf("HTTP response code: 401")>-1);
		}
    }

    @Test
    public void testSetUnknownType() throws Exception {
        System.setProperty("fedoragsearch.fgsUserName", "fgsTester");
        System.setProperty("fedoragsearch.fgsPassword", "fgsTesterPassword");
        StringBuffer result = doOp("?operation=configure&configName=configDemoSearchResultFiltering&propertyName=fedoragsearch.searchResultFilteringType&propertyValue=unknowntype&restXslt=copyXml");
		assertTrue(result.indexOf("may be stated, not 'unknowntype'")>-1);
    }

    @Test
    public void testSetNoType() throws Exception {
        StringBuffer result = doOp("?operation=configure&configName=configDemoSearchResultFiltering&propertyName=fedoragsearch.searchResultFilteringType&propertyValue=&restXslt=copyXml");
		assertTrue(result.indexOf("must be stated")>-1);
    }

    @Test
    public void testSetMoreTypes() throws Exception {
        StringBuffer result = doOp("?operation=configure&configName=configDemoSearchResultFiltering&propertyName=fedoragsearch.searchResultFilteringType&propertyValue=presearch+insearch&restXslt=copyXml");
		assertTrue(result.indexOf("one and only one")>-1);
    }

    @Test
    public void testSetUnknownSrfModule() throws Exception {
        StringBuffer result = doOp("?operation=configure&configName=configDemoSearchResultFiltering&propertyName=fedoragsearch.searchResultFilteringModule&propertyValue=unknown.Module&restXslt=copyXml");
		assertTrue(result.indexOf("class not found")>-1);
    }

    @Test
    public void testSetNoSrfModule() throws Exception {
        StringBuffer result = doOp("?operation=configure&configName=configDemoSearchResultFiltering&propertyName=fedoragsearch.searchResultFilteringModule&propertyValue=&restXslt=copyXml");
  	    assertXpathNotExists("/resultPage/error", result.toString());
    }
}
