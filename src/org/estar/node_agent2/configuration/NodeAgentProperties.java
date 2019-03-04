package org.estar.node_agent2.configuration;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import ngat.util.logging.LogManager;
import ngat.util.logging.Logger;

import org.estar.node_agent2.util.LoggerUtil;

/**
 * Class holding configuration information from the /etc/nodeagent property files.
 * @author cjm
 */
public class NodeAgentProperties extends Properties
{
	/**
	 * Base directory containing the property files: /etc/nodeagent.
	 */
	public static final String BASE_DIR                             = "/etc/nodeagent";

	public static final String NODE_AGENT_NAME 						= "nodeagent.name";
	public static final String IS_TEA_CONNECTED 					= "isteaconnected";
	public static final String TEA_HOST_NAME_PROPERTY 				= "tea.hostname";
	public static final String TEA_REQUEST_HANDLER_RMI_NAME			= "tea.request.handler.rmi.name";
	public static final String TEA_AVAILABILITY_PREDICTOR_RMI_NAME	= "tea.availability.predictor.rmi.name";
	
	public static final String NODE_AGENT_RESPONSE_HANDLER_RMI_NAME = "nodeagent.response.handler.rmi.name";
	public static final String NODE_AGENT_PORT						= "nodeagent.port";
	
	private static final String PROPERTIES_FILE_PATH 				= BASE_DIR + "/server.configuration";
	public static final String HASH_STORAGE_FILE_PATH 				= BASE_DIR + "/rtml.hashstoragefile";
	public static final String USERALIAS_MAP_LOCATION 				= BASE_DIR + "/useralias.map";
	public static final String PROJECTALIAS_MAP_LOCATION 			= BASE_DIR + "/projectalias.map";
	public static final String PASSWORD_MAP_LOCATION 				= BASE_DIR + "/userpassword.map";
	
	public static final String REQUESTS_LOG_FILE_PATH				= BASE_DIR + "/requests.log";
	
	public static final String NULL_RETURN_PATH						= "file:/dev/null";
	
	public static final String TRUE 								= new Boolean(true).toString();
	public static final String FALSE 								= new Boolean(false).toString();
	
	public static final String RMI_PREFIX = "rmi://";
	
	public static NodeAgentProperties instance;
	
	static Logger traceLogger = LogManager.getLogger(LoggerUtil.TRACE_LOGGER_NAME);
	static Logger errorLogger = LogManager.getLogger(LoggerUtil.ERROR_LOGGER_NAME);
	
	/*
	example properties file (estar.nodeagent.server.configuration):
	telescope.name=Liverpool Telescope
	islive=true
	tea.hostname=ltproxy
	nodeagent.port=1099
	tea.connect=true
	rtml.hashstoragefile=rtml.hash.storage
	tea.request.handler.rmi.name=EARequestHandler
	nodeagent.response.handler.rmi.name=NAAsyncResponseHandler
	 */
	/**
	 * Class method to retrieve a singleton instance of NodeAgentProperties.
	 * This is constructed if 'instance' is null (and saved in 'instance' for future reference,
	 * otherwise 'instance' is null.
	 * @return The instance of NodeAgentProperties
	 * @see #instance
	 * @see #errorLogger
	 */
	public static NodeAgentProperties getInstance() 
	{
		if (instance == null) 
		{
			try 
			{
				instance = new NodeAgentProperties();
			} 
			catch (IOException e) 
			{
				errorLogger.log(1, NodeAgentProperties.class.getName(), "IOException in loading " + PROPERTIES_FILE_PATH + " no properties loaded!!!");
			}
		}
		return instance;
	}
	
	/**
	 * Constructor for NodeAgentProperties. Calls the super constructor, calling
	 * getPropertiesFromFile(PROPERTIES_FILE_PATH)
	 * @throws IOException Thrown if reading the file fails.
	 * @see #PROPERTIES_FILE_PATH
	 * @see #getPropertiesFromFile
	 */
	private NodeAgentProperties() throws IOException 
	{
		super(getPropertiesFromFile(PROPERTIES_FILE_PATH));
	}
	/**
	 * Loads a set of properties from the specified filename.
	 * @param sfp The properties filename.
	 * @return An instance of Properties.
	 * @throws IOException
	 */
	private static Properties getPropertiesFromFile(String sfp) throws IOException 
	{
		Properties properties = new Properties();
		FileInputStream in = new FileInputStream(sfp);
		properties.load(in);
		return properties;
	}
	
	/**
	 * Print a debugging list of properties to System.out.
	 */
	public void debugShowProperties() 
	{
		Enumeration keysE = defaults.keys();
		String s = this.getClass().getName() +"[";
		
		while (keysE.hasMoreElements()) {
			Object key = keysE.nextElement();
			Object value = defaults.get(key);
			s = key + ":" + value;
		}
		s += "]";
		System.out.println(s);
	}

}
