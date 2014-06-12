package org.simtech.bootware.plugins.infrastructure.i1;

import org.simtech.bootware.core.Credentials;
import org.simtech.bootware.core.Instance;
import org.simtech.bootware.core.exceptions.CredentialsException;
import org.simtech.bootware.core.plugins.AbstractBasePlugin;
import org.simtech.bootware.core.plugins.InfrastructurePlugin;

public class I1 extends AbstractBasePlugin implements InfrastructurePlugin {

	public I1() {}

	public final void initialize() {
		// no op
	}

	public final void shutdown() {
		// no op
	}

	public final Instance provision(final Credentials credentials) {
		System.out.println("I1: provision");
		try {
			System.out.println("123: " + credentials.get("123"));
			System.out.println("456: " + credentials.get("456"));
		}
		catch (CredentialsException e) {
			System.out.println(e.toString());
		}
		return new Instance();
	}

	public final void deprovision(final Instance instance) {
		System.out.println("I1: deprovision");
	}

}
