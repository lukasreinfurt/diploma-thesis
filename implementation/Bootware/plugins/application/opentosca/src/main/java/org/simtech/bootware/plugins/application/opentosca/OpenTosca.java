package org.simtech.bootware.plugins.application.opentosca;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.simtech.bootware.core.ConfigurationWrapper;
import org.simtech.bootware.core.Connection;
// import org.simtech.bootware.core.events.ApplicationPluginEvent;
// import org.simtech.bootware.core.events.Severity;
import org.simtech.bootware.core.exceptions.ExecuteCommandException;
import org.simtech.bootware.core.exceptions.ProvisionApplicationException;
import org.simtech.bootware.core.exceptions.StartApplicationException;
import org.simtech.bootware.core.plugins.AbstractBasePlugin;
import org.simtech.bootware.core.plugins.ApplicationPlugin;

public class OpenTosca extends AbstractBasePlugin implements ApplicationPlugin {

	public OpenTosca() {}

	public final void initialize(final Map<String, ConfigurationWrapper> configurationList) {
		// no op
	}

	public final void shutdown() {
		// no op
	}

	public final void provision(final Connection connection) throws ProvisionApplicationException {

		if (connection != null) {
			try {
				connection.execute("curl install.opentosca.de/installEC2|sh");
			}
			catch (IllegalArgumentException e) {
				throw new ProvisionApplicationException(e);
			}
			catch (ExecuteCommandException e) {
				throw new ProvisionApplicationException(e);
			}
		}
		else {
			throw new ProvisionApplicationException("Connection is null.");
		}
	}

	public final void deprovision(final Connection connection) {
		// no op
	}

	public final URL start(final Connection connection) throws StartApplicationException {

		if (connection != null) {
			try {
				final URL url = new URL("http://" + connection.getURL() + ":8080");
				return url;
			}
			catch (MalformedURLException e) {
				throw new StartApplicationException(e);
			}
		}
		else {
			throw new StartApplicationException("Connection null.");
		}
	}

	public final void stop(final Connection connection) {
		// no op
	}

}
