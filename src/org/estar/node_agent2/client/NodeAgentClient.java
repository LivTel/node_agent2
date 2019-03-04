package org.estar.node_agent2.client;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.handler.MessageContext;

import ngat.util.logging.*;

import org.estar.node_agent2.NodeAgentWebServiceInterface;


/**
 * Client program to invoke the NodeAgent web services.
 * @author cjm
 * @see org.estar.node_agent2.NodeAgentWebServiceInterface
 */
public class NodeAgentClient 
{
	/**
	 * 	The location of the wsdl endpoint.
	 */
	public final static String WSDL_ENDPOINT_STRING = "node_agent2/node_agent?wsdl";
	/**
	 * Which NodeAgent endpoint to call: None selected.
	 */
	public final static int ENDPOINT_NONE = 0;
	/**
	 * Which NodeAgent endpoint to call: ping() selected.
	 */
	public final static int ENDPOINT_PING = 1;
	/**
	 * Which NodeAgent endpoint to call: handle_rtml selected.
	 */
	public final static int ENDPOINT_HANDLE_RTML = 2;
	/**
	 * The hostname the tomcat server/web service is running on.
	 */
	String hostName = "ltobs9";
	/**
	 * The port number the tomcat server/web service is running on.
	 */
	int portNumber = 8080;
	/**
	 * The username to authenticate the web-service on.
	 */
	String username = null;
	/**
	 * The password to authenticate the web-service on.
	 */
	String password = null;
	/**
	 * The NodeAgent web service interface endpoint.
	 */
	NodeAgentWebServiceInterface nodeAgentInterface = null;
	/**
	 * Which NodeAgent endpoint to invoke.
	 * @see #ENDPOINT_NONE
	 * @see #ENDPOINT_PING
	 * @see #ENDPOINT_HANDLE_RTML
	 */
	int endpoint = ENDPOINT_NONE;
	/**
	 * A string containing a filename containing the RTML document to send to the
	 * NodeAgent to invoke the handle_rtml endpoint.
	 */
	String rtmlFilename = null;
	/**
	 * A string containing a filename, used to store the returned RTML document when
	 * invoking the handle_rtml endpoint.
	 */
	String outputFilename = null;
	/**
	 * 	Output logger.
	 */
	Logger log = null;
	/**
	 * 	Error logger.
	 */
	Logger errorLog = null;
	/**
	 * NodeAgentClient constructor. Does nothing.
	 */
	public NodeAgentClient()
	{
		super();
	}
	
	/** 
	 * Initialise the loggers
	 * @see #log
	 * @see #errorLog
	 */
	protected void initLoggers()
	{
		BogstanLogFormatter formatter = null;
		LogHandler handler = null;

		log = LogManager.getLogger(this.getClass().getName()+"Logger");
		log.setChannelID("LOG");
		errorLog = LogManager.getLogger(this.getClass().getName()+"ErrorLogger");
		errorLog.setChannelID("ERROR");
		formatter = new BogstanLogFormatter();
		handler = new ConsoleLogHandler(formatter);
		handler.setLogLevel(Logging.ALL);
		log.addHandler(handler);
		errorLog.addHandler(handler);
		log.setLogLevel(Logging.ALL);
		errorLog.setLogLevel(Logging.ALL);
	}
	
