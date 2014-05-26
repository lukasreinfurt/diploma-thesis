package org.simtech.bootware.core;

public class Context {
	String infrastructureType;
	String connectionType;
	String payloadType;

	public void setInfrastructureType(String type) {
		this.infrastructureType = type;
	}

	public String getInfrastructureType() {
		return infrastructureType;
	}

	public void setConnectionType(String type) {
		this.connectionType = type;
	}

	public String getConnectionType() {
		return connectionType;
	}

	public void setPayloadType(String type) {
		this.payloadType = type;
	}

	public String getPayloadType() {
		return payloadType;
	}
}
