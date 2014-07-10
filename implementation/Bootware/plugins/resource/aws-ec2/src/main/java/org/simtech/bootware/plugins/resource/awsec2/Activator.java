package org.simtech.bootware.plugins.resource.awsec2;

import org.simtech.bootware.core.plugins.AbstractActivator;

public class Activator extends AbstractActivator {

	public Activator() {}

	protected final AwsEc2 getPluginInstance() { return new AwsEc2(); };

}

