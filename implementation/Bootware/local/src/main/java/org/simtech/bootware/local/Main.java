package org.simtech.bootware.local;

/**
 * @author  Lukas Reinfurt
 * @version 1.0.0
*/

import javax.xml.ws.Endpoint;

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

		// Publish the bootware as a web service.
		final LocalBootwareImpl implementor = new LocalBootwareImpl();
		final String address = "http://localhost:6007/axis2/services/Bootware";
		final Endpoint endpoint = Endpoint.publish(address, implementor);
		System.out.println("WebService now running at " + address + " ...");
		implementor.run();

		// Wait for interrupt (e.g. Ctrl+C in the console)
		final Thread thread = new Thread() {
			@Override
			public void run() {
				synchronized (this) {
					while (!Thread.currentThread().isInterrupted()) {
						try {
							wait();
						}
						catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		};

		// On interrupt, stop bootware and web service.
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				implementor.stop();
				endpoint.stop();
				System.out.println("WebService stopped.");
				thread.interrupt();
			}
		});

		thread.start();
	}

}
