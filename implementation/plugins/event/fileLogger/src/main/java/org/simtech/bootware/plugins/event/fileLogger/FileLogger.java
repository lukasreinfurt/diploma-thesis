package org.simtech.bootware.plugins.event.fileLogger;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.simtech.bootware.core.events.BaseEvent;
import org.simtech.bootware.core.plugins.AbstractEventPlugin;

import net.engio.mbassy.common.DeadMessage;
import net.engio.mbassy.common.FilteredMessage;
import net.engio.mbassy.listener.Handler;

public class FileLogger extends AbstractEventPlugin {

	private PrintWriter writer;

	public final void initialize() {
		try {
			writer = new PrintWriter("filelogger.log", "UTF-8");
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public final void shutdown() {
		writer.close();
	}

	@Handler
	public final void handle(BaseEvent event) {
		writer.println(event.getTimestamp() + ": " + event.getMessage());
	}

	@Handler
	public final void handle(DeadMessage message) {
		writer.println("DeadMessage: " + message.getMessage());
	}

	@Handler
	public final void handle(FilteredMessage message) {
		writer.println("FilteredMessage: " + message.getMessage());
	}

}
