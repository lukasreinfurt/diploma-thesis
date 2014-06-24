package org.simtech.bootware.core;

import java.net.URL;
import java.util.Map;

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
	@WebMethod EndpointsWrapper deploy(@WebParam(name = "context") Context context) throws DeployException;
	@WebMethod void undeploy(@WebParam(name = "endpoints") Map<String, URL> endpoints) throws UndeployException;
	@WebMethod void setConfiguration(@WebParam(name = "configurationList") Map<String, ConfigurationWrapper> configurationList) throws SetConfigurationException;
	@WebMethod void shutdown() throws ShutdownException;
}
