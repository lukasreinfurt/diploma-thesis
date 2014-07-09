package org.simtech.bootware.plugins.communication.c2;

import org.simtech.bootware.core.plugins.AbstractActivator;

public class Activator extends AbstractActivator {

	public Activator() {}

	protected final C2 getPluginInstance() { return new C2(); };

}

