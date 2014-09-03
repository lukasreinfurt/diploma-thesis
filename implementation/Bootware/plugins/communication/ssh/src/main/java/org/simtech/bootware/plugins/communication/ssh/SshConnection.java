package org.simtech.bootware.plugins.communication.ssh;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.util.Map;

import org.simtech.bootware.core.Connection;
import org.simtech.bootware.core.EventBus;
import org.simtech.bootware.core.events.CommunicationPluginEvent;
import org.simtech.bootware.core.events.Severity;
import org.simtech.bootware.core.exceptions.ConnectConnectionException;
import org.simtech.bootware.core.exceptions.DisconnectConnectionException;
import org.simtech.bootware.core.exceptions.ExecuteCommandException;
import org.simtech.bootware.core.exceptions.UploadFileException;

import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.SCPOutputStream;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

/**
 * Implements a connection object that is used by a communication plugin.
 * <p>
 * Uses the Ganymed SSH-2 library.
 */
public class SshConnection implements Connection {

	private EventBus eventBus;
	private ch.ethz.ssh2.Connection connection;
	private Session session;

	private String url;
	private String username;
	private String key;

	private final Integer maxRetries = 20;
	private final Integer waitBetweenRetries = 5000;
	private final Integer bufferSize = 4096;

	public SshConnection(final EventBus eb) {
		eventBus = eb;
	}

	/**
	 * Implements the connect operation defined in @see org.simtech.bootware.core.Connection
	 * <p>
	 * Currently supports public key authentication.
	 */
	@SuppressWarnings("checkstyle:cyclomaticcomplexity")
	public final void connect(final Map<String, String> settings) throws ConnectConnectionException {
		url      = settings.get("resourceURL");
		username = settings.get("sshUsername");
		key      = settings.get("sshKey");

		connection = new ch.ethz.ssh2.Connection(url);
		eventBus.publish(new CommunicationPluginEvent(Severity.INFO, "Connecting to '" + url + "'."));

		// Retry a few times
		for (int retries = 0; retries < maxRetries; retries++) {
			try {
				Thread.sleep(waitBetweenRetries);
				connection.connect();
				break;
			}
			catch (ConnectException e) {
				continue;
			}
			catch (IOException e) {
				throw new ConnectConnectionException(e);
			}
			catch (InterruptedException e) {
				throw new ConnectConnectionException(e);
			}
		}

		eventBus.publish(new CommunicationPluginEvent(Severity.INFO, "Authenticating connection."));

		// Try authentication with public key
		try {
			final boolean isAuthenticated = connection.authenticateWithPublicKey(username, key.toCharArray(), null);
			if (!isAuthenticated) {
				throw new ConnectConnectionException("Authentication failed.");
			}
		}
		catch (IOException e) {
			throw new ConnectConnectionException(e);
		}
	}

	/**
	 * Implements the disconnect operation defined in @see org.simtech.bootware.core.Connection
	 */
	public final void disconnect() throws DisconnectConnectionException {
		if (connection != null) {
			connection.close();
		}
		else {
			throw new DisconnectConnectionException("Connection was null.");
		}
	}

	/**
	 * Implements the execute operation defined in @see org.simtech.bootware.core.plugins.Connection
	 */
	public final void execute(final String command) throws ExecuteCommandException {
		eventBus.publish(new CommunicationPluginEvent(Severity.INFO, "Executing command '" + command + "'."));

		// Open a session.
		try {
			session = connection.openSession();
		}
		catch (IOException e) {
			throw new ExecuteCommandException(e);
		}

		if (session != null) {
			try {
				// Execute command.
				session.execCommand(command);

				final InputStream stdout = new StreamGobbler(session.getStdout());
				final InputStream stderr = new StreamGobbler(session.getStderr());
				final BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(stdout));
				final BufferedReader stderrReader = new BufferedReader(new InputStreamReader(stderr));
				final StringBuilder stringBuilder = new StringBuilder();

				// Read stdout output of command to string.
				while (true) {
					final String line = stdoutReader.readLine();
					if (line == null) {
						stdoutReader.close();
						break;
					}
					else {
						stringBuilder.append(line);
					}
				}

				// Read stderr output of command to string
				while (true) {
					final String line = stderrReader.readLine();
					if (line == null) {
						stderrReader.close();
						break;
					}
					else {
						stringBuilder.append(line);
					}
				}

				// If the output string is not empty, publish the output
				if (stringBuilder.length() > 0) {
					eventBus.publish(new CommunicationPluginEvent(Severity.DEBUG, "Command '" + command + "' output: " + stringBuilder.toString()));
				}
			}
			catch (IOException e) {
				throw new ExecuteCommandException(e);
			}

			// Close the session.
			session.close();
		}
		else {
			throw new ExecuteCommandException("Session was null.");
		}
	}

	/**
	 * Implements the upload operation defined in @see org.simtech.bootware.core.plugins.Connection
	 */
	public final void upload(final InputStream inputStream, final long length, final String remotePath) throws UploadFileException {

		eventBus.publish(new CommunicationPluginEvent(Severity.INFO, "Uploading file '" + remotePath + "'."));

		try {
			final SCPClient scp = new SCPClient(connection);
			final File file = new File(remotePath);
			final SCPOutputStream outputStream = scp.put(file.getName(), length, file.getParent().replace("\\", "/"), "0755");

			final byte[] buffer = new byte[bufferSize];
			int n;
			while ((n = inputStream.read(buffer)) > 0) {
				outputStream.write(buffer, 0, n);
			}

			outputStream.close();
			inputStream.close();
		}
		catch (IOException e) {
			throw new UploadFileException(e);
		}
	}

	/**
	 * Implements the getURL operation defined in @see org.simtech.bootware.core.plugins.Connection
	 */
	public final String getURL() {
		return url;
	}

}
