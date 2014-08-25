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

/**
 * An event plugin that listens to messages published on a socket and writes
 * those messages to the console.
 */
public class ZeroMQSubscriber extends AbstractBasePlugin implements EventPlugin {

	private Context context;
	private Socket subscriber;
	private Thread t;
	private Listener listener;

	public ZeroMQSubscriber() {}

	/**
	 * Implements the initialize operation defined in @see org.simtech.bootware.core.plugins.Plugin
	 */
	public final void initialize(final Map<String, ConfigurationWrapper> configurationList) {
		// The socket connection is initialized later because we don't know the
		// URL we want to listen to at this moment.
	}

	/**
	 * Implements the shutdown operation defined in @see org.simtech.bootware.core.plugins.Plugin
	 */
	public final void shutdown() {
		// Wait for listener thread to finish
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

	/**
	 * Implements an event handler that reacts to events of the type @see org.simtech.bootware.core.events.RemoteBootwareStartedEvent
	 *
	 * @see org.simtech.bootware.core.events.RemoteBootwareStartedEvent contain
	 * the URL of the remote bootware to which we want to connect to.
	 */
	@Handler
	public final void handle(final RemoteBootwareStartedEvent event) {
		try {
			final URI remoteBootwareUrl = new URI(event.getUrl());
			final String zeromqUrl = "tcp://" +  remoteBootwareUrl.getHost() + ":5563";
			eventBus.publish(new PluginEvent(Severity.INFO, "Connecting ZeroMQ subscriber to " + zeromqUrl + "."));

			// Connect to socket on remote bootware and subscribe to all messages.
			context    = ZMQ.context(1);
			subscriber = context.socket(ZMQ.SUB);
			subscriber.connect(zeromqUrl);
			subscriber.subscribe("".getBytes());

			// Start a new threaded listener so we wont block further execution.
			listener = new Listener();
			t = new Thread(listener);
			t.start();
		}
		catch (URISyntaxException e) {
			eventBus.publish(new PluginEvent(Severity.INFO, "Error connecting ZeroMQ subscriber: " + e.getMessage()));
		}
	}

	/**
	 * A threaded listener that does not block.
	 * <p>
	 * It writes all messages it receives to the console.
	 */
	private class Listener implements Runnable {

		private volatile Boolean running = true;

		public Listener() {}

		/**
		 * Stops the listener.
		 */
		public final void terminate() {
			running = false;
		}

		/**
		 * Starts the listener.
		 */
		public final void run() {
			while (running) {
				final String contents = subscriber.recvStr(ZMQ.NOBLOCK);
				if (contents != null) {
					System.out.println("Remote Bootware => " + contents);
				}
			}
		}

	}

}
