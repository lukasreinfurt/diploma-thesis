package org.simtech.bootware.core.events;

/**
 * An event type published by communication plugins.
 */
public class CommunicationPluginEvent extends PluginEvent {

	public CommunicationPluginEvent(final Severity severity, final String message) {
		super(severity, message);
	}

}
