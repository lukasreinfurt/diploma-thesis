package org.simtech.bootware.core.exceptions;

@SuppressWarnings("serial")
public class DeployException extends Exception {
	public DeployException() { super(); }
	public DeployException(final String message) { super(message); }
	public DeployException(final String message, final Throwable cause) { super(message, cause); }
	public DeployException(final Throwable cause) { super(cause); }
}
