package org.simtech.bootware.plugins.event.consoleLogger;

import org.simtech.bootware.core.plugins.AbstractActivator;

public class Activator extends AbstractActivator {
	protected final ConsoleLogger getPluginInstance() { return new ConsoleLogger(); };
}

