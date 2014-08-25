package org.simtech.bootware.core;

import java.util.HashMap;
import java.util.Map;

/**
 * A wrapper class that wraps a configuration list map.
 * This simplifies jaxb marshalling and unmarshalling.
 */
public class ConfigurationListWrapper {

	private Map<String, ConfigurationWrapper> configurationList = new HashMap<String, ConfigurationWrapper>();

	public ConfigurationListWrapper() {}

	public final void setConfigurationList(final Map<String, ConfigurationWrapper> map) {
		configurationList = map;
	}

	public final Map<String, ConfigurationWrapper> getConfigurationList() {
		return configurationList;
	}

}