	/**
	 * Parse arguments.
	 * @param args An array of arguments to parse.
	 * @exception Exception Thrown if an error occurs.
	 * @see #help
	 * @see #hostName
	 * @see #portNumber
	 * @see #username
	 * @see #password
	 * @see #rtmlFilename
	 * @see #outputFilename
	 * @see #endpoint
	 * @see #ENDPOINT_HANDLE_RTML
	 * @see #ENDPOINT_PING
	 */
	public void parseArguments(String args[]) throws Exception
	{
		if(args.length < 1)
		{
			help();
			System.exit(0);
		}
		for(int i = 0; i < args.length; i++)
		{
			if(args[i].equals("-handle_rtml"))
			{
				if((i+1) < args.length)
				{
					rtmlFilename = args[i+1];
					endpoint = ENDPOINT_HANDLE_RTML;
				}
				else
				{
					System.err.println(this.getClass().getName()+
							":parseArguments:handle_rtml:No RTML filename specified.");
					System.exit(1);
				}
				i+= 1;
			}			
			else if(args[i].equals("-help"))
			{
				help();
				System.exit(0);
			}
			else if(args[i].equals("-host")||args[i].equals("-hostname"))
			{
				if((i+1) < args.length)
				{
					hostName = args[i+1];
				}
				else
				{
					System.err.println(this.getClass().getName()+
							":parseArguments:hostname:No hostname specified.");
					System.exit(3);
				}
				i+= 1;
			}			
			else if(args[i].equals("-output_filename"))
			{
				if((i+1) < args.length)
				{
					outputFilename = args[i+1];
				}
				else
				{
					System.err.println(this.getClass().getName()+
							":parseArguments:output_filename:No output_filename specified.");
					System.exit(2);
				}
				i+= 1;
			}			
			else if(args[i].equals("-password"))
			{
				if((i+1) < args.length)
				{
					password = args[i+1];
				}
				else
				{
					System.err.println(this.getClass().getName()+
							":parseArguments:password:No password specified.");
					System.exit(4);
				}
				i+= 1;
			}			
			else if(args[i].equals("-ping"))
			{
				endpoint = ENDPOINT_PING;
			}			
			else if(args[i].equals("-port")||args[i].equals("-port_number"))
			{
				if((i+1) < args.length)
				{
					portNumber = Integer.parseInt(args[i+1]);
				}
				else
				{
					System.err.println(this.getClass().getName()+
							":parseArguments:port_number:No port_number specified.");
					System.exit(5);
				}
				i+= 1;
			}			
			else if(args[i].equals("-username"))
			{
				if((i+1) < args.length)
				{
					username = args[i+1];
				}
				else
				{
					System.err.println(this.getClass().getName()+
							":parseArguments:username:No username specified.");
					System.exit(6);
				}
				i+= 1;
			}
			else
			{
				System.err.println(this.getClass().getName()+":parseArguments:Unknown Argument"+
						   args[i]);
				System.exit(7);			
			}
		}
	}
	
	/**
	 * Create the Node Agent Web Service endpoint to connect to the Node Agent
	 * at the specified hostname and port.
	 * @see #hostName
	 * @see #portNumber
	 * @see #WSDL_ENDPOINT_STRING
	 * @see #nodeAgentInterface
	 * @exception MalformedURLException Thrown if the URL of the endpoint is not correct.
	 */			
	public void createWebServiceEndpoint() throws MalformedURLException
	{
		URL wsdlUrl = null;
		QName qname = null;
		Service service = null;
		
		// create WSDL URL
		wsdlUrl = new URL("http://"+hostName+":"+portNumber+"/"+WSDL_ENDPOINT_STRING);
        // Create qualifier name ...
        qname = new QName("http://node_agent2.estar.org/", "NodeAgentWebServiceImplService");
        // Create the service
        service = Service.create(wsdlUrl, qname);
        // Create an instance of the Node Agent Web Service Interface
        nodeAgentInterface = service.getPort(NodeAgentWebServiceInterface.class);
	}
	
	/**
	 * Set the username and password in the SOAP headers (HTTP request headers) used to communicate with
	 * the web service.
	 * @see #nodeAgentInterface
	 * @see #hostName
	 * @see #portNumber
	 * @see #username
	 * @see #password
	 * @see #WSDL_ENDPOINT_STRING
	 */
	public void setUsernamePassword()
	{
		Map<String, Object> reqContext = ((BindingProvider) nodeAgentInterface)
                .getRequestContext();
        reqContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                "http://"+hostName+":"+portNumber+"/"+WSDL_ENDPOINT_STRING);

