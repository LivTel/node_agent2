package org.estar.node_agent2.messaging;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;

import ngat.util.logging.LogManager;
import ngat.util.logging.Logger;

import org.estar.node_agent2.configuration.NodeAgentProperties;
import org.estar.node_agent2.util.LoggerUtil;

import org.estar.rtml.RTMLDocument;

import org.estar.tea.EmbeddedAgentRequestHandler;
import org.estar.tea.EmbeddedAgentTestHarness;
import org.estar.tea.NodeAgentAsynchronousResponseHandler;
import org.estar.tea.TelescopeAvailability;
import org.estar.tea.TelescopeAvailabilityPredictor;

/**
 * This singleton class used to invoke 'handle' RTML  calls on the TEA.
 * It is initialised by the InitialisationServlet.
 */
public class RMITeaConnectionHandler
{
	/**
	 * Logging logger.
	 * @see ngat.util.logging.LogManager#getLogger
	 * @see org.estar.util.LoggerUtil#TRACE_LOGGER_NAME
	 */
	static Logger traceLogger = LogManager.getLogger(LoggerUtil.TRACE_LOGGER_NAME);
	/**
	 * Error logger.
	 * @see ngat.util.logging.LogManager#getLogger
	 * @see org.estar.util.LoggerUtil#ERROR_LOGGER_NAME
	 */
	static Logger errorLogger = LogManager.getLogger(LoggerUtil.ERROR_LOGGER_NAME);
	/**
	 * The constructed singleton instance of this class.
	 */
	private static RMITeaConnectionHandler instance = null;

	//instance variables
	private java.util.Date instantiationTimeStamp;	
	private static String embeddedAgentRequestHandlerURL;
	private static String telescopeAvailabilityPredictorRequestHandlerURL;
	
	//private EmbeddedAgentRequestHandler earh;
	private TelescopeAvailabilityPredictor tap;
	private RMIBindingPersistorRunnable persistenceRunnable;
	
