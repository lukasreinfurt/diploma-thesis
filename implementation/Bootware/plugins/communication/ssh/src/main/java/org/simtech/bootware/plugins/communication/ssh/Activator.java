package org.simtech.bootware.plugins.communication.ssh;

import org.simtech.bootware.core.plugins.AbstractActivator;

public class Activator extends AbstractActivator {

	public Activator() {}

	protected final SSH getPluginInstance() { return new SSH(); };

}

