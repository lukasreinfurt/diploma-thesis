package org.simtech.bootware.core.plugins;

import org.simtech.bootware.core.Connection;
import org.simtech.bootware.core.Instance;
import org.simtech.bootware.core.exceptions.ConnectConnectionException;
import org.simtech.bootware.core.exceptions.DisconnectConnectionException;

public abstract class AbstractConnectionPlugin extends AbstractBasePlugin {

	public abstract Connection connect(Instance instance) throws ConnectConnectionException;
	public abstract void disconnect(Connection connect) throws DisconnectConnectionException;

}
