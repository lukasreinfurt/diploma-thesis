package org.simtech.bootware.aws;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.ConnectionInfo;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.SCPOutputStream;

public class SSH {

	private Connection connection;
	private Session session;

	public void SSH() {
	}

	public void connect(String hostname, String username, String keyfile) {
		System.out.print("Attempting to connect via SSH.");

		connection = new Connection(hostname);

		for (int retries = 0; retries < 20; retries++) {
			try {
				Thread.sleep(5000);
				connection.connect();
				System.out.println("done.");
				break;
			}
			catch (ConnectException e) {
				System.out.print(".");
			}
			catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}

		try {
			boolean isAuthenticated = connection.authenticateWithPublicKey(username, keyfile.toCharArray(), null);
			if (isAuthenticated == false) {
				System.out.println("Authentication failed.");
			}
			System.out.println("Authenticated.");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void disconnect() {
		if (connection != null) connection.close();
		System.out.println("SSH disconnected.");
	}

	public void execute(String command) {
		try {
			session = connection.openSession();
			System.out.println("Session opened.");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		if (session != null) {
			try {
				System.out.println(command);
				session.execCommand(command);
				InputStream stdout = new StreamGobbler(session.getStdout());
				BufferedReader br  = new BufferedReader(new InputStreamReader(stdout));
				while (true) {
					String line = br.readLine();
					if (line == null) {
						br.close();
						break;
					}
					System.out.println(line);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			session.close();
			System.out.println("Session closed.");
		}

	}

	public void upload(String localFile, String remotePath) {
		try {
			File file = new File(localFile);
			long length = file.length();
			SCPClient scp = new SCPClient(connection);
			System.out.println("Uploading file '" + localFile + "' (" + length + ") to '" + remotePath + "'.");
			SCPOutputStream outputStream = scp.put(localFile, length, remotePath, "0755");
			//outputStream.close();
			System.out.println("Upload complete.");
		}
		catch (Exception e) {
			e.printStackTrace(System.out);
		}

	}

}
