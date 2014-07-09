package org.simtech.bootware.core.exceptions;

@SuppressWarnings("serial")
public class DeprovisionResourceException extends ResourceException {
	public DeprovisionResourceException() { super(); }
	public DeprovisionResourceException(final String message) { super(message); }
	public DeprovisionResourceException(final String message, final Throwable cause) { super(message, cause); }
	public DeprovisionResourceException(final Throwable cause) { super(cause); }
}
