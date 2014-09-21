package org.simtech.bootware.plugins.provision.empty;

import java.util.HashMap;
import java.util.Map;

import org.simtech.bootware.core.ApplicationInstance;
import org.simtech.bootware.core.ConfigurationWrapper;
import org.simtech.bootware.core.exceptions.DeprovisionException;
import org.simtech.bootware.core.exceptions.InitializeException;
import org.simtech.bootware.core.exceptions.ProvisionException;
import org.simtech.bootware.core.plugins.AbstractBasePlugin;
import org.simtech.bootware.core.plugins.ProvisionPlugin;

/**
 * An empty provision plugin that can be used if no provision plugin is required.
 */
public class Empty extends AbstractBasePlugin implements ProvisionPlugin {

	public Empty() {}

	public final void initialize(final Map<String, ConfigurationWrapper> configurationList) throws InitializeException {
		// no op
	}

	public final void shutdown() {
		// no op
	}

	public final Map<String, String> provision(final ApplicationInstance instance) throws ProvisionException {
		final Map<String, String> response = new HashMap<String, String>();
		return response;
	}

	public final void deprovision(final ApplicationInstance instance) throws DeprovisionException {
		// no op
	}

}
