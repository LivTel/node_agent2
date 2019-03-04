package org.estar.node_agent2.messaging;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;

import ngat.util.logging.LogManager;
import ngat.util.logging.Logger;

import org.estar.node_agent2.configuration.NodeAgentProperties;
import org.estar.node_agent2.util.LoggerUtil;
import org.estar.node_agent2.util.RTMLUtil;
import org.estar.rtml.RTMLDocument;
import org.estar.rtml.RTMLIntelligentAgent;
import org.estar.tea.NodeAgentAsynchronousResponseHandler;

/**
 * receives a rtml update document from the TEA and attempts to return it to the relevant IA
 * using an instance of IntelligentAgentMessagingClient
 * @author nrc, cjm
 */
public class DefaultNodeAgentAsynchronousResponseHandler extends UnicastRemoteObject implements NodeAgentAsynchronousResponseHandler
{
	protected DefaultNodeAgentAsynchronousResponseHandler() throws RemoteException {
		super();
	}

	static Logger traceLogger = LogManager.getLogger(LoggerUtil.TRACE_LOGGER_NAME);
	static Logger errorLogger = LogManager.getLogger(LoggerUtil.ERROR_LOGGER_NAME);
	
	Date instantiationDate = new Date();

	/**
	 * Invoked by the TEA using RMI 
	 */
	public void handleAsyncResponse(RTMLDocument rtmlDocument) throws RemoteException 
	{
		String documentType = rtmlDocument.getType();
		if (documentType == null) 
		{
			documentType = rtmlDocument.getMode();
		}
		
		traceLogger.log(5, DefaultNodeAgentAsynchronousResponseHandler.class.getName(), "received ASYCHRONOUS RTML : " +documentType);
		RTMLIntelligentAgent returnAgent;

		//don't try to send a null returnAgent onwards - it was probably entered via the commandline client
		returnAgent =rtmlDocument.getIntelligentAgent();
		if (returnAgent == null) 
		{
			traceLogger.log(5, DefaultNodeAgentAsynchronousResponseHandler.class.getName(), "NodeAgent reports: intelligent agent in received document was null, unable to send document to Agent");
			throw new RemoteException("NodeAgent reports: intelligent agent in received document was null, unable to send document to Agent");
		}
		
		if (rtmlDocument.getVersion().equals(RTMLDocument.RTML_VERSION_22)) 
		{
			traceLogger.log(5, DefaultNodeAgentAsynchronousResponseHandler.class.getName(), "... document has version 2.2" );
			if (returnAgent.getHostname() == null) 
			{
				traceLogger.log(5, DefaultNodeAgentAsynchronousResponseHandler.class.getName(), "NodeAgent reports: hostname in received document was null, unable to send document to Agent");
				throw new RemoteException("NodeAgent reports: hostname in received document was null, unable to send document to Agent");
			}
			if (returnAgent.getPort() == 0) 
			{
				traceLogger.log(5, DefaultNodeAgentAsynchronousResponseHandler.class.getName(), "NodeAgent reports: port in received document was null, unable to send document to Agent");
				throw new RemoteException("NodeAgent reports: port in received document was null, unable to send document to Agent");
			}
		} 
		else if (rtmlDocument.getVersion().equals(RTMLDocument.RTML_VERSION_31)) 
		{
			traceLogger.log(5, DefaultNodeAgentAsynchronousResponseHandler.class.getName(), "... document has version 3.1" );
			if (returnAgent.getUri() == null) 
			{
				traceLogger.log(5, DefaultNodeAgentAsynchronousResponseHandler.class.getName(), "NodeAgent reports: uri in intelligent agent in received document was null, unable to send document to Agent");
				throw new RemoteException("NodeAgent reports: uri in intelligent agent in received document was null, unable to send document to Agent");
			} 
			if (returnAgent.getUri().equals(NodeAgentProperties.NULL_RETURN_PATH)) 
			{
				//the document has a return path of file:/dev/null - just drop it and pretend to the TEA that everything was fine.
				traceLogger.log(5, DefaultNodeAgentAsynchronousResponseHandler.class.getName(), "... document has return path of " +returnAgent.getHostname() +" dumping document");
				return; 
			}
			if (returnAgent.getHostname() == null) 
			{
				//the document has a return agent that is null - just drop it and pretend to the TEA that everything was fine.
				traceLogger.log(5, DefaultNodeAgentAsynchronousResponseHandler.class.getName(), "... document has return agent of " +returnAgent.getHostname() +" dumping document");
				return;
			}
			//********** VALID DOCUMENT ***********
			//we can't get away with simply dumping the document. Keep pressing on.
			traceLogger.log(5, DefaultNodeAgentAsynchronousResponseHandler.class.getName(), "... document has return path of" +returnAgent.getHostname() +" NOT dumping document");
		} 
		else 
		{
			// *********** UNKNOWN VERSION NUMBER ************
			traceLogger.log(5, DefaultNodeAgentAsynchronousResponseHandler.class.getName(), "NodeAgent reports: unknown rtml document version: " +rtmlDocument.getVersion());
			throw new RemoteException("NodeAgent reports: unknown rtml document version: " +rtmlDocument.getVersion());
		}

		try 
		{
			//rewrite the ngat project and user alias's back to estar project and user ID's
			rtmlDocument = RTMLUtil.rewriteDocumentIfAliased(rtmlDocument, RTMLUtil.RTML_IDENT_SOURCE_NGAT);
		} 
		catch (Exception e) 
		{
			errorLogger.log(1, DefaultNodeAgentAsynchronousResponseHandler.class.getName(), e);
			String m = e.getMessage() +", NodeAgent failed to rewrite alias correctly for document : " +returnAgent.getId();
			throw new RemoteException(m);
		}
		
		String returnAgentURL = "";
		if (rtmlDocument.version == RTMLDocument.RTML_VERSION_22) 
		{
			returnAgentURL = returnAgent.getHostname() +":" +returnAgent.getPort();
		} 
		else 
		{
			returnAgentURL = rtmlDocument.getIntelligentAgent().getUri();
		}
		
		try 
		{
			traceLogger.log(5, DefaultNodeAgentAsynchronousResponseHandler.class.getName(), "... sending document to client");
			IAAsynchronousMessagingClient intelligentAgentMessagingClient = new IAAsynchronousMessagingClient();
			intelligentAgentMessagingClient.sendRTMLUpdateToIA(rtmlDocument);
		}
		catch (Exception e) 
		{
			String m = "NodeAgent failed to send asychronous RTML update to IA at " + returnAgentURL + " for document " + returnAgent.getId();
			errorLogger.log(1, DefaultNodeAgentAsynchronousResponseHandler.class.getName(), m);
			e.printStackTrace();
			throw new RemoteException(m);
		}
	}
	
	public String ping() 
	{
		return this.getClass().getName() +" instantiationDate=" + instantiationDate;
	}
}
