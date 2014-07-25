package org.simtech.bootware.plugins.event.zeromqsubscriber;

import org.simtech.bootware.core.plugins.AbstractActivator;

public class Activator extends AbstractActivator {

	public Activator() {}

	protected final ZeroMQSubscriber getPluginInstance() { return new ZeroMQSubscriber(); };

}

