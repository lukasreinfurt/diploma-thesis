package org.simtech.bootware.core.exceptions;

@SuppressWarnings("serial")
public class InfrastructureException extends Exception {
	public InfrastructureException() { super(); }
	public InfrastructureException(final String message) { super(message); }
	public InfrastructureException(final String message, final Throwable cause) { super(message, cause); }
	public InfrastructureException(final Throwable cause) { super(cause); }
}
