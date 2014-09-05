package org.simtech.bootware.core.events;

/**
 * An event type published by provision plugins.
 */
public class ProvisionPluginEvent extends PluginEvent {

	public ProvisionPluginEvent(final Severity severity, final String message) {
		super(severity, message);
	}

}
