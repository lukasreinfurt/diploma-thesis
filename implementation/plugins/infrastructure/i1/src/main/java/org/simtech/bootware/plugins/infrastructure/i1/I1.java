package org.simtech.bootware.plugins.infrastructure.i1;

import org.simtech.bootware.core.plugins.AbstractInfrastructurePlugin;
import org.simtech.bootware.core.Credentials;
import org.simtech.bootware.core.Instance;

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

	public Instance provision(Credentials credentials) {
		System.out.println("I1: provision");
		return new Instance();
	}

	public void deprovision(Instance instance) {
		System.out.println("I1: deprovision");
	}

}
