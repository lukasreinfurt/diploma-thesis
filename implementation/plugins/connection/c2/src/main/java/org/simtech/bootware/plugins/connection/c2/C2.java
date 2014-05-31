package org.simtech.bootware.plugins.connection.c2;

import org.simtech.bootware.core.Connection;
import org.simtech.bootware.core.Instance;
import org.simtech.bootware.core.plugins.AbstractBasePlugin;
import org.simtech.bootware.core.plugins.ConnectionPlugin;

public class C2 extends AbstractBasePlugin implements ConnectionPlugin {

	public final void initialize() {
		// no op
	}

	public final void shutdown() {
		// no op
	}

	public final Connection connect(Instance instance) {
		System.out.println("C2: connect");
		return new Connection();
	}

	public final void disconnect(Connection connection) {
		System.out.println("C2: disconnect");
	}

}
