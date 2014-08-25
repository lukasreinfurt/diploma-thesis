package org.simtech.bootware.core.events;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Abstract base event that should be used as baseline for all Bootware events.
 */
public class BaseEvent implements Event {

	protected String timestamp;
	protected Severity severity;
	protected String message;

	/**
	 * Creates a base event and sets its timestamp to the current time.
	 *
	 * @param s Severity of the event (e.g. INFO, ERROR, etc.)
	 * @param m The event message.
	 */
	protected BaseEvent(final Severity s, final String m) {
		final Date date = new Date();
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss:SSS");
		timestamp = sdf.format(date);
		severity = s;
		message = m;
	}

	/**
	 * Returns the event timestamp.
	 *
	 * @return The event timestamp in the format "yyyy/MM/dd hh:mm:ss:SSS".
	 */
	public final String getTimestamp() {
		return timestamp;
	}

	/**
	 * Sets the event severity.
	 *
	 * @param s Severity of the event (e.g. INFO, ERROR, etc.)
	 */
	public final void setSeverity(final Severity s) {
		severity = s;
	}

	/**
	 * Returns the event severity.
	 *
	 * @return The event severity.
	 */
	public final Severity getSeverity() {
		return severity;
	}

	/**
	 * Sets the event message.
	 *
	 * @param m The event message.
	 */
	public final void setMessage(final String m) {
		message = m;
	}

	/**
	 * @return The event message
	 */
	public final String getMessage() {
		return message;
	}
}
