package org.simtech.bootware.plugins.communication.ssh;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
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

public class SshConnection implements Connection {

	private EventBus eventBus;
	private ch.ethz.ssh2.Connection connection;
	private Session session;
	private final Integer maxRetries = 20;
	private final Integer waitBetweenRetries = 5000;
	private final Integer bufferSize = 4096;

	public SshConnection(final EventBus eb) {
		eventBus = eb;
	}

	@SuppressWarnings("checkstyle:cyclomaticcomplexity")
	public final void connect(final Map<String, String> settings) throws ConnectConnectionException {
		final String publicDNS = settings.get("publicDNS");
		final String username  = settings.get("username");
		final String key       = settings.get("privateKey");

		connection = new ch.ethz.ssh2.Connection(publicDNS);
		eventBus.publish(new CommunicationPluginEvent(Severity.INFO, "Connecting to '" + publicDNS + "'."));

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

	public final void disconnect() throws DisconnectConnectionException {
		if (connection != null) {
			connection.close();
		}
		else {
			throw new DisconnectConnectionException("Connection was null.");
		}
	}

	public final void execute(final String command) throws ExecuteCommandException {
		eventBus.publish(new CommunicationPluginEvent(Severity.INFO, "Executing command '" + command + "'."));

		try {
			session = connection.openSession();
		}
		catch (IOException e) {
			throw new ExecuteCommandException(e);
		}

		if (session != null) {
			try {
				session.execCommand(command);
				final InputStream is     = new StreamGobbler(session.getStdout());
				final BufferedReader br  = new BufferedReader(new InputStreamReader(is));
				final StringBuilder sb   = new StringBuilder();
				while (true) {
					final String line = br.readLine();
					if (line == null) {
						br.close();
						break;
					}
					else {
						sb.append(line);
					}
				}
				eventBus.publish(new CommunicationPluginEvent(Severity.DEBUG, "Command '" + command + "' output: " + sb.toString()));
			}
			catch (IOException e) {
				throw new ExecuteCommandException(e);
			}
			session.close();
		}
	}

	public final void upload(final String localFile, final String remotePath) throws UploadFileException {
		eventBus.publish(new CommunicationPluginEvent(Severity.INFO, "Uploading file '" + localFile + "'."));

		try {
			final SCPClient scp = new SCPClient(connection);

			final File file = new File(localFile);
			final SCPOutputStream outputStream = scp.put(file.getName(), file.length(), remotePath, "0755");

			final FileInputStream inputStream = new FileInputStream(localFile);
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

}
