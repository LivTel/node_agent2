package org.estar.node_agent2;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import ngat.util.logging.LogManager;
import ngat.util.logging.Logger;

import org.estar.node_agent2.configuration.NodeAgentProperties;
import org.estar.node_agent2.messaging.RMITeaConnectionHandler;
import org.estar.node_agent2.storage.PersistenceController;
import org.estar.node_agent2.storage.PersistentMap;
import org.estar.node_agent2.util.LoggerUtil;
import org.estar.node_agent2.util.RTMLUtil;

import org.estar.rtml.RTMLDocument;
import org.estar.rtml.RTMLContact;
import org.estar.rtml.RTMLParser;

/**
 * Implementation of the Node Agent WebService interface.
 * @author cjm
 * @see org.estar.node_agent2.NodeAgentWebServiceInterface
 */
@WebService(endpointInterface = "org.estar.node_agent2.NodeAgentWebServiceInterface")
public class NodeAgentWebServiceImpl implements NodeAgentWebServiceInterface 
{
	static Logger traceLogger = LogManager.getLogger(LoggerUtil.TRACE_LOGGER_NAME);
	static Logger errorLogger = LogManager.getLogger(LoggerUtil.ERROR_LOGGER_NAME);
	
	@Resource
    WebServiceContext wsctx;

	/**
	 * Ping web-service entry point. We check the username and password are legal.
	 * If the NodeAgent is configured to be connected we return "ACK", otherwise we return
	 * "NAK (not live)".
	 * @see #traceLogger
	 * @see #checkUsernamePassword
	 * @see org.estar.node_agent2.configuration.NodeAgentProperties
	 * @see org.estar.node_agent2.configuration.NodeAgentProperties#IS_TEA_CONNECTED
	 */
	@Override
	public String ping()
	{
		traceLogger.log(5, this.getClass().getName(), ".ping() invoked");
		boolean isLive;
		String returnString;
		
		// check username and password in the SOAP headers are legal
		checkUsernamePassword();
		// Is the TEA connected
		isLive = NodeAgentProperties.getInstance().getProperty(NodeAgentProperties.IS_TEA_CONNECTED).equalsIgnoreCase(NodeAgentProperties.TRUE);
		if (isLive) 
		{
			returnString = "ACK";
		} 
		else 
		{
			returnString = "NAK (not live)";
		}
		traceLogger.log(5, this.getClass().getName(), "... returned '" + returnString + "'");
		return returnString;
	}
	
	/**
	 * Method to handle the handle_rtml web-service entry-point.
	 * @param rtmlDocumentString The RTML document to process, as a string.
	 * @return A string representation of the reply RTML document.
	 * @see #traceLogger
	 * @see #checkUsernamePassword
	 * @see #handleRTMLDocument
	 * @see org.estar.rtml.RTMLDocument
	 * @see org.estar.node_agent2.util.RTMLUtil
	 * @see org.estar.node_agent2.util.RTMLUtil#getRTMLAsString
	 * @see org.estar.node_agent2.util.RTMLUtil#getLastResortErrorDocumentString
	 */
	@Override
	public String handle_rtml(String rtmlDocumentString)
	{
		RTMLDocument rtmlDocument = null;
		String headerUsername = null;

		traceLogger.log(1, this.getClass().getName(), "invoked handle_rtml: " +rtmlDocumentString );
		// check username and password in the SOAP headers are legal
		traceLogger.log(2, this.getClass().getName(), "handle_rtml: Checking username and passsword.");
		headerUsername = checkUsernamePassword();
		// handle the RTML document
		try 
		{
			traceLogger.log(2, this.getClass().getName(), "handle_rtml: Calling handleRTMLDocument.");
			rtmlDocument = handleRTMLDocument(headerUsername,rtmlDocumentString);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			errorLogger.log(1, this.getClass().getName(), "handle_rtml:handleRTMLDocument failed with exception:"+e);
		}
		// turn returned RTMLDocument into a string
		try 
		{
			String rtmlReturnString = RTMLUtil.getRTMLAsString(rtmlDocument);
			traceLogger.log(2, this.getClass().getName(), "... ... returning SYNCHRONOUS response to IA");
			traceLogger.log(2, this.getClass().getName(), "payload= " + rtmlReturnString);
			return rtmlReturnString;
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			errorLogger.log(1, this.getClass().getName(), "handle_rtml:Failed to create return document with exception:"+e);
			return RTMLUtil.getLastResortErrorDocumentString("handle_rtml failed with exception:"+e.toString());
		}
	}
	
