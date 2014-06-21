package org.simtech.bootware.core.exceptions;

@SuppressWarnings("serial")
public class ConfigurationException extends ContextException {
	public ConfigurationException() { super(); }
	public ConfigurationException(final String message) { super(message); }
	public ConfigurationException(final String message, final Throwable cause) { super(message, cause); }
	public ConfigurationException(final Throwable cause) { super(cause); }
}
