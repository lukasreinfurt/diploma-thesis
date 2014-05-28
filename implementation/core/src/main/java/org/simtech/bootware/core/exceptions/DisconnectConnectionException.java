package org.simtech.bootware.core.exceptions;

@SuppressWarnings("serial")
public class DisconnectConnectionException extends ConnectionException {
	public DisconnectConnectionException() { super(); }
	public DisconnectConnectionException(String message) { super(message); }
	public DisconnectConnectionException(String message, Throwable cause) { super(message, cause); }
	public DisconnectConnectionException(Throwable cause) { super(cause); }
}
