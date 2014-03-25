package org.simtech.bootware.webservice;

import javax.xml.ws.Endpoint;

public class Main {

	public static void main (String[] args) {
		final Object implementor = new Hello();
		final String address     = "http://localhost:8080/axis2/services/Hello";
		final Endpoint endpoint  = Endpoint.publish(address, implementor);

		final Thread thread = new Thread() {
			@Override
			public void run() {
				System.out.println("WebService now running at " + address + " ...");
				synchronized (this) {
					while (!Thread.currentThread().isInterrupted()) {
						try {
							wait();
						} catch (InterruptedException e) {
						}
					}
				}
			}
		};

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				endpoint.stop();
				System.out.println("WebService stopped.");
				thread.interrupt();
			}
		});

		thread.start();
	}

}
