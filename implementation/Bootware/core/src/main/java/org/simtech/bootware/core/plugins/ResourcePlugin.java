package org.simtech.bootware.core.plugins;

import org.simtech.bootware.core.ConfigurationWrapper;
import org.simtech.bootware.core.Instance;
import org.simtech.bootware.core.exceptions.DeprovisionResourceException;
import org.simtech.bootware.core.exceptions.ProvisionResourceException;

public interface ResourcePlugin extends Plugin {
	Instance provision(ConfigurationWrapper configuration) throws ProvisionResourceException;
	void deprovision(Instance instance) throws DeprovisionResourceException;
}
