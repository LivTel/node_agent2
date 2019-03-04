package org.estar.node_agent2.storage;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import ngat.util.logging.LogManager;
import ngat.util.logging.Logger;

import org.estar.node_agent2.util.LoggerUtil;

/**
 * Class to store alias mappings on disc.
 * @author cjm,nrc
 */
public class PersistentMap  extends Properties
{
	/**
	 * Trace logger.
	 * @see org.estar.node_agent2.util.LoggerUtil#TRACE_LOGGER_NAME
	 */
	static Logger traceLogger = LogManager.getLogger(LoggerUtil.TRACE_LOGGER_NAME);
	/**
	 * Trace logger.
	 * @see org.estar.node_agent2.util.LoggerUtil#ERROR_LOGGER_NAME
	 */
	static Logger errorLogger = LogManager.getLogger(LoggerUtil.ERROR_LOGGER_NAME);

	/**
	 * Constructor.
	 * @param fileName The name to read the persistent data in from.
	 * @throws IOException Thrown if reading the file fails.
	 * @see #traceLogger
	 */
	public PersistentMap(String fileName) throws IOException 
	{	
		traceLogger.log(5, PersistentMap.class.getName(), "Loading alias store: " + fileName);
		Properties properties = new Properties();
		FileInputStream in = new FileInputStream(fileName);
		properties.load(in);
		this.putAll(properties);
		traceLogger.log(5, PersistentMap.class.getName(), "... " + this);
	}
	/**
	 * Method to get the key for the specifeid value i.e. a reverse lookup.
	 * @param value The value to find the key for.
	 * @return The string value of the key.
	 */
	public String getKey(String value) 
	{
		Enumeration keysE = this.keys();
		while (keysE.hasMoreElements()) 
		{
			String key = (String)keysE.nextElement();
			if (this.getProperty(key).equals(value)) 
			{
				return key;
			}
		}
		return null;
	}
	
	/**
	 * Print out the keywords and values in this store.
	 */
	public String toString() 
	{
		String s = this.getClass().getName() + "[";
		Enumeration keysE = this.keys();
		boolean hasElements = false;
		while (keysE.hasMoreElements()) 
		{
			String key = (String)keysE.nextElement();
			String value = this.getProperty(key);
			s += key + "=" + value + ";";
			hasElements = true;
		}
		if (hasElements) 
		{
			s = s.substring(0, s.length() -1);
		}
		s += "]";
		return s;
	}
}
