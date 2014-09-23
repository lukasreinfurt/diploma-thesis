package org.simtech.bootware.core;

import java.io.InputStream;
import java.util.Map;

import org.simtech.bootware.core.exceptions.ConnectConnectionException;
import org.simtech.bootware.core.exceptions.DisconnectConnectionException;
import org.simtech.bootware.core.exceptions.ExecuteCommandException;
import org.simtech.bootware.core.exceptions.UploadFileException;

/**
 * Interface that should be implemented by Connection objects.
 */
public interface Connection {

	/**
	 * Creates a connection to a specific resource.
	 *
	 * @param settings Describe the specific resource, e.g. its URL, password, etc.
	 *
	 * @throws ConnectConnectionException If the connection could not be established.
	 */
	void connect(Map<String, String> settings) throws ConnectConnectionException;

	/**
	 * Disconnects the connection.
	 *
	 * @throws DisconnectConnectionException If the connection could not be disconnected.
	 */
	void disconnect() throws DisconnectConnectionException;

	/**
	 * Executes a command on the resource using the connection.
	 * <p>
	 * Requires that an active connection exists (i.e. that @see #connect was called earlier).
	 *
	 * @param command The command that should be executed.
	 *
	 * @throws ExecuteCommandException If there was an error executing the command.
	 */
	void execute(String command) throws ExecuteCommandException;

	/**
	 * Upload a file to the resource using the connection.
	 * <p>
	 * Requires that an active connection exists (i.e. that @see #connect was called earlier).
	 *
	 * @param inputStream An input stream of the file that should be uploaded.
	 * @param remotePath The absolute path on the resource where the file should be uploaded to, including file name.
	 *
	 * @throws UploadFileException If there was an error uploading the file.
	 */
	void upload(InputStream inputStream, String remotePath) throws UploadFileException;

	/**
	 * Returns the URL of the connection.
	 *
	 * @return The URL of the connection as String.
	 */
	String getURL();

}
