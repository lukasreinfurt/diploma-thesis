package org.simtech.bootware.plugins.infrastructure.i2;

import org.simtech.bootware.core.plugins.AbstractInfrastructurePlugin;

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

	public void test() {
		System.out.println("I2");
	}

}
