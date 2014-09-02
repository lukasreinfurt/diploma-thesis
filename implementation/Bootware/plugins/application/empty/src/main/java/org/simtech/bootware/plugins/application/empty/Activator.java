package org.simtech.bootware.plugins.application.empty;

import org.simtech.bootware.core.plugins.AbstractActivator;

public class Activator extends AbstractActivator {

	public Activator() {}

	protected final Empty getPluginInstance() { return new Empty(); };

}

