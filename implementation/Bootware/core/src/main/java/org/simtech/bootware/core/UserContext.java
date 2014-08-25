package org.simtech.bootware.core;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Describes an application and a resource that should be deployed or undeployed.
 * <p>
 * The user context is supplied as input to the deploy and undeploy operations.
 * It specifies an application that should be deployed or undeployed and also a
 * resource on which this application should live.
 * These two values are used to map an user context to a request context, that
 * contains more details necessary to fulfill such a request.
 * <p>
 * The user context can also contain a service package reference that will be
 * used by a provision plugin to provision the give service.
 * The user context can also contain additional configuration values for various
 * plugins.
 */
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
