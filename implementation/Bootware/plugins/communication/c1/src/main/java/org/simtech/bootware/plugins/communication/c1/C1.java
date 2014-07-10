package org.simtech.bootware.plugins.communication.c1;

import java.util.Map;

import org.simtech.bootware.core.ConfigurationWrapper;
import org.simtech.bootware.core.Connection;
import org.simtech.bootware.core.plugins.AbstractBasePlugin;
import org.simtech.bootware.core.plugins.CommunicationPlugin;

public class C1 extends AbstractBasePlugin implements CommunicationPlugin {

	public C1() {}

	public final void initialize(final ConfigurationWrapper configuration) {
		// no op
	}

	public final void shutdown() {
		// no op
	}

	public final Connection connect(final Map<String, String> instanceInformation) {
		System.out.println("C1: connect");
		return new Connection();
	}

	public final void disconnect(final Connection connection) {
		System.out.println("C1: disconnect");
	}

}
