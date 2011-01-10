/*
 * <p><b>License and Copyright: </b>The contents of this file is subject to the
 * same open source license as the Fedora Repository System at www.fedora-commons.org
 * Copyright &copy; 2006, 2007, 2008 by The Technical University of Denmark.
 * All rights reserved.</p>
 */
package dk.defxws.fedoragsearch.server;

import java.net.URL;

import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import dk.defxws.fedoragsearch.server.errors.ConfigException;

import org.apache.log4j.Logger;

import org.fcrepo.client.messaging.JmsMessagingClient;
import org.fcrepo.client.messaging.MessagingClient;
import org.fcrepo.client.messaging.MessagingListener;

import org.fcrepo.server.errors.MessagingException;
import org.fcrepo.server.messaging.APIMMessage;
import org.fcrepo.server.messaging.AtomAPIMMessage;

/**
 * Starts up the Fedora message listener which 
 * listens for updates to Fedora objects from the Fedora 
 * messaging system and performs index updates as 
 * needed to keep the index in sync with the repository.
 *
 * @author Bill Branan
 */
public class UpdateListener extends HttpServlet implements MessagingListener {

    private static final long serialVersionUID = 1L;
    private final Logger logger = Logger.getLogger(UpdateListener.class);
    private ArrayList<MessagingClient> messagingClientList =
            new ArrayList<MessagingClient>();
    
    /**
     * Initializes the update listener in order to start 
     * listening for update messages. 
     * 
     * {@inheritDoc}
     */
    public void init() throws ServletException {
        logger.info("Initializing the Update Listener");

        Hashtable updaterPropertiesTable;
        Config config = null;
        try {
            config = Config.getCurrentConfig();        
            updaterPropertiesTable = config.getUpdaterProps();            
        } catch(ConfigException ce) {
            logger.error("Config Exception encountered attempting to retrieve "
            		   + "updater properties: ", ce);           
            updaterPropertiesTable = null;
        }        
        
        if(updaterPropertiesTable == null) { 
            // There are no updaters to configure
            String warningMessage = "Updater properties were not loaded so no "
            	  + "update listeners were created. Update messages will "
            	  + "not be received or processed."; 
            logger.warn(warningMessage);
            return;            
        }
        
        // Create a messaging client for each set of updater properties
        Iterator updaterProperties = updaterPropertiesTable.values().iterator();
        int updaterIndex = 0;
        while(updaterProperties.hasNext()) {
            Properties properties = (Properties) updaterProperties.next();           
            
            String clientId = properties.getProperty("client.id");
            if(clientId == null) {
                ++updaterIndex;
                clientId = "fedoragsearch" + updaterIndex;
            }
            
            try {
                JmsMessagingClient messagingClient =
                        new JmsMessagingClient(clientId, this, properties, true);
                messagingClient.start(false);
                messagingClientList.add(messagingClient);
            } catch (MessagingException me) {
                String errorMessage = "Messaging exception encountered "
                    + "attempting to start messaging client with id " 
                    + clientId + ". Error message was: " + me.getMessage();
                logger.error(errorMessage, me);
            } 
        }        
    }
    
    /**
     * Closes down the messaging clients. 
     * 
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        if (messagingClientList != null && messagingClientList.size() > 0) {
            Iterator<MessagingClient> clients = messagingClientList.iterator();
            while (clients.hasNext()) {
                MessagingClient client = clients.next();
                try {
                    client.stop(false);
                } catch (MessagingException me) {
                    logger.warn("Messaging exception encountered stopping the "
                              + "messaging client: " + me.getMessage() + ". This "
                              + "error is expected and can be ignored if the message "
                              + "broker was shut down prior to the UpdateListener.");
                }
            }
        }
        super.destroy();
    }

    /**
     * Handles update messages as they are received. Extracts
     * update information and calls updateIndex().
     * 
     * {@inheritDoc}
     */
    public void onMessage(String clientId, Message message) {

        logger.debug("Received Fedora Message: " + message.toString());
        
        String messageText = "";
        if (message instanceof TextMessage) {
            try {
                messageText = ((TextMessage) message).getText();
            } catch (JMSException jmse) {
                logger.error("Unable to retrieve text from update message, "
                        + "message cannot be processed:" + message.toString());
                return;
            }
        } else {
            logger.warn("Receieved non-text message in UpdateListener, "
                    + "message was of type " + message.getClass());
            return;
        }

        logger.debug("Message Text: " + messageText);

        APIMMessage apimMessage = new AtomAPIMMessage(messageText);
        String baseUrl = apimMessage.getBaseUrl();
        String methodName = apimMessage.getMethodName();
        String pid = apimMessage.getPID();
        
        if(pid == null || pid.equals("")) {
            logger.warn("Received update message with no PID. No update performed.\n" 
                        + messageText);
            return;
        }
        
        URL repositoryUrl = null;
        try {
            repositoryUrl = new URL(baseUrl);
        } catch (Exception e) {
            logger.error("Could not create URL from message base url"
            		   + " because of exception: " + e.getMessage(), e);
            repositoryUrl = null;
        }
            
        // Call GSearch update index
        String action = "fromPid";
        if(methodName.equals("purgeObject")) {
            action = "deletePid";
        }
        String repositoryName = "";       
        String value = pid;
        String indexName = "";
        String indexDocXslt = "";
        String resultPageXslt = "";

        GenericOperationsImpl ops = new GenericOperationsImpl();
        Config config = null;
        try {
            config = Config.getCurrentConfig();
            
            if(repositoryUrl != null) {
                repositoryName = config.getRepositoryNameFromUrl(repositoryUrl);
            }
            ops.init(indexName, config);
            String updateStaus =
                    ops.updateIndex(action,
                                    value,
                                    repositoryName,
                                    indexName,
                                    indexDocXslt,
                                    resultPageXslt);
            logger.info("Index updated by notification message, returning:\n"
                      + updateStaus);
        } catch (RemoteException re) {
            logger.error("Unable to perform index update due to Exception: "
                        + re.getMessage(), re);
        }
    }    
    
}
