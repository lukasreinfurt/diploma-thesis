package org.simtech.bootware.core;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(
		propOrder = {
		"resource",
		"application",
		"servicePackageReference",
		"configurationList"
		})
public class UserContext {

	private String resource;
	private String servicePackageReference;
	private String application;
	private Map<String, ConfigurationWrapper> configurationList = new HashMap<String, ConfigurationWrapper>();

	public UserContext() {}

	public final void setResource(final String resource) {
		this.resource = resource;
	}

	@XmlElement(required = true)
	public final String getResource() {
		return resource;
	}

	public final void setServicePackageReference(final String reference) {
		servicePackageReference = reference;
	}

	public final String getServicePackageReference() {
		return servicePackageReference;
	}

	public final void setApplication(final String application) {
		this.application = application;
	}

	@XmlElement(required = true)
	public final String getApplication() {
		return application;
	}

	public final void setConfigurationList(final Map<String, ConfigurationWrapper> map) {
		configurationList = map;
	}

	public final Map<String, ConfigurationWrapper> getConfigurationList() {
		return configurationList;
	}

}
