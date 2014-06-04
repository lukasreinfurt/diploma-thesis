package org.simtech.bootware.core.exceptions;

@SuppressWarnings("serial")
public class ProvisionPayloadException extends PayloadException {
	public ProvisionPayloadException() { super(); }
	public ProvisionPayloadException(final String message) { super(message); }
	public ProvisionPayloadException(final String message, final Throwable cause) { super(message, cause); }
	public ProvisionPayloadException(final Throwable cause) { super(cause); }
}
