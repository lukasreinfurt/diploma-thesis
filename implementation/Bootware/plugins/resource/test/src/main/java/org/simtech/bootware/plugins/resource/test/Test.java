package org.simtech.bootware.plugins.resource.test;

import java.util.HashMap;
import java.util.Map;

import org.simtech.bootware.core.ConfigurationWrapper;
import org.simtech.bootware.core.events.ResourcePluginEvent;
import org.simtech.bootware.core.events.Severity;
import org.simtech.bootware.core.exceptions.DeprovisionResourceException;
import org.simtech.bootware.core.exceptions.InitializeException;
import org.simtech.bootware.core.exceptions.ProvisionResourceException;
import org.simtech.bootware.core.plugins.AbstractBasePlugin;
import org.simtech.bootware.core.plugins.ResourcePlugin;

/**
 * An resource plugin that can be used for testing.
 */
public class Test extends AbstractBasePlugin implements ResourcePlugin {

	public Test() {}

	public final void initialize(final Map<String, ConfigurationWrapper> configurationList) throws InitializeException {
		// do initialization stuff
	}

	public final void shutdown() {
		// no shutdown stuff
	}

	public final Map<String, String> provision() throws ProvisionResourceException {
		eventBus.publish(new ResourcePluginEvent(Severity.INFO, "Provision has been called."));
		final Map<String, String> instanceInformation = new HashMap<String, String>();
		instanceInformation.put("Test", "123");
		return instanceInformation;
	}

	public final void deprovision(final Map<String, String> instanceInformation) throws DeprovisionResourceException {
		eventBus.publish(new ResourcePluginEvent(Severity.INFO, "Deprovision has been called."));
	}

}
