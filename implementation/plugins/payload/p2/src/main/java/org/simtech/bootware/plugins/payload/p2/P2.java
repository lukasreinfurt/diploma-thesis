package org.simtech.bootware.plugins.payload.p2;

import java.net.MalformedURLException;
import java.net.URL;

import org.simtech.bootware.core.Connection;
import org.simtech.bootware.core.exceptions.StartPayloadException;
import org.simtech.bootware.core.plugins.AbstractBasePlugin;
import org.simtech.bootware.core.plugins.PayloadPlugin;

public class P2 extends AbstractBasePlugin implements PayloadPlugin {

	public P2() {}

	public final void initialize() {
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

	public final URL start(final Connection connection) throws StartPayloadException {
		System.out.println("P2: start");
		try {
			final URL url = new URL("http://www.example.com");
			return url;
		}
		catch (MalformedURLException e) {
			throw new StartPayloadException(e);
		}
	}

	public final void stop(final Connection connection) {
		System.out.println("P2: stop");
	}

}
