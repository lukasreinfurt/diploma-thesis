package org.simtech.bootware.core.exceptions;

@SuppressWarnings("serial")
public class DeprovisionPayloadException extends PayloadException {
	public DeprovisionPayloadException() { super(); }
	public DeprovisionPayloadException(String message) { super(message); }
	public DeprovisionPayloadException(String message, Throwable cause) { super(message, cause); }
	public DeprovisionPayloadException(Throwable cause) { super(cause); }
}
