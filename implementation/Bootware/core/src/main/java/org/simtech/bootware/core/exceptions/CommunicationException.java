package org.simtech.bootware.core.exceptions;

@SuppressWarnings("serial")
public class CommunicationException extends Exception {
	public CommunicationException() { super(); }
	public CommunicationException(final String message) { super(message); }
	public CommunicationException(final String message, final Throwable cause) { super(message, cause); }
	public CommunicationException(final Throwable cause) { super(cause); }
}
