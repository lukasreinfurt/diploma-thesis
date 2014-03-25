package org.simtech.bootware.core;

import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.config.BusConfiguration;
import net.engio.mbassy.IPublicationErrorHandler.ConsoleLogger;

import org.simtech.bootware.core.events.Event;

public class EventBus {

	private MBassador<Event> eventBus;

	public EventBus() {
		eventBus = new MBassador<Event>(BusConfiguration.Default());

		ConsoleLogger error = new ConsoleLogger();
		eventBus.addErrorHandler(error);

	}

	public void publish(Event event) {
		eventBus.publish(event);
	}

	public void subscribe(Object subscriber) {
		eventBus.subscribe(subscriber);
	}

	public void unsubscribe(Object subscriber) {
		eventBus.unsubscribe(subscriber);
	}
}
