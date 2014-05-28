package org.simtech.bootware.core.exceptions;

public class DisconnectException extends ConnectionException {
	public DisconnectException() { super(); }
	public DisconnectException(String message) { super(message); }
	public DisconnectException(String message, Throwable cause) { super(message, cause); }
	public DisconnectException(Throwable cause) { super(cause); }
}
