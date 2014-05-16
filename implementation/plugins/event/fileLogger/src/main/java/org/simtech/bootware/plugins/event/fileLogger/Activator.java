package org.simtech.bootware.plugins.event.fileLogger;

import org.simtech.bootware.core.plugins.AbstractActivator;

public class Activator extends AbstractActivator
{
	protected FileLogger getPluginInstance() { return new FileLogger(); };
}

