package org.simtech.bootware.core.exceptions;

@SuppressWarnings("serial")
public class StartApplicationException extends ApplicationException {
	public StartApplicationException() { super(); }
	public StartApplicationException(final String message) { super(message); }
	public StartApplicationException(final String message, final Throwable cause) { super(message, cause); }
	public StartApplicationException(final Throwable cause) { super(cause); }
}
