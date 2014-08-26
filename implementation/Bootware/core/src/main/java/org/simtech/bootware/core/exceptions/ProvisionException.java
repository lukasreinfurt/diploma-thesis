package org.simtech.bootware.core.exceptions;

@SuppressWarnings("serial")
public class ProvisionException extends Exception {
	public ProvisionException() { super(); }
	public ProvisionException(final String message) { super(message); }
	public ProvisionException(final String message, final Throwable cause) { super(message, cause); }
	public ProvisionException(final Throwable cause) { super(cause); }
}
