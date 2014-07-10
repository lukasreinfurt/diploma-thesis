package org.simtech.bootware.plugins.event.fileLogger;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import org.simtech.bootware.core.ConfigurationWrapper;
import org.simtech.bootware.core.events.BaseEvent;
import org.simtech.bootware.core.plugins.AbstractBasePlugin;
import org.simtech.bootware.core.plugins.EventPlugin;

import net.engio.mbassy.common.DeadMessage;
import net.engio.mbassy.common.FilteredMessage;
import net.engio.mbassy.listener.Handler;

public class FileLogger extends AbstractBasePlugin implements EventPlugin {

	private PrintWriter writer;

	public FileLogger() {}

	public final void initialize(final ConfigurationWrapper configuration) {
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

	public final void shutdown() {
		writer.close();
	}

	@Handler
	public final void handle(final BaseEvent event) {
		writer.println(event.getTimestamp() + ": " + event.getMessage());
	}

	@Handler
	public final void handle(final DeadMessage message) {
		writer.println("DeadMessage: " + message.getMessage());
	}

	@Handler
	public final void handle(final FilteredMessage message) {
		writer.println("FilteredMessage: " + message.getMessage());
	}

}
