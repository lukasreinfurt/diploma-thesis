package org.simtech.bootware.core.exceptions;

@SuppressWarnings("serial")
public class ShutdownException extends Exception {
	public ShutdownException() { super(); }
	public ShutdownException(final String message) { super(message); }
	public ShutdownException(final String message, final Throwable cause) { super(message, cause); }
	public ShutdownException(final Throwable cause) { super(cause); }
}
