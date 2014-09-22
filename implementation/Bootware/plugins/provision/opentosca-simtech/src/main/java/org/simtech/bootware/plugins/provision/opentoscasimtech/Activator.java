package org.simtech.bootware.plugins.provision.opentoscasimtech;

import org.simtech.bootware.core.plugins.AbstractActivator;

public class Activator extends AbstractActivator {

	public Activator() {}

	protected final OpenToscaSimTech getPluginInstance() { return new OpenToscaSimTech(); };

}