	/**
	 * Method to process the RTML document.
	 * <ul>
	 * <li>A parser is created, initialised, and the input rtmlDocumentString parsed. If an error occurs
	 *     an error document is returned.
	 * <li>If the TEA is not connected, the document cannot be sent to it, so an error document is returned.
	 * <li>We rewrite the eSTAR project and user alias's to LT project and user ID's, using rewriteDocumentIfAliased.
	 *     If an error occurs an error document is returned.
	 * <li>We check whether the headerUsername from the SOAP request headers, matches the RTML's Contact Username,
	 *     (after both have been unaliased), by calling  checkUsernamesMatch.
	 * <li>If the document is a score request(isScoreRequest), we call the TEA's RMI method handleScore.
	 * <li>If the document is a request document (isRequest), we call the TEA's RMI method handleRequest.
	 * <li>If the document is an abort document (isAbort), we call the TEA's RMI method handleAbort.
	 * <li>If the document was not one of the above three types, we throw an exception.
	 * </ul>
	 * @param headerUsername A string containing the username extracted from the SOAP request headers,
	 *        we use this to compare with the (alias converted) username in the RTML document to ensure
	 *        the usernames match.
	 * @param rtmlDocumentString A string representation of the document to process.
	 * @return An instance of RTMLDocument containing the document object model of the 
	 *         reply document after processing.
	 * @see #traceLogger
	 * @see #errorLogger
	 * @see #logRTMLDocument
	 * @see #checkUsernamesMatch
	 * @see org.estar.node_agent2.util.RTMLUtil#createErrorDocument
	 * @see org.estar.node_agent2.util.RTMLUtil#rewriteDocumentIfAliased
	 * @see org.estar.node_agent2.configuration.NodeAgentProperties#getInstance
	 * @see org.estar.node_agent2.configuration.NodeAgentProperties#getProperty
	 * @see org.estar.node_agent2.configuration.NodeAgentProperties#NODE_AGENT_NAME
	 * @see org.estar.node_agent2.messaging.RMITeaConnectionHandler
	 * @see org.estar.node_agent2.messaging.RMITeaConnectionHandler#getInstance
	 * @see org.estar.node_agent2.messaging.RMITeaConnectionHandler#handleScore
	 * @see org.estar.node_agent2.messaging.RMITeaConnectionHandler#handleRequest
	 * @see org.estar.node_agent2.messaging.RMITeaConnectionHandler#handleAbort
	 * @see org.estar.rtml.RTMLDocument
	 * @see org.estar.rtml.RTMLDocument#isScoreRequest
	 * @see org.estar.rtml.RTMLDocument#isRequest
	 * @see org.estar.rtml.RTMLDocument#isAbort
	 * @see org.estar.rtml.RTMLParser
	 * @see org.estar.rtml.RTMLParser#init
	 * @see org.estar.rtml.RTMLParser#parse
	 */
	protected RTMLDocument handleRTMLDocument(String headerUsername,String rtmlDocumentString) 
	{
		boolean isTeaConnected;
		String loggerMessage;
		String nodeAgentName = null;
		RTMLDocument rtmlDocument = null;
	
		traceLogger.log(1, this.getClass().getName(), "handleRTMLDocument(String) invoked");
		traceLogger.log(2, this.getClass().getName(), "looking up node_agent name");
		nodeAgentName = NodeAgentProperties.getInstance().getProperty(NodeAgentProperties.NODE_AGENT_NAME);
		traceLogger.log(3, this.getClass().getName(), "... found, name is :" +nodeAgentName );
		//parse the String to extract the RTMLDocument
		try 
		{
			traceLogger.log(2, this.getClass().getName(), "... testing errorLogger, the word 'TESTED' should follow this line");
			errorLogger.log(1, this.getClass().getName(), "... 'TESTED'");
			traceLogger.log(2, this.getClass().getName(), "... instantiating Parser");
			RTMLParser parser = new RTMLParser();
			traceLogger.log(2, this.getClass().getName(), "... successful");
			traceLogger.log(2, this.getClass().getName(), "... initialising Parser");
			parser.init(true);
			traceLogger.log(2, this.getClass().getName(), "... successful");
			traceLogger.log(2, this.getClass().getName(), "... parsing document String");
			rtmlDocument = parser.parse(rtmlDocumentString.trim());
			traceLogger.log(2, this.getClass().getName(), "... parse successful");
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			//create an RTML error document from scratch and return it
			errorLogger.log(1, this.getClass().getName(), "... unable to parse received RTML document, returning RTML error document to client:");
			RTMLDocument errorDocument = RTMLUtil.createErrorDocument(e, nodeAgentName, rtmlDocumentString);
			logRTMLDocument(errorDocument);
			return  errorDocument;
		}
		// if the TEA is not connected log and return an error document to the client
		isTeaConnected = NodeAgentProperties.getInstance().getProperty(NodeAgentProperties.IS_TEA_CONNECTED).equalsIgnoreCase(NodeAgentProperties.TRUE);
		if(!isTeaConnected) 
		{
			loggerMessage = NodeAgentProperties.IS_TEA_CONNECTED  + " = false, not sending rtml onwards";
			traceLogger.log(2, this.getClass().getName(), "... " +loggerMessage);
			traceLogger.log(2, this.getClass().getName(), "... returning RTML error document");
			Exception e = new Exception(loggerMessage);
			RTMLDocument errorDocument = RTMLUtil.createErrorDocument(e, nodeAgentName, rtmlDocumentString);
			logRTMLDocument(errorDocument);
			return  errorDocument;
		} 
		else 
		{
			loggerMessage = NodeAgentProperties.IS_TEA_CONNECTED  + " = true, sending rtml onwards";
			traceLogger.log(2, this.getClass().getName(), "... " +loggerMessage);
		}
		
		//rewrite the estar project and user alias's to ngat project and user ID's
		try
		{
			traceLogger.log(2, this.getClass().getName(), "... rewriting document if aliased");
			rtmlDocument = RTMLUtil.rewriteDocumentIfAliased(rtmlDocument, RTMLUtil.RTML_IDENT_SOURCE_ESTAR);
			traceLogger.log(2, this.getClass().getName(), "... completed rewrite");
		} 
		catch (Exception e)
		{
			RTMLDocument errorDocument = RTMLUtil.createErrorDocument(e, nodeAgentName, rtmlDocumentString);
			logRTMLDocument(errorDocument);
			return errorDocument;
		}
		// extract the unaliased username and compare it to the SOAP headers username, are they the same user?
		try
		{
			checkUsernamesMatch(headerUsername,rtmlDocument);
		}
		catch (Exception e)
		{
			RTMLDocument errorDocument = RTMLUtil.createErrorDocument(e, nodeAgentName, rtmlDocumentString);
			logRTMLDocument(errorDocument);
			return errorDocument;
		}
		try 
		{
			//send the document onwards and get the response
			traceLogger.log(2, this.getClass().getName(), "... ... sending document onwards to TEA (and awaiting response)");
			
			//******************* SEND TO TEA **********************
			RTMLDocument teaRTMLResponse;
			
			if (rtmlDocument.isScoreRequest()) 
			{
				traceLogger.log(2, this.getClass().getName(), "... document type is score request");
				teaRTMLResponse = RMITeaConnectionHandler.getInstance().handleScore(rtmlDocument);
			}
			else if (rtmlDocument.isRequest())
			{
				traceLogger.log(2, this.getClass().getName(), "... document type is request");
				teaRTMLResponse = RMITeaConnectionHandler.getInstance().handleRequest(rtmlDocument);
			}
			else if (rtmlDocument.isAbort())
			{
				traceLogger.log(2, this.getClass().getName(), "... document type is abort");
				teaRTMLResponse = RMITeaConnectionHandler.getInstance().handleAbort(rtmlDocument);
			}
			else
			{
				errorLogger.log(1, this.getClass().getName(),"Unknown document request type.");
				throw new Exception("Unknown document request type.");
			}
			//return the rtml document
			return teaRTMLResponse;
		} 
		catch (Exception e) 
		{
			RTMLDocument errorDocument = RTMLUtil.createErrorDocument(e, nodeAgentName, rtmlDocumentString);
			logRTMLDocument(errorDocument);
			return  errorDocument;
		}
	}
	
