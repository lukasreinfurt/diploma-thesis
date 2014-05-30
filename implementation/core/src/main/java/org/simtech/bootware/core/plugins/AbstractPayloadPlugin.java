package org.simtech.bootware.core.plugins;

import java.net.URL;

import org.simtech.bootware.core.Connection;
import org.simtech.bootware.core.exceptions.DeprovisionPayloadException;
import org.simtech.bootware.core.exceptions.ProvisionPayloadException;
import org.simtech.bootware.core.exceptions.StartPayloadException;
import org.simtech.bootware.core.exceptions.StopPayloadException;

public abstract class AbstractPayloadPlugin extends AbstractBasePlugin {

	public abstract void provision(Connection connect) throws ProvisionPayloadException;
	public abstract void deprovision(Connection connect) throws DeprovisionPayloadException;
	public abstract URL start(Connection connect) throws StartPayloadException;
	public abstract void stop(Connection connect) throws StopPayloadException;
}
