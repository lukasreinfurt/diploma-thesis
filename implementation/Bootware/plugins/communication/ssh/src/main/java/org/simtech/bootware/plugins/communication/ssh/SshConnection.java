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
import org.simtech.bootware.core.exceptions.ConnectConnectionException;

import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.SCPOutputStream;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

public class SshConnection implements Connection {

	private ch.ethz.ssh2.Connection connection;
	private Session session;
	private final Integer maxRetries = 20;
	private final Integer waitBetweenRetries = 5000;
	private final Integer bufferSize = 4096;

	public SshConnection() {}

	@SuppressWarnings("checkstyle:cyclomaticcomplexity")
	public final void connect(final Map<String, String> settings) throws ConnectConnectionException {
		final String publicDNS = settings.get("publicDNS");
		final String username  = settings.get("username");
		final String key       = settings.get("privateKey");

		connection = new ch.ethz.ssh2.Connection(publicDNS);

		for (int retries = 0; retries < maxRetries; retries++) {
			try {
				Thread.sleep(waitBetweenRetries);
				connection.connect();
				System.out.println("done.");
				break;
			}
			catch (ConnectException e) {
				System.out.print(".");
			}
			catch (IOException e) {
				e.printStackTrace();
				break;
			}
			catch (InterruptedException e) {
				e.printStackTrace();
				break;
			}
		}

		try {
			final boolean isAuthenticated = connection.authenticateWithPublicKey(username, key.toCharArray(), null);
			if (!isAuthenticated) {
				throw new ConnectConnectionException("Authentication failed.");
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public final void disconnect() {
		if (connection != null) {
			connection.close();
		}
	}

	public final void execute(final String command) {
		try {
			session = connection.openSession();
			System.out.println("Session opened.");
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		if (session != null) {
			try {
				System.out.println(command);
				session.execCommand(command);
				final InputStream stdout = new StreamGobbler(session.getStdout());
				final BufferedReader br  = new BufferedReader(new InputStreamReader(stdout));
				while (true) {
					final String line = br.readLine();
					if (line == null) {
						br.close();
						break;
					}
					System.out.println(line);
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			session.close();
			System.out.println("Session closed.");
		}
	}

	public final void upload(final String localFile, final String remotePath) {
		try {
			final FileInputStream inputStream = new FileInputStream(localFile);
			final File file = new File(localFile);
			final long length = file.length();
			final SCPClient scp = new SCPClient(connection);
			final SCPOutputStream outputStream = scp.put("aaaaaaaaaaaaa", length, remotePath, "0755");

			final byte[] buffer = new byte[bufferSize];
			int n;
			while ((n = inputStream.read(buffer)) > 0) {
				outputStream.write(buffer, 0, n);
			}

			outputStream.close();
			inputStream.close();
		}
		catch (IOException e) {
			e.printStackTrace(System.out);
		}
	}

}
