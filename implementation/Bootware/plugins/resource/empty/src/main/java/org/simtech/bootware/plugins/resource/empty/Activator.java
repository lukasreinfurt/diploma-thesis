package org.simtech.bootware.plugins.resource.empty;

import org.simtech.bootware.core.plugins.AbstractActivator;

public class Activator extends AbstractActivator {

	public Activator() {}

	protected final Empty getPluginInstance() { return new Empty(); };

}