	/**
	 * Method to log and RTML document to the errorLogger.
	 * @param errorDocument The error document to log.
	 * @see org.estar.node_agent2.util.RTMLUtil#getRTMLAsString
	 * @see #traceLogger
	 * @see #errorLogger
	 */
	protected void logRTMLDocument(RTMLDocument errorDocument) 
	{
		String rtmlString = "";

		try 
		{
			rtmlString = RTMLUtil.getRTMLAsString(errorDocument);
		} 
		catch (Exception e) 
		{
			traceLogger.log(5, this.getClass().getName(), "failed to log String version of RTMLDocument!");
			e.printStackTrace();
		}
		errorLogger.log(1, this.getClass().getName(), rtmlString);
	}

	/**
	 * Check the Username and Password supplied in the SOAP headers are legal.
	 * @return The extracted username from the request header.
	 * @see #wsctx
	 * @exception RuntimeException Thrown if the username is not known, or the password is incorrect.
	 */
	protected String checkUsernamePassword() throws RuntimeException
	{
		MessageContext mctx = wsctx.getMessageContext();
		PersistentMap passwordMapStore = null;
	    String headerUsername = null;
	    String headerPassword = null;
	    String savedPassword = null;
	    
		//get detail from request headers
	    Map http_headers = (Map) mctx.get(MessageContext.HTTP_REQUEST_HEADERS);
	    List userList = (List) http_headers.get("Username");
	    List passList = (List) http_headers.get("Password");
	    
        //get username and password from SOAP headers
	    if(userList!=null)
	    {
	      	headerUsername = userList.get(0).toString();
	    }
	    if(passList!=null)
	    {
	        headerPassword = passList.get(0).toString();
	    }
	    if(headerUsername ==null)
	    {
     		errorLogger.log(1, this.getClass().getName(),"checkUsernamePassword:Failed to find username in headers.");
			throw new RuntimeException(this.getClass().getName()+
					":checkUsernamePassword:Failed to find username in headers.");
	    }	    	
	    if(headerPassword ==null)
	    {
     		errorLogger.log(1, this.getClass().getName(),"checkUsernamePassword:Failed to find password in headers for username:"+headerUsername);
			throw new RuntimeException(this.getClass().getName()+
					":checkUsernamePassword:Failed to find password in headers for username:"+headerUsername);
	    }	    	
	    // get password for the supplied username from the persistent store
		passwordMapStore = PersistenceController.getInstance().getPasswordMapStore();
		savedPassword = passwordMapStore.getProperty(headerUsername);
	    if(savedPassword ==null)
	    {
     		errorLogger.log(1, this.getClass().getName(),
     				"checkUsernamePassword:Failed to find password for username:"+headerUsername+
     				" in persistent store.");
			throw new RuntimeException(this.getClass().getName()+
					":checkUsernamePassword:Failed to find password for username:"+headerUsername+
					" in persistent store.");
	    }
	    if(headerPassword.equals(savedPassword))
	    {
           		traceLogger.log(2, this.getClass().getName(),"checkUsernamePassword:Password for username "+headerUsername+" is correct.");
       	}
        else
        {
      		traceLogger.log(1, this.getClass().getName(),"checkUsernamePassword:Password for username "+headerUsername+" is NOT correct ("+headerPassword+" vs "+savedPassword+").");
     		errorLogger.log(1, this.getClass().getName(),"checkUsernamePassword:Password for username "+headerUsername+" is NOT correct ("+headerPassword+" vs "+savedPassword+").");
        	throw new RuntimeException(this.getClass().getName()+
        			":checkUsernamePassword:Incorrect Password for User:"+headerUsername);
        }
	    return headerUsername;
	}
	
