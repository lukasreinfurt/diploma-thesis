package org.simtech.bootware.plugins.connection.c1;

import org.simtech.bootware.core.plugins.AbstractConnectionPlugin;
import org.simtech.bootware.core.Connection;
import org.simtech.bootware.core.Instance;

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

	public Connection connect(Instance instance) {
		System.out.println("C1: connect");
		return new Connection();
	}

	public void disconnect(Connection connection) {
		System.out.println("C1: disconnect");
	}

}
