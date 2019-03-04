package org.estar.node_agent2.initialisation;
import javax.servlet.http.HttpServlet;
import ngat.util.logging.LogManager;
import ngat.util.logging.Logger;

import org.estar.node_agent2.configuration.NodeAgentProperties;
import org.estar.node_agent2.messaging.RMITeaConnectionHandler;
import org.estar.node_agent2.util.LoggerUtil;

/**
 * Serlet started from web.xml that initialises property loading, RMI conencted to the TEA,
 * and logging.
 * @author cjm
 */
public class InitialisationServlet extends HttpServlet
{
	/**
	 * Class variable, logger used for logging.
	 */
	static Logger traceLogger = null;
	/**
	 * Class variable, logger used for errors.
	 */
	static Logger errorLogger = null;
	/**
	 * Constructor for the initialisation servlet.
	 * <ul>
	 * <li>Setup the loggers.
	 * </ul>
	 * @see #traceLogger
	 * @see #errorLogger
	 * @see org.estar.node_agent2.util.LoggerUtil#setUpLoggers
	 * @see org.estar.node_agent2.util.LoggerUtil#TRACE_LOGGER_NAME
	 * @see org.estar.node_agent2.util.LoggerUtil#ERROR_LOGGER_NAME
	 * @see ngat.util.logging.LogManager
	 * @see ngat.util.logging.LogManager#getLogger
	 * 
	 */
	public InitialisationServlet() 
	{
		LoggerUtil.setUpLoggers();
		
		traceLogger  = LogManager.getLogger(LoggerUtil.TRACE_LOGGER_NAME);
		errorLogger  = LogManager.getLogger(LoggerUtil.ERROR_LOGGER_NAME);
	}
	
	/**
	 * Initialise the TEA connectyion handler, if configured to do so.
	 * <ul>
	 * <li>Get an instance of NodeAgentProperties.
	 * <li>If the IS_TEA_CONNECTED property is true, we get an instance of RMITeaConnectionHandler.
	 * </ul>
	 * @see #traceLogger
	 * @see org.estar.node_agent2.configuration.NodeAgentProperties
	 * @see org.estar.node_agent2.configuration.NodeAgentProperties#getInstance
	 * @see org.estar.node_agent2.configuration.NodeAgentProperties#IS_TEA_CONNECTED
	 * @see org.estar.noe_agent2.messaging.RMITeaConnectionHandler
	 * @see org.estar.noe_agent2.messaging.RMITeaConnectionHandler#getInstance
	 */
	public void init() throws javax.servlet.ServletException 
	{
		traceLogger.log(5,this.getClass().getName(), ".init()");
		//if isLive property is set, create the connection to the TEA
		try 
		{
			NodeAgentProperties nodeAgentProperties = NodeAgentProperties.getInstance();
			if (nodeAgentProperties != null) 
			{
				if (!nodeAgentProperties.getProperty(NodeAgentProperties.IS_TEA_CONNECTED).equalsIgnoreCase(NodeAgentProperties.TRUE))
				{
					traceLogger.log(1, InitialisationServlet.class.getName(), 
							"... " + NodeAgentProperties.IS_TEA_CONNECTED + " = false, will not connect RMITeaConnectionHandler on request receipt");
				}
				else
				{
					traceLogger.log(5, InitialisationServlet.class.getName(), 
							"..." + NodeAgentProperties.IS_TEA_CONNECTED + " = true");
					traceLogger.log(5, InitialisationServlet.class.getName(), 
							"... loading RMITeaConnectionHandler instance");
					RMITeaConnectionHandler.getInstance();
				}
			} 
			else
			{
				errorLogger.log(1, InitialisationServlet.class.getName(), 
							"... nodeAgentProperties = null!");
			}
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Method called at destruction. Destroys the RMITeaConnectionHandler instance.
	 * @see #traceLogger
	 * @see org.estar.noe_agent2.messaging.RMITeaConnectionHandler
	 * @see org.estar.noe_agent2.messaging.RMITeaConnectionHandler#getInstance
	 */
	public void destroy() 
	{
		try 
		{
			traceLogger.log(5, InitialisationServlet.class.getName(), ".destroy()");
			RMITeaConnectionHandler.getInstance().destroy();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

}
