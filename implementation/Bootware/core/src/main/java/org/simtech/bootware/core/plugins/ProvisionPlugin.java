package org.simtech.bootware.core.plugins;

import java.util.Map;

import org.simtech.bootware.core.ApplicationInstance;
import org.simtech.bootware.core.exceptions.DeprovisionException;
import org.simtech.bootware.core.exceptions.ProvisionException;

/**
 * Interface that should be implemented by provision plugins.
 */
public interface ProvisionPlugin extends Plugin {

	/**
	 * Call the given provisioning engine to provision the given service package.
	 *
	 * @param instance The ApplicationInstance containing among other this the
	 *                 informationList with the provisioning engine endpoint and
	 *                 the user context with the service package reference.
	 *
	 * @return A map of strings that contains information returned from the provisioning process.
	 */
	Map<String, String> provision(final ApplicationInstance instance) throws ProvisionException;

	/**
	 * Call the given provisioning engine to deprovision the given service package.
	 *
	 * @param instance The ApplicationInstance containing among other this the
	 *                 informationList with the provisioning engine endpoint and
	 *                 the user context with the service package reference.
	 */
	void deprovision(final ApplicationInstance instance) throws DeprovisionException;
}
