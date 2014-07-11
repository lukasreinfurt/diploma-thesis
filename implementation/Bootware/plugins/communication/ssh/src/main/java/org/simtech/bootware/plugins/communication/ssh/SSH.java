package org.simtech.bootware.plugins.communication.ssh;

import java.util.Map;

import org.simtech.bootware.core.ConfigurationWrapper;
import org.simtech.bootware.core.Connection;
import org.simtech.bootware.core.events.CommunicationPluginEvent;
import org.simtech.bootware.core.events.Severity;
import org.simtech.bootware.core.exceptions.ConnectConnectionException;
import org.simtech.bootware.core.plugins.AbstractBasePlugin;
import org.simtech.bootware.core.plugins.CommunicationPlugin;

public class SSH extends AbstractBasePlugin implements CommunicationPlugin {

	public SSH() {}

	public final void initialize(final ConfigurationWrapper configuration) {
		// no op
	}

	public final void shutdown() {
		// no op
	}

	public final Connection connect(final Map<String, String> instanceInformation) throws ConnectConnectionException {
		eventBus.publish(new CommunicationPluginEvent(Severity.INFO, "Connect has been called."));
		final Connection connection = new SshConnection();

		connection.connect(instanceInformation);

		return connection;
	}

	public final void disconnect(final Connection connection) {
		if (connection != null) {
			connection.disconnect();
		}
		eventBus.publish(new CommunicationPluginEvent(Severity.INFO, "Disconnect has been called."));
	}

}
