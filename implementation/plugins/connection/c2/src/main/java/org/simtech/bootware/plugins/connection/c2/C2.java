package org.simtech.bootware.plugins.connection.c2;

import org.simtech.bootware.core.plugins.AbstractConnectionPlugin;

public class C2 extends AbstractConnectionPlugin {

	public C2() {
		super();
	}

	public void initialize() {
		// no op
	}

	public void shutdown() {
		// no op
	}

	public void test() {
		System.out.println("C2");
	}

}
