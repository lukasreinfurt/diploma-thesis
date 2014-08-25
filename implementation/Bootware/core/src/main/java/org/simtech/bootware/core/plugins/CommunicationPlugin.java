package org.simtech.bootware.core.plugins;

import java.util.Map;

import org.simtech.bootware.core.Connection;
import org.simtech.bootware.core.exceptions.ConnectConnectionException;
import org.simtech.bootware.core.exceptions.DisconnectConnectionException;

/**
 * Interface that communication plugins should implement
 */
public interface CommunicationPlugin extends Plugin {

	/**
	 * Create a connection to a specific resource.
	 *
	 * @param instanceInformation Information about the resource that is required to create the connection.
	 *
	 * @return The connection object that describes the connection to the resource.
	 *
	 * @throws ConnectConnectionException If there is an error when connecting to the resource.
	 */
	Connection connect(Map<String, String> instanceInformation) throws ConnectConnectionException;

	/**
	 * Disconnect a specific connection.
	 *
	 * @param connect The connection that should be disconnected.
	 *
	 * @throws DisconnectConnectionException If there is an error when disconnecting the connection.
	 */
	void disconnect(Connection connect) throws DisconnectConnectionException;
}
