package org.simtech.bootware.plugins.provision.empty;

import org.simtech.bootware.core.plugins.AbstractActivator;

public class Activator extends AbstractActivator {

	public Activator() {}

	protected final Empty getPluginInstance() { return new Empty(); };

}

