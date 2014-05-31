package org.simtech.bootware.core.plugins;

import org.simtech.bootware.core.Credentials;
import org.simtech.bootware.core.Instance;
import org.simtech.bootware.core.exceptions.DeprovisionInfrastructureException;
import org.simtech.bootware.core.exceptions.ProvisionInfrastructureException;

public interface InfrastructurePlugin extends Plugin {

	Instance provision(Credentials credentials) throws ProvisionInfrastructureException;
	void deprovision(Instance instance) throws DeprovisionInfrastructureException;

}
