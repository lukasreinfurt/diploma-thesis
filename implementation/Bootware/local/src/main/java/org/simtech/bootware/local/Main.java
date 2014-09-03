package org.simtech.bootware.local;

import javax.xml.ws.Endpoint;

import org.simtech.bootware.core.exceptions.ShutdownException;

public final class Main {

	private Main() {
		// not called
	}

	/**
	 * Runs the local bootware web service.
	 *
	 * @param args Commandline arguments (not used).
	 */
	public static void main(final String[] args) {

		final LocalBootwareImpl implementor = new LocalBootwareImpl();
		final String address = "http://localhost:6007/axis2/services/Bootware";
		final Endpoint endpoint = Endpoint.publish(address, implementor);

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
							Thread.currentThread().interrupt();
						}
					}

					// Stop bootware service once bootware has stopped
					endpoint.stop();
					System.out.println("WebService stopped.");
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
					System.out.println("There was an error while shutting down the local bootware: " + e.getMessage());
				}
				catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		});

		thread.start();
	}

}
