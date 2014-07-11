package org.simtech.bootware.plugins.event.consoleLogger;

import org.simtech.bootware.core.ConfigurationWrapper;
import org.simtech.bootware.core.events.BaseEvent;
import org.simtech.bootware.core.plugins.AbstractBasePlugin;
import org.simtech.bootware.core.plugins.EventPlugin;

import net.engio.mbassy.common.DeadMessage;
import net.engio.mbassy.common.FilteredMessage;
import net.engio.mbassy.listener.Handler;

public class ConsoleLogger extends AbstractBasePlugin implements EventPlugin {

	public ConsoleLogger() {}

	public final void initialize(final ConfigurationWrapper configuration) {
		// no op
	}

	public final void shutdown() {
		// no op
	}

	@Handler
	public final void handle(final BaseEvent event) {
		System.out.println("[" + event.getSeverity() + "] " + event.getMessage());
	}

	@Handler
	public final void handle(final DeadMessage message) {
		System.out.println("DeadMessage: " + message.getMessage());
	}

	@Handler
	public final void handle(final FilteredMessage message) {
		System.out.println("FilteredMessage: " + message.getMessage());
	}

}
