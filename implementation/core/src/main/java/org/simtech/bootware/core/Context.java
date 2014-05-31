package org.simtech.bootware.core;

public class Context {
	private String infrastructureType;
	private String connectionType;
	private String payloadType;

	public Context() {}

	public final void setInfrastructureType(final String type) {
		this.infrastructureType = type;
	}

	public final String getInfrastructureType() {
		return infrastructureType;
	}

	public final void setConnectionType(final String type) {
		this.connectionType = type;
	}

	public final String getConnectionType() {
		return connectionType;
	}

	public final void setPayloadType(final String type) {
		this.payloadType = type;
	}

	public final String getPayloadType() {
		return payloadType;
	}
}