        Map<String, List<String>> headers = new HashMap<String, List<String>>();
        headers.put("username", Collections.singletonList(username));
        headers.put("password", Collections.singletonList(password));
        reqContext.put(MessageContext.HTTP_REQUEST_HEADERS, headers);	
	}

	/**
	 * help method.
	 */
	public void help()
	{
		System.err.println("java org.estar.node_agent2.client.NodeAgentClient");
		System.err.println("\t-host <hostname>");
		System.err.println("\t-port_number <port number>");
		System.err.println("\t-username <username>");
		System.err.println("\t-password <password>");
		System.err.println("\t-ping");
		System.err.println("\t-handle_rtml <RTML filename>");
		System.err.println("\t-output_filename <filename>");
	}
	
	/**
	 * Load the contents of a file and return them as a string.
	 * @param fileName The name of the file to load.
	 * @return The contents of the file, as a String.
	 * @throws IOException Thrown if a problem occurs.
	 */
	protected String loadFile(String fileName) throws IOException 
	{
		InputStream inputStream = null;
		File file = null;
		String line;
		
		file = new File(fileName);
	    inputStream = new FileInputStream(file);
		StringBuilder resultStringBuilder = new StringBuilder();
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		while ((line = br.readLine()) != null)
		{
			resultStringBuilder.append(line).append("\n");
		}
		br.close();
		return resultStringBuilder.toString();
	}
	
	/**
	 * Save the specified string into the specified filename
	 * @param fileName The name of the file to use.
	 * @param contentString The string to write into the file.
	 * @throws IOException Thrown if a problem occurs.
	 */
	protected void saveFile(String fileName,String contentString) throws IOException 
	{
		File file = new File (fileName);
		BufferedWriter out = new BufferedWriter(new FileWriter(file)); 
		out.write(contentString);
		out.close();
	}
	
	/**
	 * The NodeAgentClient program entry point.
	 * @param args Command line arguments.
	 * @throws Exception Thrown if the ptogram fails.
	 * @see #parseArguments
	 * @see #createWebServiceEndpoint
	 * @see #nodeAgentInterface
	 */
	public static void main(String[] args) 
	{
		NodeAgentClient nac = null;

		// initialise
		try
		{
			nac =  new NodeAgentClient();
			nac.initLoggers();
			nac.log.log(1,"NodeAgentClient:main:Parsing arguments.");
			nac.parseArguments(args);
			nac.log.log(1,"NodeAgentClient:main:Creating web-service endpoint.");
			nac.createWebServiceEndpoint();
		}
		catch (Exception e)
		{
			nac.errorLog.log(1,"NodeAgentClient failed to create web service endpoint:",e);
			e.printStackTrace();
			System.exit(1);
		}
		// setup authentication
		nac.log.log(1,"NodeAgentClient:main:Setting up authentication.");
		nac.setUsernamePassword();
		// Which endpoint are we calling?
		if(nac.endpoint == ENDPOINT_PING)
		{
			// call ping endpoint
			try
			{
				String returnString = null;
			
				nac.log.log(1,"NodeAgentClient:main:Invoking ping endpoint.");
				returnString = nac.nodeAgentInterface.ping();
				nac.log.log(1,"NodeAgentClient: ping returned:"+returnString);
			}
			catch (Exception e)
			{
				nac.errorLog.log(1,"NodeAgentClient failed to invoke ping endpoint:",e);
				e.printStackTrace();
				System.exit(2);
			}
		}
		else if(nac.endpoint == ENDPOINT_HANDLE_RTML)
		{
			String rtmlString = null;
			String returnString = null;

			// load rtml file
			try
			{
				nac.log.log(1,"NodeAgentClient:main:Loading RTML file "+nac.rtmlFilename+".");
				rtmlString = nac.loadFile(nac.rtmlFilename);
			}
			catch(Exception e)
			{
				nac.errorLog.log(1,"NodeAgentClient failed to load RTML file "+nac.rtmlFilename+":",e);
				e.printStackTrace();
				System.exit(3);
			}
			// call handle_rtml endpoint
			try
			{
				nac.log.log(1,"NodeAgentClient:main:Invoking handle_rtml endpoint.");
				returnString = nac.nodeAgentInterface.handle_rtml(rtmlString);
			}
			catch(Exception e)
			{
				nac.errorLog.log(1,"NodeAgentClient failed to invoke handle_rtml endpoint.");
				e.printStackTrace();
				System.exit(4);
			}
			// Save the returned document, or print it out
			if(nac.outputFilename != null)
			{
					try
					{
						nac.log.log(1,"NodeAgentClient:main:Saving returned RTML to file:"+nac.outputFilename);
						nac.saveFile(nac.outputFilename,returnString);
					}
					catch(Exception e)
					{
						nac.errorLog.log(1,"NodeAgentClient failed to save retutrned rtml into filename:"+nac.outputFilename,e);
						e.printStackTrace();
						System.exit(5);					
					}
			}
			else
			{
				nac.log.log(1,"NodeAgentClient:main:handle_rtml returned:"+returnString);
			}
		}
		else
		{
			nac.errorLog.log(1,"NodeAgentClient:No endpoint selected:Please use -ping or -handle_rtml");
			System.exit(6);
		}
		System.exit(0);
   } 
}
