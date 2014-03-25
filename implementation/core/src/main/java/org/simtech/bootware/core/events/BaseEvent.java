package org.simtech.bootware.core.events;

import java.util.Date;
import java.text.SimpleDateFormat;

public class BaseEvent implements Event {

	protected String timestamp;
	protected String message;

	protected BaseEvent() {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss:SSS");
		timestamp = sdf.format(date);
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
}
