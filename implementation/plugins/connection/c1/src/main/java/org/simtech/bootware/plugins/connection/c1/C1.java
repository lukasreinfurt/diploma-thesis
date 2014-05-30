package org.simtech.bootware.plugins.connection.c1;

import org.simtech.bootware.core.Connection;
import org.simtech.bootware.core.Instance;
import org.simtech.bootware.core.plugins.AbstractConnectionPlugin;

public class C1 extends AbstractConnectionPlugin {

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
