package org.simtech.bootware.core.exceptions;

@SuppressWarnings("serial")
public class ContextException extends Exception {
	public ContextException() { super(); }
	public ContextException(final String message) { super(message); }
	public ContextException(final String message, final Throwable cause) { super(message, cause); }
	public ContextException(final Throwable cause) { super(cause); }
}
