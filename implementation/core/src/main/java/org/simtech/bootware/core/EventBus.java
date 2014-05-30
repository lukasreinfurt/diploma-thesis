package org.simtech.bootware.core;

import org.simtech.bootware.core.events.Event;

import net.engio.mbassy.IPublicationErrorHandler.ConsoleLogger;
import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.config.BusConfiguration;

/**
 * A thin wrapper layer around the MBassador library.
 */
public class EventBus {

	private MBassador<Event> eventBus;

	/**
	 * Creates an event bus.
	 */
	public EventBus() {
		eventBus = new MBassador<Event>(BusConfiguration.Default());

		final ConsoleLogger error = new ConsoleLogger();
		eventBus.addErrorHandler(error);

	}

	/**
	 * Publish an event on the event bus.
	 *
	 * @param event Event to be published.
	 */
	public final void publish(Event event) {
		eventBus.publish(event);
	}

	/**
	 * Subscribe to the event bus.
	 *
	 * @param subscriber Object to be subscribed.
	 */
	public final void subscribe(Object subscriber) {
		eventBus.subscribe(subscriber);
	}

	/**
	 * Unsubscribe from the event bus.
	 *
	 * @param subscriber Object to be unsubscribed.
	 */
	public final void unsubscribe(Object subscriber) {
		eventBus.unsubscribe(subscriber);
	}
}
