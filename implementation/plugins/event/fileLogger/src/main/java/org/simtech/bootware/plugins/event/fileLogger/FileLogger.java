package org.simtech.bootware.plugins.event.fileLogger;

import org.simtech.bootware.core.plugins.AbstractEventPlugin;
import org.simtech.bootware.core.events.BaseEvent;

import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Filter;
import net.engio.mbassy.common.DeadMessage;
import net.engio.mbassy.common.FilteredMessage;

import java.io.PrintWriter;
import java.io.FileNotFoundException;

public class FileLogger extends AbstractEventPlugin {

	private PrintWriter writer;

	public void initialize() {
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

	public void shutdown() {
		writer.close();
	}

	@Handler
	public void handle(BaseEvent event) {
		writer.println(event.getTimestamp() + ": " + event.getMessage());
	}

	@Handler
	public void handle(DeadMessage message) {
		writer.println("DeadMessage: " + message.getMessage());
	}

	@Handler
	public void handle(FilteredMessage message) {
		writer.println("FilteredMessage: " + message.getMessage());
	}

}
