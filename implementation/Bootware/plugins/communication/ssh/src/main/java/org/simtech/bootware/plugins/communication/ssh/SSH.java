package org.simtech.bootware.plugins.communication.ssh;

import java.util.Map;

import org.simtech.bootware.core.ConfigurationWrapper;
import org.simtech.bootware.core.Connection;
import org.simtech.bootware.core.exceptions.ConnectConnectionException;
import org.simtech.bootware.core.exceptions.DisconnectConnectionException;
import org.simtech.bootware.core.plugins.AbstractBasePlugin;
import org.simtech.bootware.core.plugins.CommunicationPlugin;

/**
 * An communication plugin that can create SSH connections
 */
public class SSH extends AbstractBasePlugin implements CommunicationPlugin {

	public SSH() {}

	/**
	 * Implements the initialize operation defined in @see org.simtech.bootware.core.plugins.Plugin
	 */
	public final void initialize(final Map<String, ConfigurationWrapper> configurationList) {
		// no op
	}

	/**
	 * Implements the shutdown operation defined in @see org.simtech.bootware.core.plugins.Plugin
	 */
	public final void shutdown() {
		// no op
	}

	/**
	 * Implements the connect operation defined in @see org.simtech.bootware.core.plugins.CommunicationPlugin
	 */
	public final Connection connect(final Map<String, String> instanceInformation) throws ConnectConnectionException {
		final Connection connection = new SshConnection(eventBus);
		connection.connect(instanceInformation);
		return connection;
	}

	/**
	 * Implements the disconnect operation defined in @see org.simtech.bootware.core.plugins.CommunicationPlugin
	 */
	public final void disconnect(final Connection connection) throws DisconnectConnectionException {
		if (connection != null) {
			connection.disconnect();
		}
		else {
			throw new DisconnectConnectionException("Connection was null.");
		}
	}

}
