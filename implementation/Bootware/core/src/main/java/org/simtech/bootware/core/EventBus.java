package org.simtech.bootware.core;

import org.simtech.bootware.core.events.Event;

import net.engio.mbassy.IPublicationErrorHandler.ConsoleLogger;
import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.config.BusConfiguration;

/**
 * A thin wrapper layer around the MBassador library.
 * <p>
 * The event bus offers PubSub functionality to distribute events between
 * publishers and subscribers.
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
	public final void publish(final Event event) {
		eventBus.publish(event);
	}

	/**
	 * Subscribe to the event bus.
	 * <p>
	 * The object can implement zero to many handle methods with the handle
	 * annotation to react to specific events. If it doesn't implement any handle
	 * methods it will be ignored.
	 * For example:
	 * <code><pre>
	 *   @Handler
	 *   public final void handle(final BaseEvent event) {
	 *     System.out.println("[" + event.getSeverity() + "] " + event.getMessage());
	 *   }
	 * </pre></code>
	 *
	 * @param subscriber Object to be subscribed.
	 */
	public final void subscribe(final Object subscriber) {
		eventBus.subscribe(subscriber);
	}

	/**
	 * Unsubscribe from the event bus.
	 *
	 * @param subscriber Object to be unsubscribed.
	 */
	public final void unsubscribe(final Object subscriber) {
		eventBus.unsubscribe(subscriber);
	}
}
