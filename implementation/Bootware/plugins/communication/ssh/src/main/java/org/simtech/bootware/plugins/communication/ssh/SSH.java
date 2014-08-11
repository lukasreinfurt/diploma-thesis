package org.simtech.bootware.plugins.communication.ssh;

import java.util.Map;

import org.simtech.bootware.core.ConfigurationWrapper;
import org.simtech.bootware.core.Connection;
import org.simtech.bootware.core.exceptions.ConnectConnectionException;
import org.simtech.bootware.core.exceptions.DisconnectConnectionException;
import org.simtech.bootware.core.plugins.AbstractBasePlugin;
import org.simtech.bootware.core.plugins.CommunicationPlugin;

public class SSH extends AbstractBasePlugin implements CommunicationPlugin {

	public SSH() {}

	public final void initialize(final Map<String, ConfigurationWrapper> configurationList) {
		// no op
	}

	public final void shutdown() {
		// no op
	}

	public final Connection connect(final Map<String, String> instanceInformation) throws ConnectConnectionException {
		final Connection connection = new SshConnection(eventBus);
		connection.connect(instanceInformation);
		return connection;
	}

	public final void disconnect(final Connection connection) throws DisconnectConnectionException {
		if (connection != null) {
			connection.disconnect();
		}
		else {
			throw new DisconnectConnectionException("Connection was null.");
		}
	}

}