	/**
	 * Return the singleton instance of the TeaConnectionHandler.
	 * @see #instance
	 */
	public static RMITeaConnectionHandler getInstance()
	{
		if (instance == null) 
		{
			instance = new RMITeaConnectionHandler();
		}
		return instance;
	}
	/**
	 * Constructor.
	 * <ul>
	 * <li>Logs construction to traceLogger.
	 * <li>Constructs embeddedAgentRequestHandlerURL from NodeAgentProperties.
	 * <li>Constructs telescopeAvailabilityPredictorRequestHandlerURL from NodeAgentProperties.
	 * <li>Creates an instance of DefaultNodeAgentAsynchronousResponseHandler.
	 * <li>Creates an instance of RMIBindingPersistorRunnable to run the instance of 
	 *     DefaultNodeAgentAsynchronousResponseHandler in a thread.
	 * </ul>
	 * @see #traceLogger
	 * @see #embeddedAgentRequestHandlerURL
	 * @see #telescopeAvailabilityPredictorRequestHandlerURL
	 * @see DefaultNodeAgentAsynchronousResponseHandler
	 * @see RMIBindingPersistorRunnable
	 * @see org.estar.configuration.NodeAgentProperties#RMI_PREFIX
	 * @see org.estar.configuration.NodeAgentProperties#TEA_HOST_NAME_PROPERTY
	 * @see org.estar.configuration.NodeAgentProperties#TEA_REQUEST_HANDLER_RMI_NAME
	 * @see org.estar.configuration.NodeAgentProperties#TEA_AVAILABILITY_PREDICTOR_RMI_NAME
	 * @see org.estar.configuration.NodeAgentProperties#NODE_AGENT_RESPONSE_HANDLER_RMI_NAME
	 * @see org.estar.configuration.NodeAgentProperties#getInstance
	 */
	private RMITeaConnectionHandler() 
	{
		traceLogger.log(5, RMITeaConnectionHandler.class.getName(), "... instantiating RMITeaConnectionHandler"); 
		
		embeddedAgentRequestHandlerURL 	= NodeAgentProperties.RMI_PREFIX;
		embeddedAgentRequestHandlerURL  += NodeAgentProperties.getInstance().
			getProperty(NodeAgentProperties.TEA_HOST_NAME_PROPERTY);
		embeddedAgentRequestHandlerURL  += "/" + NodeAgentProperties.getInstance().
			getProperty(NodeAgentProperties.TEA_REQUEST_HANDLER_RMI_NAME);
		
		telescopeAvailabilityPredictorRequestHandlerURL 	= NodeAgentProperties.RMI_PREFIX;
		telescopeAvailabilityPredictorRequestHandlerURL  += NodeAgentProperties.getInstance().
			getProperty(NodeAgentProperties.TEA_HOST_NAME_PROPERTY);
		telescopeAvailabilityPredictorRequestHandlerURL  += "/" + NodeAgentProperties.getInstance().
			getProperty(NodeAgentProperties.TEA_AVAILABILITY_PREDICTOR_RMI_NAME);
		
		String dnaarhBindingName =  NodeAgentProperties.getInstance().
			getProperty(NodeAgentProperties.NODE_AGENT_RESPONSE_HANDLER_RMI_NAME);
		try 
		{
			//bind and keep alive an asychronous response handler
			DefaultNodeAgentAsynchronousResponseHandler aRH = new DefaultNodeAgentAsynchronousResponseHandler();
			persistenceRunnable = new RMIBindingPersistorRunnable(dnaarhBindingName, aRH);
			new Thread(persistenceRunnable).start();
			
		} 
		catch (MalformedURLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (RemoteException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		instantiationTimeStamp = new Date();	
	}
	
	/**
	 * Method to handle a score request RTML document.
	 * <ul>
	 * <li>We lookup the embedded agent request handler (RMI interface) using Naming.lookup
	 *     on the embeddedAgentRequestHandlerURL.
	 * <li>We invoke the embedded agent request handler RMI method handleScore, with the supplied RTML document.
	 *     and return the reply document.
	 * </ul>
	 * @param rtmlDocument A document object model containing the RTML document to score.
	 * @return An instance of RTMLDocument containing the reply from TEA's handleScore RMI method.
	 * @see #traceLogger
	 * @see #embeddedAgentRequestHandlerURL
	 * @see org.estar.tea.EmbeddedAgentRequestHandler
	 * @see org.estar.tea.EmbeddedAgentRequestHandler#handleScore
	 */
	public synchronized RTMLDocument handleScore(RTMLDocument rtmlDocument) throws MalformedURLException, 
										       RemoteException, NotBoundException
	{
		traceLogger.log(5, RMITeaConnectionHandler.class.getName(), "handleScore() invoked");
		RTMLDocument replyDocument = null;
		
		//locate reference to EmbeddedAgentRequestHandler (in the TEA)
		traceLogger.log(5, RMITeaConnectionHandler.class.getName(),
				"looking up EmbeddedAgentRequestHandler on TEA using URL: "+
				embeddedAgentRequestHandlerURL);
		EmbeddedAgentRequestHandler earh = (EmbeddedAgentRequestHandler)Naming.lookup(
										embeddedAgentRequestHandlerURL);
		traceLogger.log(5, RMITeaConnectionHandler.class.getName(), "... located EmbeddedAgentRequestHandler: "+
				earh);
		
		//invoke required method on earh
		traceLogger.log(5, RMITeaConnectionHandler.class.getName(), 
				"... calling EmbeddedAgentRequestHandler.handleScore(rtmlDocument)");
		replyDocument = earh.handleScore(rtmlDocument);

		return replyDocument;
	}

	/**
	 * Method to handle a request RTML document.
	 * <ul>
	 * <li>We lookup the embedded agent request handler (RMI interface) using Naming.lookup
	 *     on the embeddedAgentRequestHandlerURL.
	 * <li>We invoke the embedded agent request handler RMI method handleRequest, with the supplied RTML document.
	 *     and return the reply document.
	 * </ul>
	 * @param rtmlDocument A document object model containing the RTML document to process.
	 * @return An instance of RTMLDocument containing the reply from TEA's handleRequest RMI method.
	 * @see #traceLogger
	 * @see #embeddedAgentRequestHandlerURL
	 * @see org.estar.tea.EmbeddedAgentRequestHandler
	 * @see org.estar.tea.EmbeddedAgentRequestHandler#handleRequest
	 */
	public synchronized RTMLDocument handleRequest(RTMLDocument rtmlDocument) throws MalformedURLException, 
										    RemoteException, NotBoundException 
	{
		traceLogger.log(5, RMITeaConnectionHandler.class.getName(), "handleRequest() invoked");
		RTMLDocument replyDocument = null;
		
		//locate reference to EmbeddedAgentRequestHandler (in the TEA)
		traceLogger.log(5, RMITeaConnectionHandler.class.getName(),
				"looking up EmbeddedAgentRequestHandler on TEA using URL: "+
				embeddedAgentRequestHandlerURL);
		EmbeddedAgentRequestHandler earh = (EmbeddedAgentRequestHandler)Naming.lookup(
										embeddedAgentRequestHandlerURL);
		
		traceLogger.log(5, RMITeaConnectionHandler.class.getName(), "... located EmbeddedAgentRequestHandler: "+
				earh);
		
		//invoke required method on earh
		traceLogger.log(5, RMITeaConnectionHandler.class.getName(), 
				"... calling EmbeddedAgentRequestHandler.handleRequest(rtmlDocument)");
		replyDocument = earh.handleRequest(rtmlDocument);

		return replyDocument;
	}
	
	/**
	 * Method to handle an abort RTML document.
	 * <ul>
	 * <li>We lookup the embedded agent request handler (RMI interface) using Naming.lookup
	 *     on the embeddedAgentRequestHandlerURL.
	 * <li>We invoke the embedded agent request handler RMI method handleAbort, with the supplied RTML document.
	 *     and return the reply document.
	 * </ul>
	 * @param rtmlDocument A document object model containing the RTML document to abort.
	 * @return An instance of RTMLDocument containing the reply from TEA's handleAbort RMI method.
	 * @see #traceLogger
	 * @see #embeddedAgentRequestHandlerURL
	 * @see org.estar.tea.EmbeddedAgentRequestHandler
	 * @see org.estar.tea.EmbeddedAgentRequestHandler#handleAbort
	 */
	public synchronized RTMLDocument handleAbort(RTMLDocument rtmlDocument) throws MalformedURLException, 
    RemoteException, NotBoundException 
    {
		traceLogger.log(5, RMITeaConnectionHandler.class.getName(), "handleAbort() invoked");
		RTMLDocument replyDocument = null;

		//locate reference to EmbeddedAgentRequestHandler (in the TEA)
		traceLogger.log(5, RMITeaConnectionHandler.class.getName(),
				"looking up EmbeddedAgentRequestHandler on TEA using URL: "+
				embeddedAgentRequestHandlerURL);
		EmbeddedAgentRequestHandler earh = (EmbeddedAgentRequestHandler)Naming.lookup(
				embeddedAgentRequestHandlerURL);

		traceLogger.log(5, RMITeaConnectionHandler.class.getName(), "... located EmbeddedAgentRequestHandler: "+
earh);

		//invoke required method on earh
		traceLogger.log(5, RMITeaConnectionHandler.class.getName(), 
				"... calling EmbeddedAgentRequestHandler.handleAbort(rtmlDocument)");
		replyDocument = earh.handleAbort(rtmlDocument);

		return replyDocument;
    }
	
	/**
	 * Method to handle an update RTML document.
	 * <ul>
	 * <li>We lookup the embedded agent request handler (RMI interface) using Naming.lookup
	 *     on the embeddedAgentRequestHandlerURL.
	 * <li>We invoke the embedded agent request handler RMI method handleUpdate, with the supplied RTML document.
	 *     and return the reply document.
	 * </ul>
	 * @param rtmlDocument A document object model containing the RTML document to provide an update for.
	 * @return An instance of RTMLDocument containing the reply from TEA's handleUpdate RMI method.
	 * @see #traceLogger
	 * @see #embeddedAgentRequestHandlerURL
	 * @see org.estar.tea.EmbeddedAgentRequestHandler
	 * @see org.estar.tea.EmbeddedAgentRequestHandler#handleUpdate
	 */
	public synchronized RTMLDocument handleUpdate(RTMLDocument rtmlDocument) throws MalformedURLException, 
    RemoteException, NotBoundException 
    {
		traceLogger.log(5, RMITeaConnectionHandler.class.getName(), "handleUpdate() invoked");
		RTMLDocument replyDocument = null;

		//locate reference to EmbeddedAgentRequestHandler (in the TEA)
		traceLogger.log(5, RMITeaConnectionHandler.class.getName(),
				"looking up EmbeddedAgentRequestHandler on TEA using URL: "+
				embeddedAgentRequestHandlerURL);
		EmbeddedAgentRequestHandler earh = (EmbeddedAgentRequestHandler)Naming.lookup(
				embeddedAgentRequestHandlerURL);

		traceLogger.log(5, RMITeaConnectionHandler.class.getName(), "... located EmbeddedAgentRequestHandler: "+
earh);

		//invoke required method on earh
		traceLogger.log(5, RMITeaConnectionHandler.class.getName(), 
				"... calling EmbeddedAgentRequestHandler.handleUpdate(rtmlDocument)");
		replyDocument = earh.handleUpdate(rtmlDocument);

		return replyDocument;
    }
	
	public void destroy() {
		persistenceRunnable.stop();
	}
	
	public String toString() {
		return this.getClass().getName() + " [instantiationTimeStamp:" + instantiationTimeStamp + "]";
	}
	
	
}

/**
 * Setup an RMI binding from the specified bind name to the specified object, and try
 * to rebind when the binding has become unbound.
 */
class RMIBindingPersistorRunnable implements Runnable 
{
	/**
	 * The object bound to the bindName.
	 */
	private Remote object;
	/**
	 * A string representing the bind name.
	 */
	private String bindName;
	/**
	 * A boolean, the run method keeps trying to rebind whilst this is true, when
	 * it is set to false the thread terminates.
	 */
	private volatile boolean shouldPersist;
	private static Date bindTime;
       	/**
	 * Logging logger.
	 * @see ngat.util.logging.LogManager#getLogger
	 * @see org.estar.util.LoggerUtil#TRACE_LOGGER_NAME
	 */
	static Logger traceLogger = LogManager.getLogger(LoggerUtil.TRACE_LOGGER_NAME);
	/**
	 * Error logger.
	 * @see ngat.util.logging.LogManager#getLogger
	 * @see org.estar.util.LoggerUtil#ERROR_LOGGER_NAME
	 */
	static Logger errorLogger = LogManager.getLogger(LoggerUtil.ERROR_LOGGER_NAME);

	/**
	 * Constructor.
	 */
	public RMIBindingPersistorRunnable(String bindName, Remote object) throws RemoteException, 
										  MalformedURLException 
	{
		traceLogger.log(5, RMIBindingPersistorRunnable.class.getName(), 
				"new RMIBindingPeristanceThread(" + bindName + "," + object.getClass().getName() + 
				"[" + object  + "] )");
		this.object= object;
		this.bindName = bindName;
	}

	/**
	 * Cause the thread to wait for a specified number of milliseconds. 
	 * The wait is done on an internally generated object.
	 */
	private void delay(int msD) 
	{
		Object waiter = new Object();
		synchronized(waiter) 
		{ 
			try 
			{ 
				waiter.wait(msD); 
			} 
			catch (InterruptedException e)  
			{} 
		}
	}

	/**
	 * Stop the thread running, by setting shouldPersist to false.
	 * @see #shouldPersist
	 */
	public void stop() 
	{
		shouldPersist = false;
	}

	/**
	 * run method for the thread.
	 * <ul>
	 * <li>We call Naming.rebind to bind object to the RMI handler described in bindName.
	 *     If the initial binding fails we log an error and stop the method.
	 * <li>We set bindTime to now.
	 * <li>We set shouldPersist to true.
	 * <li>While shouldPersist is true:
	 *     <ul>
	 *     <li>We delay for 1 minute.
	 *     <li>We do a Naming.lookup on the bindName, to see if the RMI handler is still bound.
	 *     <li>If the RMI handler is no longer bound (i.e. a NotBoundException is caught) we:
	 *         <ul>
	 *         <li>We call Naming.rebind to bind object to the RMI handler described in bindName.
	 *             If the binding fails we log an error and stop the method (set shouldPersist to false).
	 *         </ul>
	 *     </ul>
	 * <ul>
	 * @see #delay
	 * @see #traceLogger
	 * @see #bindName
	 * @see #object
	 * @see #bindTime
	 * @see #shouldPersist
	 */
	public void run() 
	{
		traceLogger.log(5, RMIBindingPersistorRunnable.class.getName(), "... run()");

		// try to initially bind the name to the object
		try 
		{
			traceLogger.log(5, RMIBindingPersistorRunnable.class.getName(), "... doing initial rebind");
			traceLogger.log(5, RMIBindingPersistorRunnable.class.getName(), "... Naming.rebind(" + 
					bindName + ", " + object.getClass().getName() + "[" + object + "] );");
			Naming.rebind(bindName, object);
			bindTime = new Date();
			traceLogger.log(5, RMIBindingPersistorRunnable.class.getName(), 
					"... ... rebind successful at " + bindTime);
		} 
		catch (Exception e1) 
		{
			errorLogger.log(1, RMIBindingPersistorRunnable.class.getName(), 
					"initial rebind failed, halting RMIBindingPersistorRunnable!");
			e1.printStackTrace();
			return;
		} 
		
		// Check the binding still exists, if it doesn't try to rebind it.
		shouldPersist = true;
		while (shouldPersist) 
		{
			delay(60000);
			try 
			{
				Naming.lookup(bindName);
			} 
			catch (NotBoundException e) 
			{
				//Not bound - log the fact and rebind 
				Date nowD = new Date();
				errorLogger.log(1, RMITeaConnectionHandler.class.getName(), bindName + 
						" found to be unbound at " +nowD + 
						" (last bind was at " + bindTime + ")");
				try 
				{
					Naming.rebind(bindName, object);
					bindTime = new Date();
					traceLogger.log(5, RMIBindingPersistorRunnable.class.getName(), 
							"... ... rebind successful at " + bindTime);
				} 
				catch (Exception e1) 
				{
					errorLogger.log(1, RMIBindingPersistorRunnable.class.getName(), 
				       "rebind failed after NotBoundException, halting RMIBindingPersistorRunnable!");
					e1.printStackTrace();
					shouldPersist = false;
					return;
				} 
			} 
			catch (Exception e) 
			{
				errorLogger.log(1, RMIBindingPersistorRunnable.class.getName(), 
			   "lookup failed but no NotBoundException was thrown, halting RMIBindingPersistorRunnable!");
				e.printStackTrace();
				shouldPersist = false;
				return;
			}
		}
		traceLogger.log(5, RMIBindingPersistorRunnable.class.getName(), 
				"shouldPersist = false, exiting run cycle");
	}
		
}
