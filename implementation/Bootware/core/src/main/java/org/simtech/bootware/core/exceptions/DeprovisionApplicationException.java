package org.simtech.bootware.core.exceptions;

@SuppressWarnings("serial")
public class DeprovisionApplicationException extends ApplicationException {
	public DeprovisionApplicationException() { super(); }
	public DeprovisionApplicationException(final String message) { super(message); }
	public DeprovisionApplicationException(final String message, final Throwable cause) { super(message, cause); }
	public DeprovisionApplicationException(final Throwable cause) { super(cause); }
}
