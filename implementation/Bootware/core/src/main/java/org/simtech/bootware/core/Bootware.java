package org.simtech.bootware.core;

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

	/**
	 * Checks if the bootware is ready.
	 *
	 * @return A boolean indicating if the bootware is ready.
	 */
	@WebMethod Boolean isReady();

	/**
	 * Deploys an application on a specific resource.
	 *
	 * @param userContext Describes the application and the resource that should be provisioned.
	 *
	 * @return A wrapper object that contains a map of String tuples that describe the deployed application.
	 *
	 * @throws DeployException If there was an error during the deploy process.
	 */
	@WebMethod InformationListWrapper deploy(@WebParam(name = "context") UserContext context) throws DeployException;

	/**
	 * Undeploys an application and the resource it's running on.
	 *
	 * @param userContext Describes the application and the resource that should be deprovisioned.
	 *
	 * @throws UndeployException If there was an error during the undeploy process.
	 */
	@WebMethod void undeploy(@WebParam(name = "context") UserContext context) throws UndeployException;

	/**
	 * Sets the default configuration.
	 *
	 * @param configurationListWrapper A wrapper object that contains maps of String tuples containing configuration values.
	 *
	 * @throws SetConfigurationException If there was an error while setting the configuration.
	 */
	@WebMethod void setConfiguration(@WebParam(name = "configurationListWrapper") ConfigurationListWrapper configurationListWrapper) throws SetConfigurationException;

	/**
	 * Executes the shutdown process of the bootware.
	 *
	 * @throws ShutdownException If there was an error during the shutdown process.
	 */
	@WebMethod void shutdown() throws ShutdownException;
}
