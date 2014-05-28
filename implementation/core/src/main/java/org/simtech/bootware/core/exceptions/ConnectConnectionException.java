package org.simtech.bootware.core.exceptions;

public class ConnectConnectionException extends ConnectionException {
	public ConnectConnectionException() { super(); }
	public ConnectConnectionException(String message) { super(message); }
	public ConnectConnectionException(String message, Throwable cause) { super(message, cause); }
	public ConnectConnectionException(Throwable cause) { super(cause); }
}
