package org.simtech.bootware.core.plugins;

import java.net.URL;

import org.simtech.bootware.core.Connection;
import org.simtech.bootware.core.exceptions.DeprovisionPayloadException;
import org.simtech.bootware.core.exceptions.ProvisionPayloadException;
import org.simtech.bootware.core.exceptions.StartPayloadException;
import org.simtech.bootware.core.exceptions.StopPayloadException;

public interface PayloadPlugin extends Plugin {
	void provision(Connection connect) throws ProvisionPayloadException;
	void deprovision(Connection connect) throws DeprovisionPayloadException;
	URL start(Connection connect) throws StartPayloadException;
	void stop(Connection connect) throws StopPayloadException;
}
