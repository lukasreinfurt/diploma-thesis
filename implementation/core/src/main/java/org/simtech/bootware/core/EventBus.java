package org.simtech.bootware.core;

import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.config.BusConfiguration;
import net.engio.mbassy.IPublicationErrorHandler.ConsoleLogger;

import org.simtech.bootware.core.events.Event;

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

		ConsoleLogger error = new ConsoleLogger();
		eventBus.addErrorHandler(error);

	}

	/**
	 * Publish an event on the event bus.
	 *
	 * @param event Event to be published.
	 */
	public void publish(Event event) {
		eventBus.publish(event);
	}

	/**
	 * Subscribe to the event bus.
	 *
	 * @param subscriber Object to be subscribed.
	 */
	public void subscribe(Object subscriber) {
		eventBus.subscribe(subscriber);
	}

	/**
	 * Unsubscribe from the event bus.
	 *
	 * @param subscriber Object to be unsubscribed.
	 */
	public void unsubscribe(Object subscriber) {
		eventBus.unsubscribe(subscriber);
	}
}
