package org.simtech.bootware.plugins.payload.p2;

import java.net.URL;

import org.simtech.bootware.core.plugins.AbstractPayloadPlugin;
import org.simtech.bootware.core.Connection;
import org.simtech.bootware.core.exceptions.StartPayloadException;

public class P2 extends AbstractPayloadPlugin {

	public void initialize() {
		// no op
	}

	public void shutdown() {
		// no op
	}

	public void provision(Connection connection) {
		System.out.println("P2: provision");
	}

	public void deprovision(Connection connection) {
		System.out.println("P2: deprovision");
	}

	public URL start(Connection connection) throws StartPayloadException {
		System.out.println("P2: start");
		try {
			URL url = new URL("http://www.example.com");
			return url;
		}
		catch (Exception e) {
			throw new StartPayloadException(e);
		}
	}

	public void stop(Connection connection) {
		System.out.println("P2: stop");
	}

}
