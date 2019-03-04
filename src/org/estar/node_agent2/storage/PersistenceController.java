package org.estar.node_agent2.storage;

import java.io.IOException;

import ngat.util.logging.LogManager;
import ngat.util.logging.Logger;

import org.estar.node_agent2.configuration.NodeAgentProperties;
import org.estar.node_agent2.util.LoggerUtil;

public class PersistenceController 
{
	/**
	 * The singleton instance of this class.
	 */
	public static PersistenceController instance;
	/**
	 * The trace logger.
	 */
	static Logger traceLogger = LogManager.getLogger(LoggerUtil.TRACE_LOGGER_NAME);
	/**
	 * The error logger.
	 */
	static Logger errorLogger = LogManager.getLogger(LoggerUtil.ERROR_LOGGER_NAME);
	
	/**
	 * The persistent map holding user aliases.
	 * @see org.estar.node_agent2.storage.PersistentMap
	 */
	private PersistentMap userAliasMapStore; 
	/**
	 * The persistent map holding project aliases.
	 * @see org.estar.node_agent2.storage.PersistentMap
	 */
	private PersistentMap projectAliasMapStore; 
	/**
	 * The persistent map holding user - password relationships.
	 * @see org.estar.node_agent2.storage.PersistentMap
	 */
	private PersistentMap passwordMapStore; 
	
	/**
	 * Get the singleton instance of this class. Construct it if required.
	 * @return The singleton instance of this class.
	 */
	public static PersistenceController getInstance() 
	{
		if (instance == null) 
		{
			instance = new PersistenceController();
		}
		return instance;
	}
	/**
	 * Internal constructor used by getInstance to instantiate the only instance of this class.
	 * Creates the userAliasMapStore, projectAliasMapStore and passwordMapStore PersistentMap's.
	 * @see #userAliasMapStore
	 * @see #projectAliasMapStore
	 * @see #passwordMapStore
	 * @see #errorLogger
	 */
	private PersistenceController() 
	{
		try 
		{
			userAliasMapStore = new PersistentMap(NodeAgentProperties.USERALIAS_MAP_LOCATION);
			projectAliasMapStore = new PersistentMap(NodeAgentProperties.PROJECTALIAS_MAP_LOCATION);
			passwordMapStore = new PersistentMap(NodeAgentProperties.PASSWORD_MAP_LOCATION); 
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			errorLogger.log(1, PersistenceController.class.getName(), e);
		}
	}

	/**
	 * Get the project alias PersistentMap instance.
	 * @return The project alias PersistentMap instance.
	 * @see #projectAliasMapStore
	 */
	public PersistentMap getProjectAliasMapStore() 
	{
		return projectAliasMapStore;
	}

	/**
	 * Set the project alias PersistentMap.
	 * @param projectAliasMapStore The project alias PersistentMap instance to use.
	 * @see #projectAliasMapStore
	 */
	public void setProjectAliasMapStore(PersistentMap projectAliasMapStore) 
	{
		this.projectAliasMapStore = projectAliasMapStore;
	}

	/**
	 * Get the user alias PersistentMap instance.
	 * @return The user alias PersistentMap instance.
	 * @see #userAliasMapStore
	 */
	public PersistentMap getUserAliasMapStore() 
	{
		return userAliasMapStore;
	}

	/**
	 * Set the user alias PersistentMap.
	 * @param userAliasMapStore The user alias PersistentMap instance to use.
	 * @see #userAliasMapStore
	 */
	public void setUserAliasMapStore(PersistentMap userAliasMapStore) 
	{
		this.userAliasMapStore = userAliasMapStore;
	}

	/**
	 * Get the password PersistentMap instance.
	 * @return The password PersistentMap instance.
	 * @see #passwordMapStore
	 */
	public PersistentMap getPasswordMapStore() 
	{
		return passwordMapStore;
	}

	/**
	 * Set the password PersistentMap.
	 * @param passwordMapStore The password PersistentMap instance to use.
	 * @see #passwordMapStore
	 */
	public void setPasswordMapStore(PersistentMap passwordMapStore) 
	{
		this.passwordMapStore = passwordMapStore;
	}

}
