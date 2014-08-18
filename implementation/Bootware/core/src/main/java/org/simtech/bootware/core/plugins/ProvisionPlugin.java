package org.simtech.bootware.core.plugins;

import java.util.Map;

public interface ProvisionPlugin extends Plugin {
	Map<String, String> provision(String provisioningEngineEndpoint, String servicePackageReference);
	void deprovision(String provisioningEngineEndpoint, String servicePackageReference);
}
