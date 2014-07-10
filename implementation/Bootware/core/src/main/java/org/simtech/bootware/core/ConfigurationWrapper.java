package org.simtech.bootware.core;

import java.util.HashMap;
import java.util.Map;

import org.simtech.bootware.core.exceptions.ConfigurationException;

public class ConfigurationWrapper {

	private Map<String, String> configuration = new HashMap<String, String>();

	public ConfigurationWrapper() {}

	public final void setConfiguration(final Map<String, String> map) {
		configuration = map;
	}

	public final Map<String, String> getConfiguration() {
		return configuration;
	}

	public final String get(final String entry) throws ConfigurationException {
		final String value = configuration.get(entry);
		if (value == null) {
			throw new ConfigurationException("Entry " + entry + " could not be found in configuration.");
		}
		return value;
	}
}
