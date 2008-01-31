//$Id$

package dk.defxws.fedoragsearch.client;

import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import junit.framework.TestCase;

/**
 * @author gsp
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class OperationsTest extends TestCase {

	public static void main(String[] args) {
		junit.textui.TestRunner.run(OperationsTest.class);
	}

	public final void testGfindObjects() {
		OperationsService opsService = new OperationsServiceLocator();
		try {
			Operations ops = opsService.getFgsOperations();
//			try {
//				String result = ops.gfindObjects("metal", 1, 2, 3, 50, "Lucene", "");
//				assertEquals("12052 ", result); 
//			} catch (RemoteException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//				fail(e1.toString());
//			}
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail(e.toString());
		}
	}

	public final void testGetIndexInfo() {
		OperationsService opsService = new OperationsServiceLocator();
		try {
			Operations ops = opsService.getFgsOperations();
			try {
				String result = ops.getIndexInfo("DemoOnLucene", "");
				assertEquals("lucene info ", result);
			} catch (RemoteException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				fail(e1.toString());
			}
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail(e.toString());
		}
	}

}
