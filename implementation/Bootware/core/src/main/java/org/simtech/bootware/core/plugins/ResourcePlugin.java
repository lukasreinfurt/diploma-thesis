package org.simtech.bootware.core.plugins;

import java.util.Map;

import org.simtech.bootware.core.exceptions.DeprovisionResourceException;
import org.simtech.bootware.core.exceptions.ProvisionResourceException;

public interface ResourcePlugin extends Plugin {
	Map<String, String> provision() throws ProvisionResourceException;
	void deprovision(Map<String, String> instanceInformation) throws DeprovisionResourceException;
}
