package org.simtech.bootware.core.exceptions;

@SuppressWarnings("serial")
public class PayloadException extends Exception {
	public PayloadException() { super(); }
	public PayloadException(String message) { super(message); }
	public PayloadException(String message, Throwable cause) { super(message, cause); }
	public PayloadException(Throwable cause) { super(cause); }
}
