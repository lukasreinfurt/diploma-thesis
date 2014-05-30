package org.simtech.bootware.plugins.event.consoleLogger;

import org.simtech.bootware.core.events.ErrorEvent;
import org.simtech.bootware.core.events.InfoEvent;
import org.simtech.bootware.core.events.StateTransitionEvent;
import org.simtech.bootware.core.events.SuccessEvent;
import org.simtech.bootware.core.events.WarningEvent;
import org.simtech.bootware.core.plugins.AbstractEventPlugin;

import net.engio.mbassy.common.DeadMessage;
import net.engio.mbassy.common.FilteredMessage;
import net.engio.mbassy.listener.Handler;

public class ConsoleLogger extends AbstractEventPlugin {

	public final void initialize() {
		// no op
	}

	public final void shutdown() {
		// no op
	}

	@Handler
	public final void handle(StateTransitionEvent event) {
		System.out.println("[TRANSITION] " + event.getMessage());
	}

	@Handler
	public final void handle(InfoEvent event) {
		System.out.println("[INFO] " + event.getMessage());
	}

	@Handler
	public final void handle(SuccessEvent event) {
		System.out.println("[SUCCESS] " + event.getMessage());
	}

	@Handler
	public final void handle(WarningEvent event) {
		System.out.println("[WARNING] " + event.getMessage());
	}

	@Handler
	public final void handle(ErrorEvent event) {
		System.out.println("[ERROR] " + event.getMessage());
	}

	@Handler
	public final void handle(DeadMessage message) {
		System.out.println("DeadMessage: " + message.getMessage());
	}

	@Handler
	public final void handle(FilteredMessage message) {
		System.out.println("FilteredMessage: " + message.getMessage());
	}

}
