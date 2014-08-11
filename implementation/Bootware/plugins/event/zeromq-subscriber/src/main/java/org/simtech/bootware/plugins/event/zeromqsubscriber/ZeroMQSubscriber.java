package org.simtech.bootware.plugins.event.zeromqsubscriber;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.simtech.bootware.core.ConfigurationWrapper;
import org.simtech.bootware.core.events.PluginEvent;
import org.simtech.bootware.core.events.RemoteBootwareStartedEvent;
import org.simtech.bootware.core.events.Severity;
import org.simtech.bootware.core.plugins.AbstractBasePlugin;
import org.simtech.bootware.core.plugins.EventPlugin;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import net.engio.mbassy.listener.Handler;

public class ZeroMQSubscriber extends AbstractBasePlugin implements EventPlugin {

	private Context context;
	private Socket subscriber;
	private Thread t;
	private Listener listener;

	public ZeroMQSubscriber() {}

	public final void initialize(final Map<String, ConfigurationWrapper> configurationList) {
		// no op
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

		if (subscriber != null) {
			subscriber.close();
		}

		if (context != null) {
			context.term();
		}
	}

	@Handler
	public final void handle(final RemoteBootwareStartedEvent event) {
		try {
			final URI remoteBootwareUrl = new URI(event.getUrl());
			final String zeromqUrl = "tcp://" +  remoteBootwareUrl.getHost() + ":5563";
			eventBus.publish(new PluginEvent(Severity.INFO, "Connecting ZeroMQ subscriber to " + zeromqUrl + "."));

			context    = ZMQ.context(1);
			subscriber = context.socket(ZMQ.SUB);
			subscriber.connect(zeromqUrl);
			subscriber.subscribe("remote".getBytes());

			listener = new Listener();
			t = new Thread(listener);
			t.start();
		}
		catch (URISyntaxException e) {
			eventBus.publish(new PluginEvent(Severity.INFO, "Error connecting ZeroMQ subscriber: " + e.getMessage()));
		}
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
