package org.simtech.bootware.core;

public class Context {
	private String infrastructureType;
	private String connectionType;
	private String payloadType;

	public final void setInfrastructureType(String type) {
		this.infrastructureType = type;
	}

	public final String getInfrastructureType() {
		return infrastructureType;
	}

	public final void setConnectionType(String type) {
		this.connectionType = type;
	}

	public final String getConnectionType() {
		return connectionType;
	}

	public final void setPayloadType(String type) {
		this.payloadType = type;
	}

	public final String getPayloadType() {
		return payloadType;
	}
}
