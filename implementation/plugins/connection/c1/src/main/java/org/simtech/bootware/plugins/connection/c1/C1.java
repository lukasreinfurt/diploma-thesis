package org.simtech.bootware.plugins.connection.c1;

import org.simtech.bootware.core.Connection;
import org.simtech.bootware.core.Instance;
import org.simtech.bootware.core.plugins.AbstractConnectionPlugin;

public class C1 extends AbstractConnectionPlugin {

	public final void initialize() {
		// no op
	}

	public final void shutdown() {
		// no op
	}

	public final Connection connect(Instance instance) {
		System.out.println("C1: connect");
		return new Connection();
	}

	public final void disconnect(Connection connection) {
		System.out.println("C1: disconnect");
	}

}
