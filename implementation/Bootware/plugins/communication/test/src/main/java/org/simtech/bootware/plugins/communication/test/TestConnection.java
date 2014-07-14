package org.simtech.bootware.plugins.communication.test;

import java.util.Map;

import org.simtech.bootware.core.Connection;
import org.simtech.bootware.core.EventBus;
import org.simtech.bootware.core.events.CommunicationPluginEvent;
import org.simtech.bootware.core.events.Severity;
import org.simtech.bootware.core.exceptions.ConnectConnectionException;
import org.simtech.bootware.core.exceptions.DisconnectConnectionException;
import org.simtech.bootware.core.exceptions.ExecuteCommandException;
import org.simtech.bootware.core.exceptions.UploadFileException;

public class TestConnection implements Connection {

	private EventBus eventBus;

	public TestConnection(final EventBus eb) {
		eventBus = eb;
	}

	public final void connect(final Map<String, String> settings) throws ConnectConnectionException {
		// no op
	}

	public final void disconnect() throws DisconnectConnectionException {
		// no op
	}

	public final void execute(final String command) throws ExecuteCommandException {
		eventBus.publish(new CommunicationPluginEvent(Severity.INFO, "Executing command '" + command + "'."));
	}

	public final void upload(final String localFile, final String remotePath) throws UploadFileException {
		eventBus.publish(new CommunicationPluginEvent(Severity.INFO, "Uploading file '" + localFile + "'."));
	}

}
