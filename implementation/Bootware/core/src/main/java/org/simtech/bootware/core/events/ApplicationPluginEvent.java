package org.simtech.bootware.core.events;

/**
 * An event type published by application plugins.
 */
public class ApplicationPluginEvent extends PluginEvent {

	public ApplicationPluginEvent(final Severity severity, final String message) {
		super(severity, message);
	}

}
