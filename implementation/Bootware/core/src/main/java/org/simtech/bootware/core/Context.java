package org.simtech.bootware.core;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;

import org.simtech.bootware.core.exceptions.CredentialsException;

public class Context {

	private String infrastructureType;
	private String connectionType;
	private String payloadType;
	private Map<String, CredentialsWrapper> credentialsList = new HashMap<String, CredentialsWrapper>();

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

	public final void setCredentialsList(final Map<String, CredentialsWrapper> map) {
		credentialsList = map;
	}

	public final Map<String, CredentialsWrapper> getCredentialsList() {
		return credentialsList;
	}

	public final CredentialsWrapper getCredentialsFor(final String entry) throws CredentialsException {
		final CredentialsWrapper credentials = credentialsList.get(entry);
		if (credentials == null) {
			throw new CredentialsException("Credentials for " + entry + " could not be found.");
		}
		return credentials;
	}
}
