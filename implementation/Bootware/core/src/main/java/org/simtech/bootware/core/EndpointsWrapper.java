package org.simtech.bootware.core;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class EndpointsWrapper {

	private Map<String, URL> endpoints = new HashMap<String, URL>();

	public EndpointsWrapper() {}

	public final void setEndpoints(final Map<String, URL> map) {
		endpoints = map;
	}

	public final Map<String, URL> getEndpoints() {
		return endpoints;
	}

}
