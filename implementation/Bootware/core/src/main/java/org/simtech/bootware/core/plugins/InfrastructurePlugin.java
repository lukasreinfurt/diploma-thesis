package org.simtech.bootware.core.plugins;

import org.simtech.bootware.core.ConfigurationWrapper;
import org.simtech.bootware.core.Instance;
import org.simtech.bootware.core.exceptions.DeprovisionInfrastructureException;
import org.simtech.bootware.core.exceptions.ProvisionInfrastructureException;

public interface InfrastructurePlugin extends Plugin {
	Instance provision(ConfigurationWrapper configuration) throws ProvisionInfrastructureException;
	void deprovision(Instance instance) throws DeprovisionInfrastructureException;
}
