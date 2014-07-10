package org.simtech.bootware.plugins.resource.i1;

import java.util.HashMap;
import java.util.Map;

import org.simtech.bootware.core.ConfigurationWrapper;
import org.simtech.bootware.core.exceptions.ConfigurationException;
import org.simtech.bootware.core.plugins.AbstractBasePlugin;
import org.simtech.bootware.core.plugins.ResourcePlugin;

public class I1 extends AbstractBasePlugin implements ResourcePlugin {

	public I1() {}

	public final void initialize(final ConfigurationWrapper configuration) {
		System.out.println("I1: provision");
		try {
			System.out.println("123: " + configuration.get("123"));
			System.out.println("456: " + configuration.get("456"));
		}
		catch (ConfigurationException e) {
			System.out.println(e.toString());
		}
	}

	public final void shutdown() {
		// no op
	}

	public final Map<String, String> provision() {
		return new HashMap<String, String>();
	}

	public final void deprovision(final Map<String, String> instanceInformation) {
		System.out.println("I1: deprovision");
	}

}
