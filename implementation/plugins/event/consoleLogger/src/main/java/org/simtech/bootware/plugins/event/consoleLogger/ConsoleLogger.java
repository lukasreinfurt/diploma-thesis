package org.simtech.bootware.plugins.event.consoleLogger;

import org.simtech.bootware.core.plugins.AbstractEventPlugin;
import org.simtech.bootware.core.events.StateTransitionEvent;
import org.simtech.bootware.core.events.InfoEvent;
import org.simtech.bootware.core.events.SuccessEvent;
import org.simtech.bootware.core.events.WarningEvent;
import org.simtech.bootware.core.events.ErrorEvent;

import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Filter;
import net.engio.mbassy.common.DeadMessage;
import net.engio.mbassy.common.FilteredMessage;

public class ConsoleLogger extends AbstractEventPlugin {

	public void initialize() {
		// no op
	}

	public void shutdown() {
		// no op
	}

	@Handler
	public void handle(StateTransitionEvent event) {
		System.out.println("[TRANSITION] " + event.getMessage());
	}

	@Handler
	public void handle(InfoEvent event) {
		System.out.println("[INFO] " + event.getMessage());
	}

	@Handler
	public void handle(SuccessEvent event) {
		System.out.println("[SUCCESS] " + event.getMessage());
	}

	@Handler
	public void handle(WarningEvent event) {
		System.out.println("[WARNING] " + event.getMessage());
	}

	@Handler
	public void handle(ErrorEvent event) {
		System.out.println("[ERROR] " + event.getMessage());
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
