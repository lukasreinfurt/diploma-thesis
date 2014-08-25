package org.simtech.bootware.plugins.event.fileLogger;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.simtech.bootware.core.ConfigurationWrapper;
import org.simtech.bootware.core.events.BaseEvent;
import org.simtech.bootware.core.plugins.AbstractBasePlugin;
import org.simtech.bootware.core.plugins.EventPlugin;

import net.engio.mbassy.common.DeadMessage;
import net.engio.mbassy.common.FilteredMessage;
import net.engio.mbassy.listener.Handler;

/**
 * An event plugin that writes all events to a file.
 */
public class FileLogger extends AbstractBasePlugin implements EventPlugin {

	private PrintWriter writer;

	public FileLogger() {}

	/**
	 * Implements the initialize operation defined in @see org.simtech.bootware.core.plugins.Plugin
	 */
	public final void initialize(final Map<String, ConfigurationWrapper> configurationList) {
		// Open writer object to be used later.
		try {
			writer = new PrintWriter("filelogger.log", "UTF-8");
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Implements the shutdown operation defined in @see org.simtech.bootware.core.plugins.Plugin
	 */
	public final void shutdown() {
		writer.close();
	}

	/**
	 * Implements an event handler that reacts to events of the type @see org.simtech.bootware.core.events.BaseEvent
	 */
	@Handler
	public final void handle(final BaseEvent event) {
		writer.println(event.getTimestamp() + ": " + event.getMessage());
	}

	/**
	 * Implements an event handler that reacts to DeadMessage events.
	 * <p>
	 * DeadMessage events are published by squirrel-foundation when an event is
	 * published to which nobody subscribed.
	 */
	@Handler
	public final void handle(final DeadMessage message) {
		writer.println("DeadMessage: " + message.getMessage());
	}

	/**
	 * Implements an event handler that reacts to FilteredMessage events.
	 * <p>
	 * FilteredMessage events are published by squirrel-foundation when an event is
	 * published that doesn't reach any subscriber because it doesn't pass any filters.
	 */
	@Handler
	public final void handle(final FilteredMessage message) {
		writer.println("FilteredMessage: " + message.getMessage());
	}

}
