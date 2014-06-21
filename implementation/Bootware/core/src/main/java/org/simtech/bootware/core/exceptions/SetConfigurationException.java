package org.simtech.bootware.core.exceptions;

@SuppressWarnings("serial")
public class SetConfigurationException extends Exception {
	public SetConfigurationException() { super(); }
	public SetConfigurationException(final String message) { super(message); }
	public SetConfigurationException(final String message, final Throwable cause) { super(message, cause); }
	public SetConfigurationException(final Throwable cause) { super(cause); }
}
