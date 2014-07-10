package org.simtech.bootware.plugins.application.p2;

import java.net.MalformedURLException;
import java.net.URL;

import org.simtech.bootware.core.ConfigurationWrapper;
import org.simtech.bootware.core.Connection;
import org.simtech.bootware.core.exceptions.StartApplicationException;
import org.simtech.bootware.core.plugins.AbstractBasePlugin;
import org.simtech.bootware.core.plugins.ApplicationPlugin;

public class P2 extends AbstractBasePlugin implements ApplicationPlugin {

	public P2() {}

	public final void initialize(final ConfigurationWrapper configuration) {
		// no op
	}

	public final void shutdown() {
		// no op
	}

	public final void provision(final Connection connection) {
		System.out.println("P2: provision");
	}

	public final void deprovision(final Connection connection) {
		System.out.println("P2: deprovision");
	}

	public final URL start(final Connection connection) throws StartApplicationException {
		System.out.println("P2: start");
		try {
			final URL url = new URL("http://www.example.com");
			return url;
		}
		catch (MalformedURLException e) {
			throw new StartApplicationException(e);
		}
	}

	public final void stop(final Connection connection) {
		System.out.println("P2: stop");
	}

}
