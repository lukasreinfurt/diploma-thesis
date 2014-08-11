package org.simtech.bootware.plugins.event.zeromqsubscriber;

import java.util.Map;

import org.simtech.bootware.core.ConfigurationWrapper;
import org.simtech.bootware.core.plugins.AbstractBasePlugin;
import org.simtech.bootware.core.plugins.EventPlugin;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

public class ZeroMQSubscriber extends AbstractBasePlugin implements EventPlugin {

	private Context context;
	private Socket subscriber;
	private Thread t;
	private Listener listener;

	public ZeroMQSubscriber() {}

	public final void initialize(final Map<String, ConfigurationWrapper> configurationList) {
		context    = ZMQ.context(1);
		subscriber = context.socket(ZMQ.SUB);
		subscriber.connect("tcp://localhost:5563");
		subscriber.subscribe("remote".getBytes());

		listener = new Listener();
		t = new Thread(listener);
		t.start();
	}

	public final void shutdown() {
		if (t != null) {
			try {
				listener.terminate();
				t.join();
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		subscriber.close();
		context.term();
	}

	private class Listener implements Runnable {

		private volatile Boolean running = true;

		public Listener() {}

		public final void terminate() {
			running = false;
		}

		public final void run() {
			while (running) {
				final String address = subscriber.recvStr(ZMQ.NOBLOCK);
				final String contents = subscriber.recvStr(ZMQ.NOBLOCK);
				if (address != null && contents != null) {
					System.out.println("Remote Bootware => " + contents);
				}
			}
		}

	}

}
