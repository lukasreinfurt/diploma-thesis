package org.simtech.bootware.plugins.infrastructure.i1;

import org.simtech.bootware.core.plugins.AbstractActivator;

public class Activator extends AbstractActivator {

	public Activator() {}

	protected final I1 getPluginInstance() { return new I1(); };

}

