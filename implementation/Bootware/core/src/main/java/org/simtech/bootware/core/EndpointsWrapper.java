package org.simtech.bootware.core;

import java.util.HashMap;
import java.util.Map;

public class EndpointsWrapper {

	private Map<String, String> endpoints = new HashMap<String, String>();

	public EndpointsWrapper() {}

	public final void setEndpoints(final Map<String, String> map) {
		endpoints = map;
	}

	public final Map<String, String> getEndpoints() {
		return endpoints;
	}

}
