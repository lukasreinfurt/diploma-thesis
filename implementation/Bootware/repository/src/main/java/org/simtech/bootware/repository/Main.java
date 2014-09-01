package org.simtech.bootware.repository;

import java.net.URI;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import com.sun.net.httpserver.HttpServer;

/**
 * Simple mock repository
 */
public final class Main {

	static final String BASE_URI = "http://0.0.0.0:8888/";

	private Main() {
		// not called
	}

	/**
	 * Starts the repository server.
	 */
	public static void main(final String[] args) throws Exception {
		final ResourceConfig rc = new ResourceConfig(RestServer.class);
		final URI endpoint = new URI(BASE_URI);
		final HttpServer server = JdkHttpServerFactory.createHttpServer(endpoint, rc);
		System.out.println("The server is now listening at " + BASE_URI);
		System.out.println("Press Enter to stop the server. ");
		System.in.read();
		server.stop(0);
	}

}
