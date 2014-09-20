package org.simtech.bootware.plugins.communication.empty;

import java.io.InputStream;
import java.util.Map;

import org.simtech.bootware.core.Connection;
import org.simtech.bootware.core.EventBus;
import org.simtech.bootware.core.exceptions.ConnectConnectionException;
import org.simtech.bootware.core.exceptions.DisconnectConnectionException;
import org.simtech.bootware.core.exceptions.ExecuteCommandException;
import org.simtech.bootware.core.exceptions.UploadFileException;

public class EmptyConnection implements Connection {

	private EventBus eventBus;

	public EmptyConnection(final EventBus eb) {
		eventBus = eb;
	}

	public final void connect(final Map<String, String> settings) throws ConnectConnectionException {
		// no op
	}

	public final void disconnect() throws DisconnectConnectionException {
		// no op
	}

	public final void execute(final String command) throws ExecuteCommandException {
		// no op
	}

	public final void upload(final InputStream is, final String remotePath) throws UploadFileException {
		// no op
	}

	public final String getURL() {
		return "http://www.example.com";
	}

}
