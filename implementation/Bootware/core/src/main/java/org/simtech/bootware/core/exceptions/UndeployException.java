package org.simtech.bootware.core.exceptions;

@SuppressWarnings("serial")
public class UndeployException extends Exception {
	public UndeployException() { super(); }
	public UndeployException(final String message) { super(message); }
	public UndeployException(final String message, final Throwable cause) { super(message, cause); }
	public UndeployException(final Throwable cause) { super(cause); }
}
