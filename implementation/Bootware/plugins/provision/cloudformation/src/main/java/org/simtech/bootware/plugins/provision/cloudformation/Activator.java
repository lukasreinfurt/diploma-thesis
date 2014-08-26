package org.simtech.bootware.plugins.provision.cloudformation;

import org.simtech.bootware.core.plugins.AbstractActivator;

public class Activator extends AbstractActivator {

	public Activator() {}

	protected final CloudFormation getPluginInstance() { return new CloudFormation(); };

}

