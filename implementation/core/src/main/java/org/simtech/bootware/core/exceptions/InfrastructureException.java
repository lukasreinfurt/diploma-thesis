package org.simtech.bootware.core.exceptions;

@SuppressWarnings("serial")
public class InfrastructureException extends Exception {
	public InfrastructureException() { super(); }
	public InfrastructureException(String message) { super(message); }
	public InfrastructureException(String message, Throwable cause) { super(message, cause); }
	public InfrastructureException(Throwable cause) { super(cause); }
}
