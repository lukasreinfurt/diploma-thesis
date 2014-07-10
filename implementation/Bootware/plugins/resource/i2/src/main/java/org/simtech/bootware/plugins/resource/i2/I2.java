package org.simtech.bootware.plugins.resource.i2;

import java.util.HashMap;
import java.util.Map;

import org.simtech.bootware.core.ConfigurationWrapper;
import org.simtech.bootware.core.exceptions.ConfigurationException;
import org.simtech.bootware.core.plugins.AbstractBasePlugin;
import org.simtech.bootware.core.plugins.ResourcePlugin;

public class I2 extends AbstractBasePlugin implements ResourcePlugin {

	public I2() {}

	public final void initialize(final ConfigurationWrapper configuration) {
		try {
			System.out.println("I2: provision");
			System.out.println("789: " + configuration.get("789"));
		}
		catch (ConfigurationException e) {
			System.out.println(e.toString());
		}
	}

	public final void shutdown() {
		// no op
	}

	public final Map<String, String> provision() {
		return new HashMap<String, String>();
	}

	public final void deprovision(final Map<String, String> instanceInformation) {
		System.out.println("I2: deprovision");
	}

}
