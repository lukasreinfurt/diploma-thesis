package org.simtech.bootware.plugins.application.opentoscaec2;

import org.simtech.bootware.core.plugins.AbstractActivator;

public class Activator extends AbstractActivator {

	public Activator() {}

	protected final OpenToscaEC2 getPluginInstance() { return new OpenToscaEC2(); };

}