	/**
	 * Check the username in the SOAP request headers (after unaliasing) match the username in the RTML document 
	 * (after unaliasing). 
	 * @param headerUsername The SOAP request header username.
	 * @param rtmlDocument The RTML document, after aliases have been rewritten.
	 * @throws Exception Thrown if the usernames do not match.
	 */
	protected void checkUsernamesMatch(String headerUsername,RTMLDocument rtmlDocument) throws Exception
	{
		RTMLContact contact = null;
		PersistentMap userAliasMapStore = null;
		String rtmlUsername = null;
		String unaliasedHeaderUsername = null;
		
		// get the RTML Contact User(name)
		contact = rtmlDocument.getContact();
		if (contact == null) 
		{
			throw new Exception("checkUsernamesMatch:No Contact in received document");
		}
		rtmlUsername = contact.getUser();
		// get the user alias information
		userAliasMapStore = PersistenceController.getInstance().getUserAliasMapStore();
		if (userAliasMapStore == null) 
		{
			throw new Exception("checkUsernamesMatch:Serverside error: user alias map store is null");
		}
		// unalias the header username, if required
		unaliasedHeaderUsername = headerUsername;
		if (userAliasMapStore.containsKey(headerUsername)) 
			unaliasedHeaderUsername = userAliasMapStore.getProperty(headerUsername);
		// Compare rtmlUsername and unaliasedHeaderUsername
 		traceLogger.log(1, this.getClass().getName(),
 				"checkUsernamesMatch:Unaliased Header Username "+unaliasedHeaderUsername+".");
		traceLogger.log(1, this.getClass().getName(),
				"checkUsernamesMatch:Unaliased RTML Username "+rtmlUsername+".");
		if(rtmlUsername == null)
		{
			throw new Exception("checkUsernamesMatch:RTML username was null.");
		}
		if(unaliasedHeaderUsername == null)
		{
			throw new Exception("checkUsernamesMatch:The unaliased SOAP header username was null.");
		}
		if(unaliasedHeaderUsername.equals(rtmlUsername) == false)
		{
			traceLogger.log(1, this.getClass().getName(),
					"checkUsernamesMatch:Unaliased Header Username "+unaliasedHeaderUsername+
					" does not match RTML username "+rtmlUsername+".");
			errorLogger.log(1, this.getClass().getName(),"checkUsernamesMatch:Unaliased Header Username "+unaliasedHeaderUsername+
					" does not match RTML username "+rtmlUsername+".");
			throw new Exception("checkUsernamesMatch:Unaliased Header Username "+unaliasedHeaderUsername+
					" does not match RTML username "+rtmlUsername+".");
		}
		traceLogger.log(1, this.getClass().getName(),
				"checkUsernamesMatch:Unaliased Header Username "+unaliasedHeaderUsername+
				" matchs RTML username "+rtmlUsername+".");		
	}
}
