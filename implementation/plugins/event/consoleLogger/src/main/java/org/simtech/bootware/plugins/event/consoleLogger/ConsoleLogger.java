package org.simtech.bootware.plugins.event.consoleLogger;

import org.simtech.bootware.core.events.ErrorEvent;
import org.simtech.bootware.core.events.InfoEvent;
import org.simtech.bootware.core.events.StateTransitionEvent;
import org.simtech.bootware.core.events.SuccessEvent;
import org.simtech.bootware.core.events.WarningEvent;
import org.simtech.bootware.core.plugins.AbstractBasePlugin;
import org.simtech.bootware.core.plugins.EventPlugin;

import net.engio.mbassy.common.DeadMessage;
import net.engio.mbassy.common.FilteredMessage;
import net.engio.mbassy.listener.Handler;

public class ConsoleLogger extends AbstractBasePlugin implements EventPlugin {

	public ConsoleLogger() {

	}

	public final void initialize() {
		// no op
	}

	public final void shutdown() {
		// no op
	}

	@Handler
	public final void handle(final StateTransitionEvent event) {
		System.out.println("[TRANSITION] " + event.getMessage());
	}

	@Handler
	public final void handle(final InfoEvent event) {
		System.out.println("[INFO] " + event.getMessage());
	}

	@Handler
	public final void handle(final SuccessEvent event) {
		System.out.println("[SUCCESS] " + event.getMessage());
	}

	@Handler
	public final void handle(final WarningEvent event) {
		System.out.println("[WARNING] " + event.getMessage());
	}

	@Handler
	public final void handle(final ErrorEvent event) {
		System.out.println("[ERROR] " + event.getMessage());
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
