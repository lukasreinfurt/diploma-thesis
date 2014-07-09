package org.simtech.bootware.core.exceptions;

@SuppressWarnings("serial")
public class ResourceException extends Exception {
	public ResourceException() { super(); }
	public ResourceException(final String message) { super(message); }
	public ResourceException(final String message, final Throwable cause) { super(message, cause); }
	public ResourceException(final Throwable cause) { super(cause); }
}
