package org.simtech.bootware.plugins.application.opentosca;

import org.simtech.bootware.core.plugins.AbstractActivator;

public class Activator extends AbstractActivator {

	public Activator() {}

	protected final OpenTosca getPluginInstance() { return new OpenTosca(); };

}

