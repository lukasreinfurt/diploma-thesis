package org.simtech.bootware.core.events;

public class FSMEvent extends BaseEvent {

	public FSMEvent(final Severity severity, final String message) {
		super(severity, message);
	}

}
