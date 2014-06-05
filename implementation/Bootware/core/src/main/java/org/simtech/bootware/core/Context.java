package org.simtech.bootware.core;

import javax.xml.bind.annotation.XmlElement;

public class Context {

	private String infrastructureType;
	private String connectionType;
	private String payloadType;

	public Context() {}

	public final void setInfrastructureType(final String type) {
		this.infrastructureType = type;
	}

	@XmlElement(required = true)
	public final String getInfrastructureType() {
		return infrastructureType;
	}

	public final void setConnectionType(final String type) {
		this.connectionType = type;
	}

	@XmlElement(required = true)
	public final String getConnectionType() {
		return connectionType;
	}

	public final void setPayloadType(final String type) {
		this.payloadType = type;
	}

	@XmlElement(required = true)
	public final String getPayloadType() {
		return payloadType;
	}
}
