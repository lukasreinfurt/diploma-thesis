package org.simtech.bootware.core.plugins;

import org.simtech.bootware.core.Connection;
import org.simtech.bootware.core.Instance;
import org.simtech.bootware.core.exceptions.ConnectConnectionException;
import org.simtech.bootware.core.exceptions.DisconnectConnectionException;

public interface ConnectionPlugin extends Plugin {

	Connection connect(Instance instance) throws ConnectConnectionException;
	void disconnect(Connection connect) throws DisconnectConnectionException;

}
