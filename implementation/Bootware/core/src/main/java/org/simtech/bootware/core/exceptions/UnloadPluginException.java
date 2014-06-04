package org.simtech.bootware.core.exceptions;

@SuppressWarnings("serial")
public class UnloadPluginException extends PluginException {
	public UnloadPluginException() { super(); }
	public UnloadPluginException(final String message) { super(message); }
	public UnloadPluginException(final String message, final Throwable cause) { super(message, cause); }
	public UnloadPluginException(final Throwable cause) { super(cause); }
}
