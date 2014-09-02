package org.simtech.bootware.plugins.resource.empty;

import java.util.HashMap;
import java.util.Map;

import org.simtech.bootware.core.ConfigurationWrapper;
import org.simtech.bootware.core.exceptions.InitializeException;
import org.simtech.bootware.core.plugins.AbstractBasePlugin;
import org.simtech.bootware.core.plugins.ResourcePlugin;

public class Empty extends AbstractBasePlugin implements ResourcePlugin {

	public Empty() {}

	public final void initialize(final Map<String, ConfigurationWrapper> configurationList) throws InitializeException {
		// no op
	}

	public final void shutdown() {
		// no op
	}

	public final Map<String, String> provision() {
		final Map<String, String> instanceInformation = new HashMap<String, String>();
		return instanceInformation;
	}

	public final void deprovision(final Map<String, String> instanceInformation) {
		// no op
	}

}
