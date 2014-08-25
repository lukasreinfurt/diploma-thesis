package org.simtech.bootware.plugins.event.zeromqpublisher;

import java.util.Map;

import org.simtech.bootware.core.ConfigurationWrapper;
import org.simtech.bootware.core.events.BaseEvent;
import org.simtech.bootware.core.plugins.AbstractBasePlugin;
import org.simtech.bootware.core.plugins.EventPlugin;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import net.engio.mbassy.common.DeadMessage;
import net.engio.mbassy.common.FilteredMessage;
import net.engio.mbassy.listener.Handler;

/**
 * An event plugin that publishes events to a socket.
 */
public class ZeroMQPublisher extends AbstractBasePlugin implements EventPlugin {

	private Context context;
	private Socket publisher;
	private String topic = "remote";

	public ZeroMQPublisher() {}

	/**
	 * Implements the initialize operation defined in @see org.simtech.bootware.core.plugins.Plugin
	 */
	public final void initialize(final Map<String, ConfigurationWrapper> configurationList) {
		context   = ZMQ.context(1);
		publisher = context.socket(ZMQ.PUB);
		publisher.bind("tcp://0.0.0.0:5563");
	}

	/**
	 * Implements the shutdown operation defined in @see org.simtech.bootware.core.plugins.Plugin
	 */
	public final void shutdown() {
		publisher.close();
		context.term();
	}

	/**
	 * Implements an event handler that reacts to events of the type @see org.simtech.bootware.core.events.BaseEvent
	 */
	@Handler
	public final void handle(final BaseEvent event) {
		publisher.send("[" + event.getSeverity() + "] " + event.getMessage(), ZMQ.NOBLOCK);
	}

	/**
	 * Implements an event handler that reacts to DeadMessage events.
	 * <p>
	 * DeadMessage events are published by squirrel-foundation when an event is
	 * published to which nobody subscribed.
	 */
	@Handler
	public final void handle(final DeadMessage message) {
		publisher.send("DeadMessage: " + message.getMessage(), ZMQ.NOBLOCK);
	}

	/**
	 * Implements an event handler that reacts to FilteredMessage events.
	 * <p>
	 * FilteredMessage events are published by squirrel-foundation when an event is
	 * published that doesn't reach any subscriber because it doesn't pass any filters.
	 */
	@Handler
	public final void handle(final FilteredMessage message) {
		publisher.send("FilteredMessage: " + message.getMessage(), ZMQ.NOBLOCK);
	}

}
