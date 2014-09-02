package org.simtech.bootware.plugins.application.empty;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.simtech.bootware.core.ConfigurationWrapper;
import org.simtech.bootware.core.Connection;
import org.simtech.bootware.core.exceptions.InitializeException;
import org.simtech.bootware.core.exceptions.ProvisionApplicationException;
import org.simtech.bootware.core.exceptions.StartApplicationException;
import org.simtech.bootware.core.exceptions.StopApplicationException;
import org.simtech.bootware.core.plugins.AbstractBasePlugin;
import org.simtech.bootware.core.plugins.ApplicationPlugin;

public class Empty extends AbstractBasePlugin implements ApplicationPlugin {

	public Empty() {}

	public final void initialize(final Map<String, ConfigurationWrapper> configurationList) throws InitializeException {
		// no op
	}

	public final void shutdown() {
		// no op
	}

	public final void provision(final Connection connection) throws ProvisionApplicationException {
		// no op
	}

	public final void deprovision(final Connection connection) {
		// no op
	}

	public final URL start(final Connection connection) throws StartApplicationException {

		try {
			final URL url = new URL("http://www.example.com");
			return url;
		}
		catch (MalformedURLException e) {
			throw new StartApplicationException(e);
		}

	}

	public final void stop(final Connection connection) throws StopApplicationException {
		// no op
	}

}
