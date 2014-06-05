package org.simtech.bootware.core;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;

public class Context {

	private String infrastructureType;
	private String connectionType;
	private String payloadType;
	private Map<String, Credentials> credentialsList = new HashMap<String, Credentials>();

	public Context() {}

	public final void setInfrastructureType(final String type) {
		infrastructureType = type;
	}

	@XmlElement(required = true)
	public final String getInfrastructureType() {
		return infrastructureType;
	}

	public final void setConnectionType(final String type) {
		connectionType = type;
	}

	@XmlElement(required = true)
	public final String getConnectionType() {
		return connectionType;
	}

	public final void setPayloadType(final String type) {
		payloadType = type;
	}

	@XmlElement(required = true)
	public final String getPayloadType() {
		return payloadType;
	}

	public final void setCredentialsList(final Map<String, Credentials> map) {
		credentialsList = map;
	}

	public final Map<String, Credentials> getCredentialsList() {
		return credentialsList;
	}

	public final Credentials getCredentialsFor(final String entry) {
		return credentialsList.get(entry);
	}
}
