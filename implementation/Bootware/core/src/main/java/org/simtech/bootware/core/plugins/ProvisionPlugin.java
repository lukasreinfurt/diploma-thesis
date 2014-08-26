package org.simtech.bootware.core.plugins;

import java.util.Map;

import org.simtech.bootware.core.exceptions.DeprovisionException;
import org.simtech.bootware.core.exceptions.ProvisionException;

/**
 * Interface that should be implemented by provision plugins.
 */
public interface ProvisionPlugin extends Plugin {

	/**
	 * Call the given provisioning engine to provision the given service package.
	 *
	 * @param provisioningEngineEndpoint The endpoint of the provisioning engine that should be called.
	 * @param servicePackageReference The reference to the service package that should be provisioned.
	 *
	 * @return A map of strings that contains information returned from the provisioning process.
	 */
	Map<String, String> provision(String provisioningEngineEndpoint, String servicePackageReference) throws ProvisionException;

	/**
	 * Call the given provisioning engine to deprovision the given service package.
	 *
	 * @param provisioningEngineEndpoint The endpoint of the provisioning engine that should be called.
	 * @param servicePackageReference The reference to the service package that should be deprovisioned.
	 */
	void deprovision(String provisioningEngineEndpoint, String servicePackageReference) throws DeprovisionException;
}
