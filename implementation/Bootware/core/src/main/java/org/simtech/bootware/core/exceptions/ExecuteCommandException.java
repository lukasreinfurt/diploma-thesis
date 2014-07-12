package org.simtech.bootware.core.exceptions;

@SuppressWarnings("serial")
public class ExecuteCommandException extends CommunicationException {
	public ExecuteCommandException() { super(); }
	public ExecuteCommandException(final String message) { super(message); }
	public ExecuteCommandException(final String message, final Throwable cause) { super(message, cause); }
	public ExecuteCommandException(final Throwable cause) { super(cause); }
}
