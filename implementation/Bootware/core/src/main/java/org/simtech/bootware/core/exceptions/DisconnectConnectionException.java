package org.simtech.bootware.core.exceptions;

@SuppressWarnings("serial")
public class DisconnectConnectionException extends CommunicationException {
	public DisconnectConnectionException() { super(); }
	public DisconnectConnectionException(final String message) { super(message); }
	public DisconnectConnectionException(final String message, final Throwable cause) { super(message, cause); }
	public DisconnectConnectionException(final Throwable cause) { super(cause); }
}
