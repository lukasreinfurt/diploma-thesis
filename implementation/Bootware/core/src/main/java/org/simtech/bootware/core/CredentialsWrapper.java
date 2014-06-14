package org.simtech.bootware.core;

import java.util.HashMap;
import java.util.Map;

import org.simtech.bootware.core.exceptions.CredentialsException;

public class CredentialsWrapper {

	private Map<String, String> credentials = new HashMap<String, String>();

	public CredentialsWrapper() {}

	public final void setCredentials(final Map<String, String> map) {
		credentials = map;
	}

	public final Map<String, String> getCredentials() {
		return credentials;
	}

	public final String get(final String entry) throws CredentialsException {
		final String credential = credentials.get(entry);
		if (credential == null) {
			throw new CredentialsException("Entry " + entry + " could not be found in credentials.");
		}
		return credential;
	}
}
