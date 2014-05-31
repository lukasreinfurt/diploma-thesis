package org.simtech.bootware.plugins.event.fileLogger;

import org.simtech.bootware.core.plugins.AbstractActivator;

public class Activator extends AbstractActivator {

	public Activator() {}

	protected final FileLogger getPluginInstance() { return new FileLogger(); };

}

