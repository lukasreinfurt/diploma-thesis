package org.simtech.bootware.core.exceptions;

@SuppressWarnings("serial")
public class StartPayloadException extends PayloadException {
	public StartPayloadException() { super(); }
	public StartPayloadException(final String message) { super(message); }
	public StartPayloadException(final String message, final Throwable cause) { super(message, cause); }
	public StartPayloadException(final Throwable cause) { super(cause); }
}
