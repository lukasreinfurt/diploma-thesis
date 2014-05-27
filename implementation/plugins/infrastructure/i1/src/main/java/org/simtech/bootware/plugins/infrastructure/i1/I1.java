package org.simtech.bootware.plugins.infrastructure.i1;

import org.simtech.bootware.core.plugins.AbstractInfrastructurePlugin;

public class I1 extends AbstractInfrastructurePlugin {

	public I1() {
		super();
	}

	public void initialize() {
		// no op
	}

	public void shutdown() {
		// no op
	}

	public void test() {
		System.out.println("I1");
	}

}
