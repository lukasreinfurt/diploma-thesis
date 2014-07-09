package org.simtech.bootware.core.exceptions;

@SuppressWarnings("serial")
public class ApplicationException extends Exception {
	public ApplicationException() { super(); }
	public ApplicationException(final String message) { super(message); }
	public ApplicationException(final String message, final Throwable cause) { super(message, cause); }
	public ApplicationException(final Throwable cause) { super(cause); }
}
