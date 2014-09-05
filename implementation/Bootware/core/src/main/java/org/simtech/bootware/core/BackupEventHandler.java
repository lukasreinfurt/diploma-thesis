package org.simtech.bootware.core;

import org.simtech.bootware.core.events.BaseEvent;

import net.engio.mbassy.common.DeadMessage;
import net.engio.mbassy.common.FilteredMessage;
import net.engio.mbassy.listener.Handler;

/**
 * A backup event handler for dead and filtered messages.
 * <p>
 * It handles events that have no subscribers associated with them. This is
 * mainly useful when there are no event plugins loaded yet or anymore, e.g. at
 * beginning and at the end of the bootware execution.
 */
public class BackupEventHandler {

	public BackupEventHandler() {};

	/**
	 * Implements an event handler that reacts to DeadMessage events.
	 * <p>
	 * DeadMessage events are published by squirrel-foundation when an event is
	 * published to which nobody subscribed.
	 */
	@Handler
	public final void handle(final DeadMessage message) {
		final Object originalObject = message.getMessage();
		if (originalObject instanceof BaseEvent) {
			final BaseEvent originalEvent = (BaseEvent) originalObject;
			System.out.println("[DEAD] [" + originalEvent.getSeverity() + "] " + originalEvent.getMessage());
		}
	}

	/**
	 * Implements an event handler that reacts to FilteredMessage events.
	 * <p>
	 * FilteredMessage events are published by squirrel-foundation when an event is
	 * published that doesn't reach any subscriber because it doesn't pass any filters.
	 */
	@Handler
	public final void handle(final FilteredMessage message) {
		final Object originalObject = message.getMessage();
		if (originalObject instanceof BaseEvent) {
			final BaseEvent originalEvent = (BaseEvent) originalObject;
			System.out.println("[FILT] [" + originalEvent.getSeverity() + "] " + originalEvent.getMessage());
		}
	}

}
