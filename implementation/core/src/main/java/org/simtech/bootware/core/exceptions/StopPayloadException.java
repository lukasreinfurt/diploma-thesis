package org.simtech.bootware.core.exceptions;

public class StopPayloadException extends PayloadException {
	public StopPayloadException() { super(); }
	public StopPayloadException(String message) { super(message); }
	public StopPayloadException(String message, Throwable cause) { super(message, cause); }
	public StopPayloadException(Throwable cause) { super(cause); }
}
