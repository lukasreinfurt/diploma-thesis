package org.simtech.bootware.core.exceptions;

@SuppressWarnings("serial")
public class InitializePluginManagerException extends PluginException {
	public InitializePluginManagerException() { super(); }
	public InitializePluginManagerException(final String message) { super(message); }
	public InitializePluginManagerException(final String message, final Throwable cause) { super(message, cause); }
	public InitializePluginManagerException(final Throwable cause) { super(cause); }
}
