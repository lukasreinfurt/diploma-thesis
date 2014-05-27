package org.simtech.bootware.plugins.payload.p2;

import org.simtech.bootware.core.plugins.AbstractPayloadPlugin;

public class P2 extends AbstractPayloadPlugin {

	public P2() {
		super();
	}

	public void initialize() {
		// no op
	}

	public void shutdown() {
		// no op
	}

	public void test() {
		System.out.println("P2");
	}

}
