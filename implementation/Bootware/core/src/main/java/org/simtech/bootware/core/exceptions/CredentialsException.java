package org.simtech.bootware.core.exceptions;

@SuppressWarnings("serial")
public class CredentialsException extends ContextException {
	public CredentialsException() { super(); }
	public CredentialsException(final String message) { super(message); }
	public CredentialsException(final String message, final Throwable cause) { super(message, cause); }
	public CredentialsException(final Throwable cause) { super(cause); }
}
