package org.simtech.bootware.core;

import java.util.HashMap;
import java.util.Map;

public class Credentials {

	private Map<String, String> credentials = new HashMap<String, String>();

	public Credentials() {}

	public final void setCredentials(final Map<String, String> map) {
		credentials = map;
	}

	public final Map<String, String> getCredentials() {
		return credentials;
	}

	public final String get(final String entry) {
		return credentials.get(entry);
	}
}
