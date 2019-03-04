package org.estar.node_agent2.messaging;

import java.io.File;
import java.net.URL;
import java.util.Vector;

import org.estar.rtml.RTMLDocument;
import org.estar.rtml.RTMLIntelligentAgent;
import org.estar.rtml.RTMLParser;

import ngat.util.logging.LogManager;
import ngat.util.logging.Logger;

import org.estar.node_agent2.util.LoggerUtil;
//import org.estar.node_agent2.util.RTMLUtil;

/**
 * This class services RTML updates back to Intelligent Agents.
 * It receives messages from the DefaultNodeAgentAsynchronousResponseHandler.handleAsyncResponse(rtmlDocument)
 * method and from the RTML received, it finds which IA (from the Hash lookup table) the rtml is destined for
* and should return the RTML document to the IA.
*/
public class IAAsynchronousMessagingClient 
{
	static Logger traceLogger = LogManager.getLogger(LoggerUtil.TRACE_LOGGER_NAME);
	static Logger errorLogger = LogManager.getLogger(LoggerUtil.ERROR_LOGGER_NAME);
	
	public IAAsynchronousMessagingClient() {}
	
	/**
	 * 
	 * @param rtmlDocument
	 * @throws Exception
	 */
	public void sendRTMLUpdateToIA(RTMLDocument rtmlDocument) throws Exception 
	{
		traceLogger.log(5, IAAsynchronousMessagingClient.class.getName(), "sendRTMLUpdateToIA()");
	
		//get the address of the agent to send the document to
		RTMLIntelligentAgent returnAgent =rtmlDocument.getIntelligentAgent();
	
		// TODO not implemented at the moment
		//unmarshal a String version of the document
		//String rtmlDocumentString = RTMLUtil.getRTMLAsString(rtmlDocument);
		
		//get the username for the call from the RTML
		//String username = rtmlDocument.getContact().getUser();
		//lookup the password for the user from the passwordMapStore
		//PersistentMap passwordMapStore = PersistanceController.getInstance().getPasswordMapStore();
		//String password = passwordMapStore.getProperty(username);
		
		//build the Cookie header value from the user/password details
		//String cookieAuthString = CookieUtils.makeAuthCookieString(username, password); 
		
		//send rtmlDocumentString to host and port of the returnAgent
		//if (rtmlDocument.getVersion().equals(RTMLDocument.RTML_VERSION_22)) {
		//	String host = returnAgent.getHostname();
		//	int port = returnAgent.getPort();
		//	traceLogger.log(5, IAAsynchronousMessagingClient.class.getName(), "... host=" + host + ", port=" + port + ", username=" + username);
		//	sendRTMLUpdateToIA(cookieAuthString, rtmlDocumentString, host, port);
		//} else {
		//	String returnURL = returnAgent.getUri();
		//	sendRTMLUpdateToIA(cookieAuthString, rtmlDocumentString, new URL(returnURL));
		//}
	}		

}
