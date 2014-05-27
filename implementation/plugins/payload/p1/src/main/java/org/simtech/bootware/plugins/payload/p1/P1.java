package org.simtech.bootware.plugins.payload.p1;

import org.simtech.bootware.core.plugins.AbstractPayloadPlugin;

public class P1 extends AbstractPayloadPlugin {

	public P1() {
		super();
	}

	public void initialize() {
		// no op
	}

	public void shutdown() {
		// no op
	}

	public void test() {
		System.out.println("P1");
	}

}
