//$Id$

package dk.defxws.fedoragsearch.server;

import java.net.MalformedURLException;
import java.rmi.RemoteException;

import org.apache.axis.AxisFault;
import org.apache.log4j.Logger;

import dk.defxws.fedoragsearch.server.fedorasoap.FedoraAPIABindingSOAPHTTPStub;
import dk.defxws.fedoragsearch.server.fedorasoap.FedoraAPIMBindingSOAPHTTPStub;
import fedora.server.types.gen.Datastream;

/**
 * @author gsp
 *
 */
public class XsltExtensions {
    
    private static final Logger logger =
        Logger.getLogger(XsltExtensions.class);

    protected String dsID;
    protected byte[] ds;
    protected String dsText;
    
//    public String getFulltextFromFoxml(
//            String pid)
//    throws java.rmi.RemoteException {
//        if (logger.isDebugEnabled())
//            logger.debug("getFulltextFromFoxml" +
//                    " pid="+pid);
//        Datastream[] dsds = null;
//        try {
//            dsds = (new FedoraAPIMBindingSOAPHTTPStub(
//                    new java.net.URL(GenericSearchService.getFedoraBase()+"/fedora/services/management"), null)).getDatastreams(pid, null, "A");
//        } catch (AxisFault e) {
//            throw new RemoteException(e.getClass().getName()+": "+e.toString());
//        } catch (MalformedURLException e) {
//            throw new RemoteException(e.getClass().getName()+": "+e.toString());
//        }
////        String mimetypes = "text/plain text/html application/pdf application/ps application/msword";
//        String mimetypes = GenericSearchService.getMimeTypes();
//        String mimetype = "";
//        dsID = null;
//        if (dsds != null) {
//            int best = 99999;
//            for (int i = 0; i < dsds.length; i++) {
//                int j = mimetypes.indexOf(dsds[i].getMIMEType());
//                if (j > -1 && best > j) {
//                    dsID = dsds[i].getID();
//                    best = j;
//                    mimetype = dsds[i].getMIMEType();
//                }
//            }
//        }
//        ds = null;
//        if (dsID != null) {
//            try {
//                ds = (new FedoraAPIABindingSOAPHTTPStub(
//                        new java.net.URL(GenericSearchService.getFedoraBase()+"/fedora/services/access"), null)).getDatastreamDissemination(pid, dsID, null).getStream();
//            } catch (AxisFault e) {
//                throw new RemoteException(e.getClass().getName()+": "+e.toString());
//            } catch (MalformedURLException e) {
//                throw new RemoteException(e.getClass().getName()+": "+e.toString());
//            }
//        }
//        dsText = "";
//        if (ds != null) {
////            dsText = (new TransformerToText().getText(ds, mimetype));
//            dsText = "<![CDATA[\n" + dsText + "\n]]>";
//        }
//        if (logger.isDebugEnabled())
//            logger.debug("updateIndex" +
//                    " pid="+pid+
//                    " dsID="+dsID+
//                    " mimetype="+mimetype+
//                    " dsText="+dsText);
//        return dsText;
//    }
    
    public static void main(String[] args) {
    }
}
