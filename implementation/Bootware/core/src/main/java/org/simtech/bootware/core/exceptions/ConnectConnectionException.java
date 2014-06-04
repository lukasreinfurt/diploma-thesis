package org.simtech.bootware.core.exceptions;

@SuppressWarnings("serial")
public class ConnectConnectionException extends ConnectionException {
	public ConnectConnectionException() { super(); }
	public ConnectConnectionException(final String message) { super(message); }
	public ConnectConnectionException(final String message, final Throwable cause) { super(message, cause); }
	public ConnectConnectionException(final Throwable cause) { super(cause); }
}
