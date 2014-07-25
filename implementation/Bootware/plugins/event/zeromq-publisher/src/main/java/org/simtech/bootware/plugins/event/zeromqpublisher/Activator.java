package org.simtech.bootware.plugins.event.zeromqpublisher;

import org.simtech.bootware.core.plugins.AbstractActivator;

public class Activator extends AbstractActivator {

	public Activator() {}

	protected final ZeroMQPublisher getPluginInstance() { return new ZeroMQPublisher(); };

}

