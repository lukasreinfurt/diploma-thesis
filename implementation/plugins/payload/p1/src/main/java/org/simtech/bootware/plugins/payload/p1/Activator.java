package org.simtech.bootware.plugins.payload.p1;

import org.simtech.bootware.core.plugins.AbstractActivator;

public class Activator extends AbstractActivator {

	public Activator() {

	}

	protected final P1 getPluginInstance() { return new P1(); };
}

