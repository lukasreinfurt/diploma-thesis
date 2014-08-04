package org.simtech.bootware.local;

/**
 * @author  Lukas Reinfurt
 * @version 1.0.0
*/

import javax.xml.ws.Endpoint;

import org.simtech.bootware.core.exceptions.ShutdownException;

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
							// Do nothing
							// This exception is expected on Ctrl+C
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
