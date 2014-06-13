package org.simtech.bootware.core;

import java.util.HashMap;
import java.util.Map;

import org.simtech.bootware.core.exceptions.CredentialsException;

public class Credentials {

	private Map<String, String> credentialsList = new HashMap<String, String>();

	public Credentials() {}

	public final void setCredentialsList(final Map<String, String> map) {
		credentialsList = map;
	}

	public final Map<String, String> getCredentialsList() {
		return credentialsList;
	}

	public final String get(final String entry) throws CredentialsException {
		final String credential = credentialsList.get(entry);
		if (credential == null) {
			throw new CredentialsException("Entry " + entry + " could not be found in credentials.");
		}
		return credential;
	}
}
