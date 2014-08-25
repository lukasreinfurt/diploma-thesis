package org.simtech.bootware.core.plugins;

import java.net.URL;

import org.simtech.bootware.core.Connection;
import org.simtech.bootware.core.exceptions.DeprovisionApplicationException;
import org.simtech.bootware.core.exceptions.ProvisionApplicationException;
import org.simtech.bootware.core.exceptions.StartApplicationException;
import org.simtech.bootware.core.exceptions.StopApplicationException;

/**
 * Interface that application plugins should implement
 */
public interface ApplicationPlugin extends Plugin {

	/**
	 * Provision the application using a give connection to a resource.
	 *
	 * @param connect The connection to the resource on which the application should be provisioned.
	 *
	 * @throws ProvisionApplicationException If there is an error during the provisioning process.
	 */
	void provision(Connection connect) throws ProvisionApplicationException;

	/**
	 * Deprovision the application using a give connection to a resource.
	 *
	 * @param connect The connection to the resource on which the application should be deprovisioned.
	 *
	 * @throws DeprovisionApplicationException If there is an error during the deprovisioning process.
	 */
	void deprovision(Connection connect) throws DeprovisionApplicationException;

	/**
	 * Start the application using a give connection to a resource.
	 *
	 * @param connect The connection to the resource on which the application should be started.
	 *
	 * @return The URL to the started application.
	 *
	 * @throws StartApplicationException If there is an error when starting the application.
	 */
	URL start(Connection connect) throws StartApplicationException;

	/**
	 * Stop the application using a give connection to a resource.
	 *
	 * @param connect The connection to the resource on which the application should be stopped.
	 *
	 * @throws StopApplicationException If there is an error when stopping the application.
	 */
	void stop(Connection connect) throws StopApplicationException;
}
