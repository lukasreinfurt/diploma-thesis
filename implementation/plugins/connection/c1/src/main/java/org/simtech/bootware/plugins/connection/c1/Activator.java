package org.simtech.bootware.plugins.connection.c1;

import org.simtech.bootware.core.plugins.AbstractActivator;

public class Activator extends AbstractActivator {

	public Activator() {

	}

	protected final C1 getPluginInstance() { return new C1(); };
}

