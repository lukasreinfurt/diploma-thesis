package org.simtech.bootware.core.exceptions;

public class LoadPluginException extends PluginException {
	public LoadPluginException() { super(); }
	public LoadPluginException(String message) { super(message); }
	public LoadPluginException(String message, Throwable cause) { super(message, cause); }
	public LoadPluginException(Throwable cause) { super(cause); }
}
