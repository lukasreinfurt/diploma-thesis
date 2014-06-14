package org.simtech.bootware.core.exceptions;

@SuppressWarnings("serial")
public class SetCredentialsException extends Exception {
	public SetCredentialsException() { super(); }
	public SetCredentialsException(final String message) { super(message); }
	public SetCredentialsException(final String message, final Throwable cause) { super(message, cause); }
	public SetCredentialsException(final Throwable cause) { super(cause); }
}
