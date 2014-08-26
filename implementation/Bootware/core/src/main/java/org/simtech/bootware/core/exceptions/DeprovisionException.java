package org.simtech.bootware.core.exceptions;

@SuppressWarnings("serial")
public class DeprovisionException extends ProvisionException {
	public DeprovisionException() { super(); }
	public DeprovisionException(final String message) { super(message); }
	public DeprovisionException(final String message, final Throwable cause) { super(message, cause); }
	public DeprovisionException(final Throwable cause) { super(cause); }
}
