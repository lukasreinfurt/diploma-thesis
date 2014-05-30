package org.simtech.bootware.plugins.payload.p2;

import java.net.URL;

import org.simtech.bootware.core.Connection;
import org.simtech.bootware.core.exceptions.StartPayloadException;
import org.simtech.bootware.core.plugins.AbstractPayloadPlugin;

public class P2 extends AbstractPayloadPlugin {

	public final void initialize() {
		// no op
	}

	public final void shutdown() {
		// no op
	}

	public final void provision(Connection connection) {
		System.out.println("P2: provision");
	}

	public final void deprovision(Connection connection) {
		System.out.println("P2: deprovision");
	}

	public final URL start(Connection connection) throws StartPayloadException {
		System.out.println("P2: start");
		try {
			final URL url = new URL("http://www.example.com");
			return url;
		}
		catch (Exception e) {
			throw new StartPayloadException(e);
		}
	}

	public final void stop(Connection connection) {
		System.out.println("P2: stop");
	}

}
