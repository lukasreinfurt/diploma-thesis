package org.simtech.bootware.core;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.simtech.bootware.core.exceptions.ConfigurationException;

@XmlRootElement
@XmlType(
		propOrder = {
		"resourcePlugin",
		"communicationPlugin",
		"applicationPlugin",
		"callApplicationPlugin",
		"servicePackageReference",
		"configurationList"
		})
public class Context {

	private String resourcePlugin;
	private String callApplicationPlugin;
	private String communicationPlugin;
	private String servicePackageReference;
	private String applicationPlugin;
	private Map<String, ConfigurationWrapper> configurationList = new HashMap<String, ConfigurationWrapper>();

	public Context() {}

	public final void setResourcePlugin(final String plugin) {
		resourcePlugin = plugin;
	}

	@XmlElement(required = true)
	public final String getResourcePlugin() {
		return resourcePlugin;
	}

	public final void setCallApplicationPlugin(final String plugin) {
		callApplicationPlugin = plugin;
	}

	public final String getCallApplicationPlugin() {
		return callApplicationPlugin;
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

	public final void setApplicationPlugin(final String plugin) {
		applicationPlugin = plugin;
	}

	@XmlElement(required = true)
	public final String getApplicationPlugin() {
		return applicationPlugin;
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
