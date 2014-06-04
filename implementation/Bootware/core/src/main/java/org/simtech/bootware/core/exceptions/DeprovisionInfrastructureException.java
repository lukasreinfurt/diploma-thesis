package org.simtech.bootware.core.exceptions;

@SuppressWarnings("serial")
public class DeprovisionInfrastructureException extends InfrastructureException {
	public DeprovisionInfrastructureException() { super(); }
	public DeprovisionInfrastructureException(final String message) { super(message); }
	public DeprovisionInfrastructureException(final String message, final Throwable cause) { super(message, cause); }
	public DeprovisionInfrastructureException(final Throwable cause) { super(cause); }
}
