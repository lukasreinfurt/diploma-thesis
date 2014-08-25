package org.simtech.bootware.core.events;

/**
 * An event type published by the finite state machine.
 */
public class FSMEvent extends BaseEvent {

	public FSMEvent(final Severity severity, final String message) {
		super(severity, message);
	}

}
