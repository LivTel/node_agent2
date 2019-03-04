package org.estar.node_agent2;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

/**
 * 	Class to define the Node Agent web-service interface. Implemented web-service methods are:
 * <ul>
 * <li>String ping();
 * <li>String handle_rtml(String rtmlDocumentString);
 * </ul>
 * @author cjm
 */
@WebService
@SOAPBinding(style = Style.RPC)
public interface NodeAgentWebServiceInterface 
{
	@WebMethod
	String ping();
	
	@WebMethod
	String handle_rtml(String rtmlDocumentString);
}
