package org.simtech.bootware.core.plugins;

import java.net.URL;

import org.simtech.bootware.core.Connection;
import org.simtech.bootware.core.exceptions.DeprovisionApplicationException;
import org.simtech.bootware.core.exceptions.ProvisionApplicationException;
import org.simtech.bootware.core.exceptions.StartApplicationException;
import org.simtech.bootware.core.exceptions.StopApplicationException;

public interface ApplicationPlugin extends Plugin {
	void provision(Connection connect) throws ProvisionApplicationException;
	void deprovision(Connection connect) throws DeprovisionApplicationException;
	URL start(Connection connect) throws StartApplicationException;
	void stop(Connection connect) throws StopApplicationException;
}
