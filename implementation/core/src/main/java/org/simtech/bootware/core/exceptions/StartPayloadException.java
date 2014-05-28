package org.simtech.bootware.core.exceptions;

@SuppressWarnings("serial")
public class StartPayloadException extends PayloadException {
	public StartPayloadException() { super(); }
	public StartPayloadException(String message) { super(message); }
	public StartPayloadException(String message, Throwable cause) { super(message, cause); }
	public StartPayloadException(Throwable cause) { super(cause); }
}
