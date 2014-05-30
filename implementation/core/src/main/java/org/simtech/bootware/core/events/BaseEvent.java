package org.simtech.bootware.core.events;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Abstract base event that should be used as baseline for all Bootware events.
 */
public class BaseEvent implements Event {

	protected String timestamp;
	protected String message;

	/**
	 * Creates a base event and sets its timestamp to the current time.
	 */
	protected BaseEvent() {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss:SSS");
		timestamp = sdf.format(date);
	}

	/**
	 * @return The event timestamp in the format "yyyy/MM/dd hh:mm:ss:SSS".
	 */
	public String getTimestamp() {
		return timestamp;
	}

	/**
	 * Sets the event message.
	 *
	 * @param message The event message.
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return The event message
	 */
	public String getMessage() {
		return message;
	}
}
