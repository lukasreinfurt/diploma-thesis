package org.simtech.bootware.core.events;

/**
 * An event type published by the bootware core.
 */
public class CoreEvent extends BaseEvent {

	public CoreEvent(final Severity severity, final String message) {
		super(severity, message);
	}

}
