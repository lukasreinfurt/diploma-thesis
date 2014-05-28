package org.simtech.bootware.core.exceptions;

@SuppressWarnings("serial")
public class UnloadPluginException extends PluginException {
	public UnloadPluginException() { super(); }
	public UnloadPluginException(String message) { super(message); }
	public UnloadPluginException(String message, Throwable cause) { super(message, cause); }
	public UnloadPluginException(Throwable cause) { super(cause); }
}
