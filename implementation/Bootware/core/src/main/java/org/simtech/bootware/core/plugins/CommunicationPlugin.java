package org.simtech.bootware.core.plugins;

import java.util.Map;

import org.simtech.bootware.core.Connection;
import org.simtech.bootware.core.exceptions.ConnectConnectionException;
import org.simtech.bootware.core.exceptions.DisconnectConnectionException;

public interface CommunicationPlugin extends Plugin {
	Connection connect(Map<String, String> instanceInformation) throws ConnectConnectionException;
	void disconnect(Connection connect) throws DisconnectConnectionException;
}
