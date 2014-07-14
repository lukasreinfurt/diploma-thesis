package org.simtech.bootware.plugins.resource.test;

import java.util.HashMap;
import java.util.Map;

import org.simtech.bootware.core.ConfigurationWrapper;
import org.simtech.bootware.core.events.ResourcePluginEvent;
import org.simtech.bootware.core.events.Severity;
import org.simtech.bootware.core.plugins.AbstractBasePlugin;
import org.simtech.bootware.core.plugins.ResourcePlugin;

public class Test extends AbstractBasePlugin implements ResourcePlugin {

	public Test() {}

	public final void initialize(final ConfigurationWrapper configuration) {
		// do initialization stuff
	}

	public final void shutdown() {
		// no shutdown stuff
	}

	public final Map<String, String> provision() {
		eventBus.publish(new ResourcePluginEvent(Severity.INFO, "Provision has been called."));
		return new HashMap<String, String>();
	}

	public final void deprovision(final Map<String, String> instanceInformation) {
		eventBus.publish(new ResourcePluginEvent(Severity.INFO, "Deprovision has been called."));
	}

}