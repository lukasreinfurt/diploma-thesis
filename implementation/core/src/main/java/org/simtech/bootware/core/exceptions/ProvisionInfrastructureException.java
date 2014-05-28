package org.simtech.bootware.core.exceptions;

@SuppressWarnings("serial")
public class ProvisionInfrastructureException extends InfrastructureException {
	public ProvisionInfrastructureException() { super(); }
	public ProvisionInfrastructureException(String message) { super(message); }
	public ProvisionInfrastructureException(String message, Throwable cause) { super(message, cause); }
	public ProvisionInfrastructureException(Throwable cause) { super(cause); }
}
