package org.simtech.bootware.plugins.infrastructure.i2;

import org.simtech.bootware.core.plugins.AbstractActivator;

public class Activator extends AbstractActivator {
	protected I2 getPluginInstance() { return new I2(); };
}

