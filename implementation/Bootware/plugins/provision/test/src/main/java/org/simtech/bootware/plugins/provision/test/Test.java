package org.simtech.bootware.plugins.provision.test;

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
 * A provision plugin that can be used for testing.
 */
public class Test extends AbstractBasePlugin implements ProvisionPlugin {

	public Test() {}

	public final void initialize(final Map<String, ConfigurationWrapper> configurationList) throws InitializeException {
		// no op
	}

	public final void shutdown() {
		// no op
	}

	public final Map<String, String> provision(final ApplicationInstance instance) throws ProvisionException {
		System.out.println("Provision middleware");

		final Map<String, String> response = new HashMap<String, String>();

		response.put("odeServerUrl", "http://localhost:8080/ode");
		response.put("activeMQUrl", "tcp://localhost:61616");
		response.put("fragmentoUrl", "fragmentoUrl");

		return response;
	}

	public final void deprovision(final ApplicationInstance instance) throws DeprovisionException {
		System.out.println("Deprovision middleware");
	}

}
