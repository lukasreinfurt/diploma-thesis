package org.simtech.bootware.plugins.communication.test;

import org.simtech.bootware.core.plugins.AbstractActivator;

public class Activator extends AbstractActivator {

	public Activator() {}

	protected final Test getPluginInstance() { return new Test(); };

}

