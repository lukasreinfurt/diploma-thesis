package org.simtech.bootware.core.exceptions;

@SuppressWarnings("serial")
public class PayloadException extends Exception {
	public PayloadException() { super(); }
	public PayloadException(final String message) { super(message); }
	public PayloadException(final String message, final Throwable cause) { super(message, cause); }
	public PayloadException(final Throwable cause) { super(cause); }
}
