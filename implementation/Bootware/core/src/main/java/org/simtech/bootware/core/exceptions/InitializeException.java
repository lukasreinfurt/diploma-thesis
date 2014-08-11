package org.simtech.bootware.core.exceptions;

@SuppressWarnings("serial")
public class InitializeException extends Exception {
	public InitializeException() { super(); }
	public InitializeException(final String message) { super(message); }
	public InitializeException(final String message, final Throwable cause) { super(message, cause); }
	public InitializeException(final Throwable cause) { super(cause); }
}
