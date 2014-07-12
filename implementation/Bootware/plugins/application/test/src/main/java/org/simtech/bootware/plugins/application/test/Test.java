package org.simtech.bootware.plugins.application.test;

import java.net.MalformedURLException;
import java.net.URL;

import org.simtech.bootware.core.ConfigurationWrapper;
import org.simtech.bootware.core.Connection;
import org.simtech.bootware.core.events.ApplicationPluginEvent;
import org.simtech.bootware.core.events.Severity;
import org.simtech.bootware.core.exceptions.StartApplicationException;
import org.simtech.bootware.core.plugins.AbstractBasePlugin;
import org.simtech.bootware.core.plugins.ApplicationPlugin;

public class Test extends AbstractBasePlugin implements ApplicationPlugin {

	public Test() {}

	public final void initialize(final ConfigurationWrapper configuration) {
		// no op
	}

	public final void shutdown() {
		// no op
	}

	public final void provision(final Connection connection) {
		eventBus.publish(new ApplicationPluginEvent(Severity.SUCCESS, "Provision has been called."));
		if (connection != null) {
			connection.execute("ls /tmp");
			connection.upload("bootware-remote-1.0.0.jar", "/tmp");
			connection.execute("ls -al /tmp");
		}
	}

	public final void deprovision(final Connection connection) {
		eventBus.publish(new ApplicationPluginEvent(Severity.SUCCESS, "Deprovision has been called."));
	}

	public final URL start(final Connection connection) throws StartApplicationException {
		eventBus.publish(new ApplicationPluginEvent(Severity.SUCCESS, "Start has been called."));
		try {
			final URL url = new URL("http://www.example.com");
			return url;
		}
		catch (MalformedURLException e) {
			throw new StartApplicationException(e);
		}
	}

	public final void stop(final Connection connection) {
		eventBus.publish(new ApplicationPluginEvent(Severity.SUCCESS, "Stop has been called."));
	}

}
