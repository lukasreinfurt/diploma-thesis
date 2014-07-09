package org.simtech.bootware.core;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;

import org.simtech.bootware.core.exceptions.ConfigurationException;

public class Context {

	private String resourcePlugin;
	private String callPayloadPlugin;
	private String communicationPlugin;
	private String servicePackageReference;
	private String payloadPlugin;
	private Map<String, ConfigurationWrapper> configurationList = new HashMap<String, ConfigurationWrapper>();

	public Context() {}

	public final void setResourcePlugin(final String plugin) {
		resourcePlugin = plugin;
	}

	@XmlElement(required = true)
	public final String getResourcePlugin() {
		return resourcePlugin;
	}

	public final void setCallPayloadPlugin(final String plugin) {
		callPayloadPlugin = plugin;
	}

	public final String getCallPayloadPlugin() {
		return callPayloadPlugin;
	}

	public final void setCommunicationPlugin(final String plugin) {
		communicationPlugin = plugin;
	}

	@XmlElement(required = true)
	public final String getCommunicationPlugin() {
		return communicationPlugin;
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

	public final void setConfigurationList(final Map<String, ConfigurationWrapper> map) {
		configurationList = map;
	}

	public final Map<String, ConfigurationWrapper> getConfigurationList() {
		return configurationList;
	}

	public final ConfigurationWrapper getConfigurationFor(final String entry) throws ConfigurationException {
		final ConfigurationWrapper configuration = configurationList.get(entry);
		if (configuration == null) {
			throw new ConfigurationException("Configuration for " + entry + " could not be found.");
		}
		return configuration;
	}
}
