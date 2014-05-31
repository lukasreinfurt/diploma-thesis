package org.simtech.bootware.core.exceptions;

@SuppressWarnings("serial")
public class StopPayloadException extends PayloadException {
	public StopPayloadException() { super(); }
	public StopPayloadException(final String message) { super(message); }
	public StopPayloadException(final String message, final Throwable cause) { super(message, cause); }
	public StopPayloadException(final Throwable cause) { super(cause); }
}
