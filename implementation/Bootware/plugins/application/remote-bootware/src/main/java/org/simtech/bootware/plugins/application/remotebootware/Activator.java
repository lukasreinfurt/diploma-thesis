package org.simtech.bootware.plugins.application.remotebootware;

import org.simtech.bootware.core.plugins.AbstractActivator;

public class Activator extends AbstractActivator {

	public Activator() {}

	protected final RemoteBootware getPluginInstance() { return new RemoteBootware(); };

}

