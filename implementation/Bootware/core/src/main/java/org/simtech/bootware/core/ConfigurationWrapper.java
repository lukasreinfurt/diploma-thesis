package org.simtech.bootware.core;

import java.util.HashMap;
import java.util.Map;

public class ConfigurationWrapper {

	private Map<String, String> configuration = new HashMap<String, String>();

	public ConfigurationWrapper() {}

	public final void setConfiguration(final Map<String, String> map) {
		configuration = map;
	}

	public final Map<String, String> getConfiguration() {
		return configuration;
	}

}
