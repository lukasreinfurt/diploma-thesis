package org.simtech.bootware.plugins.provision.test;

import java.util.HashMap;
import java.util.Map;

import org.simtech.bootware.core.ConfigurationWrapper;
import org.simtech.bootware.core.plugins.AbstractBasePlugin;
import org.simtech.bootware.core.plugins.ProvisionPlugin;

public class Test extends AbstractBasePlugin implements ProvisionPlugin {

	public Test() {}

	public final void initialize(final Map<String, ConfigurationWrapper> configurationList) {
		// no op
	}

	public final void shutdown() {
		// no op
	}

	public final Map<String, String> provision(final String provisioningEgnineEndpoint, final String servicePackageReference) {
		System.out.println("Provision dfskgjhksdfhglkdsfgsdfg");
		return new HashMap<String, String>();
	}

	public final void deprovision(final String provisioningEgnineEndpoint, final String servicePackageReference) {
		System.out.println("Deprovision dfskgjhksdfhglkdsfgsdfg");
	}

}
