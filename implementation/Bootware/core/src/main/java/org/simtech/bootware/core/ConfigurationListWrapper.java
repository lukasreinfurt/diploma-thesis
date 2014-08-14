package org.simtech.bootware.core;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
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
