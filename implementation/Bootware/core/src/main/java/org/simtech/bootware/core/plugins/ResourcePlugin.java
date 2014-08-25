package org.simtech.bootware.core.plugins;

import java.util.Map;

import org.simtech.bootware.core.exceptions.DeprovisionResourceException;
import org.simtech.bootware.core.exceptions.ProvisionResourceException;

/**
 * Interface that should be implemented by resource plugins.
 */
public interface ResourcePlugin extends Plugin {

	/**
	 * Provision the resource.
	 *
	 * @return A map of strings with information about the provisioned resource.
	 *
	 * @throws ProvisionResourceException If there is an error when provisioning the resource.
	 */
	Map<String, String> provision() throws ProvisionResourceException;

	/**
	 * Deprovision the resource
	 *
	 * @param instanceInformation A map of strings with information about the provisioned resource.
	 *
	 * @throws DeprovisionResourceException If there is an error when deprovisioning the resource.
	 */
	void deprovision(Map<String, String> instanceInformation) throws DeprovisionResourceException;
}
