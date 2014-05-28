package org.simtech.bootware.core.exceptions;

public class ProvisionPayloadException extends PayloadException {
	public ProvisionPayloadException() { super(); }
	public ProvisionPayloadException(String message) { super(message); }
	public ProvisionPayloadException(String message, Throwable cause) { super(message, cause); }
	public ProvisionPayloadException(Throwable cause) { super(cause); }
}
