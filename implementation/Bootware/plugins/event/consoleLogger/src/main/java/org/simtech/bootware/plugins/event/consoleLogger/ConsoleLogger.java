package org.simtech.bootware.plugins.event.consoleLogger;

import java.util.Map;

import org.simtech.bootware.core.ConfigurationWrapper;
import org.simtech.bootware.core.events.BaseEvent;
import org.simtech.bootware.core.exceptions.InitializeException;
import org.simtech.bootware.core.plugins.AbstractBasePlugin;
import org.simtech.bootware.core.plugins.EventPlugin;

import net.engio.mbassy.common.DeadMessage;
import net.engio.mbassy.common.FilteredMessage;
import net.engio.mbassy.listener.Handler;

/**
 * An event plugin that prints all events to the console.
 */
public class ConsoleLogger extends AbstractBasePlugin implements EventPlugin {

	public ConsoleLogger() {}

	/**
	 * Implements the initialize operation defined in @see org.simtech.bootware.core.plugins.Plugin
	 */
	public final void initialize(final Map<String, ConfigurationWrapper> configurationList) throws InitializeException {
		// no op
	}

	/**
	 * Implements the initialize operation defined in @see org.simtech.bootware.core.plugins.Plugin
	 */
	public final void shutdown() {
		// no op
	}

	/**
	 * Implements an event handler that reacts to events of the type @see org.simtech.bootware.core.events.BaseEvent
	 */
	@Handler
	public final void handle(final BaseEvent event) {

		// Shorten severity to 4 characters.
		final String severity = event.getSeverity().toString();
		String shortSeverity = severity;
		final Integer maxLenght = 4;
		if (severity != null && severity.length() >= maxLenght) {
			shortSeverity = severity.substring(0, maxLenght);
		}

		System.out.println("[" + shortSeverity + "] " + event.getMessage());
	}

	/**
	 * Implements an event handler that reacts to DeadMessage events.
	 * <p>
	 * DeadMessage events are published by squirrel-foundation when an event is
	 * published to which nobody subscribed.
	 */
	@Handler
	public final void handle(final DeadMessage message) {
		System.out.println("DeadMessage: " + message.getMessage());
	}

	/**
	 * Implements an event handler that reacts to FilteredMessage events.
	 * <p>
	 * FilteredMessage events are published by squirrel-foundation when an event is
	 * published that doesn't reach any subscriber because it doesn't pass any filters.
	 */
	@Handler
	public final void handle(final FilteredMessage message) {
		System.out.println("FilteredMessage: " + message.getMessage());
	}

}
