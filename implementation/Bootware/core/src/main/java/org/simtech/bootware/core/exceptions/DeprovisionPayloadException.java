package org.simtech.bootware.core.exceptions;

@SuppressWarnings("serial")
public class DeprovisionPayloadException extends PayloadException {
	public DeprovisionPayloadException() { super(); }
	public DeprovisionPayloadException(final String message) { super(message); }
	public DeprovisionPayloadException(final String message, final Throwable cause) { super(message, cause); }
	public DeprovisionPayloadException(final Throwable cause) { super(cause); }
}
