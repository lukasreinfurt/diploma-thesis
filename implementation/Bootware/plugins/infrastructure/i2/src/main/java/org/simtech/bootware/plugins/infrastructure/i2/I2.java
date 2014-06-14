package org.simtech.bootware.plugins.infrastructure.i2;

import org.simtech.bootware.core.CredentialsWrapper;
import org.simtech.bootware.core.Instance;
import org.simtech.bootware.core.exceptions.CredentialsException;
import org.simtech.bootware.core.plugins.AbstractBasePlugin;
import org.simtech.bootware.core.plugins.InfrastructurePlugin;

public class I2 extends AbstractBasePlugin implements InfrastructurePlugin {

	public I2() {}

	public final void initialize() {
		// no op
	}

	public final void shutdown() {
		// no op
	}

	public final Instance provision(final CredentialsWrapper credentials) {
		try {
			System.out.println("I2: provision");
			System.out.println("789: " + credentials.get("789"));
		}
		catch (CredentialsException e) {
			System.out.println(e.toString());
		}
		return new Instance();
	}

	public final void deprovision(final Instance instance) {
		System.out.println("I2: deprovision");
	}

}
