package org.simtech.bootware.remote;

/**
 * @author  Lukas Reinfurt
 * @version 1.0.0
*/

import java.io.IOException;
import java.net.InetSocketAddress;

import javax.xml.ws.Endpoint;

import org.simtech.bootware.core.exceptions.ShutdownException;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;

@SuppressWarnings("checkstyle:emptyblock")
public final class Main {

	private Main() {
		// not called
	}

	/**
	 * Runs the bootware program.
	 *
	 * @param args Commandline arguments.
	 */
	public static void main(final String[] args) throws IOException {

		final RemoteBootwareImpl implementor = new RemoteBootwareImpl();

		// Bug: Using 0.0.0.0 will cause a NullPointerException on endpoint.stop().
		// See https://java.net/jira/browse/JAX_WS-941

		// Code as intended:
		final String address = "http://0.0.0.0:8080/axis2/services/Bootware";
		// final Endpoint endpoint = Endpoint.publish(address, implementor);

		// As workaround we publish to a HttpContext.
		// Workaround:
		final Integer port = 8080;
		final HttpServer server = HttpServer.create(new InetSocketAddress(port), 1);
		final HttpContext context = server.createContext("/axis2/services/Bootware");
		final Endpoint endpoint = Endpoint.create(implementor);
		endpoint.publish(context);
		server.start();
		// Workaround end.

		// Publish the bootware as web service
		implementor.run();
		System.out.println("WebService now running at " + address + " ...");

		// Wait until bootware has stopped before stopping the web service
		final Thread thread = new Thread() {
			@Override
			public void run() {
				synchronized (this) {

					// Wait until bootware has stopped
					while (!implementor.hasStopped()) {
						try {
							final Integer time = 1000;
							Thread.sleep(time);
						}
						catch (InterruptedException e) {
							// Do nothing
							// This exception is expected on Ctrl+C
						}
					}

					// Stop bootware service once bootware has stopped
					endpoint.stop();
					System.out.println("WebService stopped.");

					// Workaround:
					server.stop(0);
				}
			}
		};

		// Shutdown on interrupt (e.g. Ctrl+C in console)
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					if (!implementor.hasStopped()) {
						implementor.shutdown();
					}
					thread.interrupt();
					thread.join();
				}
				catch (ShutdownException e) {
					e.printStackTrace();
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});

		thread.start();
	}

}
