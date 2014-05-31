package org.simtech.bootware.core.exceptions;

@SuppressWarnings("serial")
public class PluginException extends Exception {
	public PluginException() { super(); }
	public PluginException(final String message) { super(message); }
	public PluginException(final String message, final Throwable cause) { super(message, cause); }
	public PluginException(final Throwable cause) { super(cause); }
}
