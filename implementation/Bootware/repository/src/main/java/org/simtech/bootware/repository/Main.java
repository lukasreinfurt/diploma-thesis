package org.simtech.bootware.repository;

import java.io.Closeable;

import com.sun.jersey.simple.container.SimpleServerFactory;

/**
 * Simple mock repository
 */
public final class Main {

	private Main() {
		// not called
	}

	/**
	 * Starts the repository server.
	 */
	public static void main(final String[] args) throws Exception {
		final Closeable server = SimpleServerFactory.create("http://0.0.0.0:80");
		try {
			System.out.println("Press any key to stop the server...");
			System.in.read();
		}
		finally {
			server.close();
		}
	}

}
