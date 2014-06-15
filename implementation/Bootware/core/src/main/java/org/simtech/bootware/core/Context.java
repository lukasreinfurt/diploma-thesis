package org.simtech.bootware.core;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;

import org.simtech.bootware.core.exceptions.CredentialsException;

public class Context {

	private String infrastructurePlugin;
	private String callPayloadPlugin;
	private String connectionPlugin;
	private String servicePackageReference;
	private String payloadPlugin;
	private Map<String, CredentialsWrapper> credentialsList = new HashMap<String, CredentialsWrapper>();

	public Context() {}

	public final void setInfrastructurePlugin(final String plugin) {
		infrastructurePlugin = plugin;
	}

	@XmlElement(required = true)
	public final String getInfrastructurePlugin() {
		return infrastructurePlugin;
	}

	public final void setCallPayloadPlugin(final String plugin) {
		callPayloadPlugin = plugin;
	}

	public final String getCallPayloadPlugin() {
		return callPayloadPlugin;
	}

	public final void setConnectionPlugin(final String plugin) {
		connectionPlugin = plugin;
	}

	@XmlElement(required = true)
	public final String getConnectionPlugin() {
		return connectionPlugin;
	}

	public final void setServicePackageReference(final String reference) {
		servicePackageReference = reference;
	}

	public final String getServicePackageReference() {
		return servicePackageReference;
	}

	public final void setPayloadPlugin(final String plugin) {
		payloadPlugin = plugin;
	}

	@XmlElement(required = true)
	public final String getPayloadPlugin() {
		return payloadPlugin;
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
