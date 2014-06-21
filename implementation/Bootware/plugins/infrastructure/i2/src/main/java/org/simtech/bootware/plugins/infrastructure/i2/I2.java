package org.simtech.bootware.plugins.infrastructure.i2;

import org.simtech.bootware.core.ConfigurationWrapper;
import org.simtech.bootware.core.Instance;
import org.simtech.bootware.core.exceptions.ConfigurationException;
import org.simtech.bootware.core.plugins.AbstractBasePlugin;
import org.simtech.bootware.core.plugins.InfrastructurePlugin;

public class I2 extends AbstractBasePlugin implements InfrastructurePlugin {

	public I2() {}

	public final void initialize() {
		// no op
	}

	public final void shutdown() {
		// no op
	}

	public final Instance provision(final ConfigurationWrapper configuration) {
		try {
			System.out.println("I2: provision");
			System.out.println("789: " + configuration.get("789"));
		}
		catch (ConfigurationException e) {
			System.out.println(e.toString());
		}
		return new Instance();
	}

	public final void deprovision(final Instance instance) {
		System.out.println("I2: deprovision");
	}

}
