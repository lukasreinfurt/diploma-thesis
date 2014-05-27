package org.simtech.bootware.plugins.connection.c1;

import org.simtech.bootware.core.plugins.AbstractConnectionPlugin;

public class C1 extends AbstractConnectionPlugin {

	public C1() {
		super();
	}

	public void initialize() {
		// no op
	}

	public void shutdown() {
		// no op
	}

	public void test() {
		System.out.println("C1");
	}

}
