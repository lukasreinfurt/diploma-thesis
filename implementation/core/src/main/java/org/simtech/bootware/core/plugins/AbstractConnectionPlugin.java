package org.simtech.bootware.core.plugins;

import org.simtech.bootware.core.Connection;
import org.simtech.bootware.core.Instance;
import org.simtech.bootware.core.exceptions.ConnectException;
import org.simtech.bootware.core.exceptions.DisconnectException;

public abstract class AbstractConnectionPlugin extends AbstractBasePlugin {

	public abstract Connection connect(Instance instance) throws ConnectException;
	public abstract void disconnect(Connection connect) throws DisconnectException;

}
