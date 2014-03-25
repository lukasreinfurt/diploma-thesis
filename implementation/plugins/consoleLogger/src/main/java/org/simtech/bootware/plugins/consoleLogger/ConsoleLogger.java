package org.simtech.bootware.plugins.consoleLogger;

import org.simtech.bootware.core.plugins.BasePlugin;
import org.simtech.bootware.core.events.BaseEvent;

import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Filter;
import net.engio.mbassy.common.DeadMessage;
import net.engio.mbassy.common.FilteredMessage;

public class ConsoleLogger extends BasePlugin {

	@Handler
	public void handle(BaseEvent event) {
		System.out.println(event.getTimestamp() + ": " + event.getMessage());
	}

	@Handler
	public void handle(DeadMessage message) {
		System.out.println("DeadMessage: " + message.getMessage());
	}

	@Handler
	public void handle(FilteredMessage message) {
		System.out.println("FilteredMessage: " + message.getMessage());
	}

}
