package org.simtech.bootware.plugins.infrastructure.i2;

import org.simtech.bootware.core.plugins.AbstractInfrastructurePlugin;
import org.simtech.bootware.core.Credentials;
import org.simtech.bootware.core.Instance;

public class I2 extends AbstractInfrastructurePlugin {

	public I2() {
		super();
	}

	public void initialize() {
		// no op
	}

	public void shutdown() {
		// no op
	}

	public Instance provision(Credentials credentials) {
		System.out.println("I2: provision");
		return new Instance();
	}

	public void deprovision(Instance instance) {
		System.out.println("I2: deprovision");
	}

}
