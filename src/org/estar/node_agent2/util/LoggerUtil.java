package org.estar.node_agent2.util;
import java.io.IOException;

import ngat.util.logging.BogstanLogFormatter;
import ngat.util.logging.ConsoleLogHandler;
import ngat.util.logging.DatagramLogHandler;
import ngat.util.logging.LogManager;
import ngat.util.logging.Logger;
import ngat.util.logging.Logging;

import org.estar.node_agent2.configuration.NodeAgentProperties;

/**
 * Logging utility class.
 * @author cjm
 */
public class LoggerUtil 
{
	/**
	 * Name used for the TRACE logger.
	 */
	public static final String TRACE_LOGGER_NAME = "TRACE";
	/**
	 * Name used for the ERROR logger.
	 */
	public static final String ERROR_LOGGER_NAME = "ERROR";

	/**
	 * Method invoked from the initialisation servlet to setup the logging.
	 */
	public static void setUpLoggers() 
	{
		NodeAgentProperties nodeAgentProperties = NodeAgentProperties.getInstance();

		ConsoleLogHandler console = new ConsoleLogHandler(new BogstanLogFormatter());
		console.setLogLevel(Logger.ALL);

		Logger traceLogger = LogManager.getLogger(TRACE_LOGGER_NAME);
		traceLogger.setLogLevel(Logger.ALL);
		traceLogger.addHandler(console);
		
		Logger errorLogger = LogManager.getLogger(ERROR_LOGGER_NAME);
		errorLogger.setLogLevel(Logger.ALL);
		errorLogger.addHandler(console);

		
		errorLogger.log(5, LoggerUtil.class.getName(),"errorLogger initialised");
		
		traceLogger.log(5, LoggerUtil.class.getName(),"traceLogger initialised");
	}
}
