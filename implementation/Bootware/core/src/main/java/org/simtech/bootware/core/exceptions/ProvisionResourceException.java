package org.simtech.bootware.core.exceptions;

@SuppressWarnings("serial")
public class ProvisionResourceException extends ResourceException {
	public ProvisionResourceException() { super(); }
	public ProvisionResourceException(final String message) { super(message); }
	public ProvisionResourceException(final String message, final Throwable cause) { super(message, cause); }
	public ProvisionResourceException(final Throwable cause) { super(cause); }
}
