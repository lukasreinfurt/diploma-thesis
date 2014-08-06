package org.simtech.bootware.core.exceptions;

@SuppressWarnings("serial")
public class ContextMappingException extends Exception {
	public ContextMappingException() { super(); }
	public ContextMappingException(final String message) { super(message); }
	public ContextMappingException(final String message, final Throwable cause) { super(message, cause); }
	public ContextMappingException(final Throwable cause) { super(cause); }
}
