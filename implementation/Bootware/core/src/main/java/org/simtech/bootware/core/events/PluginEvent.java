package org.simtech.bootware.core.events;

public class PluginEvent extends BaseEvent {

	public PluginEvent(final Severity severity, final String message) {
		super(severity, message);
	}

}
