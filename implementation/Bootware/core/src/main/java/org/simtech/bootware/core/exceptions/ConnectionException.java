package org.simtech.bootware.core.exceptions;

@SuppressWarnings("serial")
public class ConnectionException extends Exception {
	public ConnectionException() { super(); }
	public ConnectionException(final String message) { super(message); }
	public ConnectionException(final String message, final Throwable cause) { super(message, cause); }
	public ConnectionException(final Throwable cause) { super(cause); }
}
