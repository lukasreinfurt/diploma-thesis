package org.simtech.bootware.core.events;

/**
 * An event type published by resource plugins.
 */
public class ResourcePluginEvent extends PluginEvent {

	public ResourcePluginEvent(final Severity severity, final String message) {
		super(severity, message);
	}

}
