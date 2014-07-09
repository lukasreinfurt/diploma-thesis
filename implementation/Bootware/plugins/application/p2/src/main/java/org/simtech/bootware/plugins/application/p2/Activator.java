package org.simtech.bootware.plugins.application.p2;

import org.simtech.bootware.core.plugins.AbstractActivator;

public class Activator extends AbstractActivator {

	public Activator() {}

	protected final P2 getPluginInstance() { return new P2(); };

}

