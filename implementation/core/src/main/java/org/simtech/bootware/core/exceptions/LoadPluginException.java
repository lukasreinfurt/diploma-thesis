package org.simtech.bootware.core.exceptions;

@SuppressWarnings("serial")
public class LoadPluginException extends PluginException {
	public LoadPluginException() { super(); }
	public LoadPluginException(final String message) { super(message); }
	public LoadPluginException(final String message, final Throwable cause) { super(message, cause); }
	public LoadPluginException(final Throwable cause) { super(cause); }
}
