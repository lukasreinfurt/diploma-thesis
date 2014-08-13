package org.simtech.bootware.core;

import java.util.HashMap;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import org.simtech.bootware.core.exceptions.DeployException;
import org.simtech.bootware.core.exceptions.SetConfigurationException;
import org.simtech.bootware.core.exceptions.ShutdownException;
import org.simtech.bootware.core.exceptions.UndeployException;

/**
 * Interface that is implemented by the bootware.
 */
@WebService
public interface Bootware {
	@WebMethod InformationListWrapper deploy(@WebParam(name = "context") UserContext context) throws DeployException;
	@WebMethod void undeploy(@WebParam(name = "context") UserContext context) throws UndeployException;
	@WebMethod void setConfiguration(@WebParam(name = "configurationList") HashMap<String, ConfigurationWrapper> configurationList) throws SetConfigurationException;
	@WebMethod void shutdown() throws ShutdownException;
}
