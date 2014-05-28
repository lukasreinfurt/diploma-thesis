package org.simtech.bootware.core.exceptions;

public class ConnectException extends ConnectionException {
	public ConnectException() { super(); }
	public ConnectException(String message) { super(message); }
	public ConnectException(String message, Throwable cause) { super(message, cause); }
	public ConnectException(Throwable cause) { super(cause); }
}
