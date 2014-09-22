package org.simtech.bootware.plugins.provision.opentoscasimtech;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.util.Map;

import org.apache.commons.io.IOUtils;

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

	private final Integer maxRetries = 30;
	private final Integer waitBetweenRetries = 10000;
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

		// Connect. Retry a few times in case SSH server isn't available yet.
		for (int retries = 1; retries <= maxRetries; retries++) {
			try {
				Thread.sleep(waitBetweenRetries);
				eventBus.publish(new CommunicationPluginEvent(Severity.INFO, "Attempt " + retries + " out of " + maxRetries));
				connection.connect();
				break;
			}
			catch (ConnectException e) {
				if (retries == maxRetries) {
					throw new ConnectConnectionException(e);
				}
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

		// Try authentication with public key.
		try {
			final boolean isAuthenticated = connection.authenticateWithPublicKey(username, key.toCharArray(), null);
			if (!isAuthenticated) {
				throw new ConnectConnectionException("Authentication failed.");
			}
		}
		catch (IllegalStateException e) {
			throw new ConnectConnectionException(e);
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
			finally {
				// Close the session.
				session.close();
			}
		}
		else {
			throw new ExecuteCommandException("Session was null.");
		}
	}

	/**
	 * Implements the upload operation defined in @see org.simtech.bootware.core.plugins.Connection
	 */
	public final void upload(final InputStream inputStream, final String remotePath) throws UploadFileException {

		eventBus.publish(new CommunicationPluginEvent(Severity.INFO, "Uploading file '" + remotePath + "'."));

		// Upload file.
		SCPOutputStream outputStream = null;
		ByteArrayOutputStream baos = null;
		ByteArrayInputStream bais = null;
		try {

			// Set up byte array so that we can get the size of the file.
			baos = new ByteArrayOutputStream();
			IOUtils.copy(inputStream, baos);
			final byte[] bytes = baos.toByteArray();
			// Can be read multiple times with bais.reset() in between reads.
			bais = new ByteArrayInputStream(bytes);

			// Get size of file to be uploaded.
			final long size = bytes.length;

			// Create scp connection.
			final SCPClient scp = new SCPClient(connection);
			final File file = new File(remotePath);
			outputStream = scp.put(file.getName(), size, file.getParent().replace("\\", "/"), "0755");

			// Write file to connection.
			final byte[] buffer = new byte[bufferSize];
			int n;
			while ((n = bais.read(buffer)) > 0) {
				outputStream.write(buffer, 0, n);
			}
		}
		catch (IOException e) {
			throw new UploadFileException(e);
		}
		finally {
			// Clean up.
			try {
				if (outputStream != null) {
					outputStream.close();
				}
				if (baos != null) {
					baos.close();
				}
				if (bais != null) {
					bais.close();
				}
				if (inputStream != null) {
					inputStream.close();
				}
			}
			catch (IOException e) {
				throw new UploadFileException(e);
			}
		}
	}

	/**
	 * Implements the getURL operation defined in @see org.simtech.bootware.core.plugins.Connection
	 */
	public final String getURL() {
		return url;
	}

}
