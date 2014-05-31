package org.simtech.bootware.core.exceptions;

@SuppressWarnings("serial")
public class ProvisionInfrastructureException extends InfrastructureException {
	public ProvisionInfrastructureException() { super(); }
	public ProvisionInfrastructureException(final String message) { super(message); }
	public ProvisionInfrastructureException(final String message, final Throwable cause) { super(message, cause); }
	public ProvisionInfrastructureException(final Throwable cause) { super(cause); }
}
