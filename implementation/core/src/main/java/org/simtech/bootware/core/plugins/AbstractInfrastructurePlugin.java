package org.simtech.bootware.core.plugins;

import org.simtech.bootware.core.Credentials;
import org.simtech.bootware.core.Instance;
import org.simtech.bootware.core.exceptions.DeprovisionInfrastructureException;
import org.simtech.bootware.core.exceptions.ProvisionInfrastructureException;

public abstract class AbstractInfrastructurePlugin extends AbstractBasePlugin {

	public abstract Instance provision(Credentials credentials) throws ProvisionInfrastructureException;
	public abstract void deprovision(Instance instance) throws DeprovisionInfrastructureException;

}
