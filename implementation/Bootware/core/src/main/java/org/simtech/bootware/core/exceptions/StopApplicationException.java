package org.simtech.bootware.core.exceptions;

@SuppressWarnings("serial")
public class StopApplicationException extends ApplicationException {
	public StopApplicationException() { super(); }
	public StopApplicationException(final String message) { super(message); }
	public StopApplicationException(final String message, final Throwable cause) { super(message, cause); }
	public StopApplicationException(final Throwable cause) { super(cause); }
}
