package org.simtech.bootware.plugins.plugin1;

import org.simtech.bootware.core.plugins.AbstractActivator;

public class Activator extends AbstractActivator
{
	protected Plugin1 getPluginInstance() { return new Plugin1(); };
}

