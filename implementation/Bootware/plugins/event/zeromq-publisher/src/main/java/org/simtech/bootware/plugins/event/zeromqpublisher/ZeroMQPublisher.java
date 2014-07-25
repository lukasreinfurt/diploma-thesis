package org.simtech.bootware.plugins.event.zeromqpublisher;

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

public class ZeroMQPublisher extends AbstractBasePlugin implements EventPlugin {

	private Context context;
	private Socket publisher;

	public ZeroMQPublisher() {}

	public final void initialize(final ConfigurationWrapper configuration) {
		context   = ZMQ.context(1);
		publisher = context.socket(ZMQ.PUB);
		publisher.bind("tcp://localhost:5563");
	}

	public final void shutdown() {
		publisher.close();
		context.term();
	}

	@Handler
	public final void handle(final BaseEvent event) {
		publisher.sendMore("remote");
		publisher.send("[" + event.getSeverity() + "] " + event.getMessage(), ZMQ.NOBLOCK);
	}

	@Handler
	public final void handle(final DeadMessage message) {
		publisher.sendMore("remote");
		publisher.send("DeadMessage: " + message.getMessage(), ZMQ.NOBLOCK);
	}

	@Handler
	public final void handle(final FilteredMessage message) {
		publisher.sendMore("remote");
		publisher.send("FilteredMessage: " + message.getMessage(), ZMQ.NOBLOCK);
	}

}