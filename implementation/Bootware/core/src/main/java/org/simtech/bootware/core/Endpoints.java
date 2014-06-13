package org.simtech.bootware.core;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class Endpoints {

	private Map<String, URL> endpointsList = new HashMap<String, URL>();

	public Endpoints() {}

	public final void setEndpointsList(final Map<String, URL> map) {
		endpointsList = map;
	}

	public final Map<String, URL> getEndpointsList() {
		return endpointsList;
	}

	public final URL add(final String entry, final URL endpoint) {
		return endpointsList.put(entry, endpoint);
	}

	public final URL get(final String entry) {
		return endpointsList.get(entry);
	}
}
