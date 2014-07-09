package org.simtech.bootware.core.exceptions;

@SuppressWarnings("serial")
public class ProvisionApplicationException extends ApplicationException {
	public ProvisionApplicationException() { super(); }
	public ProvisionApplicationException(final String message) { super(message); }
	public ProvisionApplicationException(final String message, final Throwable cause) { super(message, cause); }
	public ProvisionApplicationException(final Throwable cause) { super(cause); }
}
