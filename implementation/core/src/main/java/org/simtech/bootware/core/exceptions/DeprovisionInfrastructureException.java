package org.simtech.bootware.core.exceptions;

@SuppressWarnings("serial")
public class DeprovisionInfrastructureException extends InfrastructureException {
	public DeprovisionInfrastructureException() { super(); }
	public DeprovisionInfrastructureException(String message) { super(message); }
	public DeprovisionInfrastructureException(String message, Throwable cause) { super(message, cause); }
	public DeprovisionInfrastructureException(Throwable cause) { super(cause); }
}
