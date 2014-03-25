package org.simtech.bootware.core.plugins;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import org.simtech.bootware.core.plugins.Plugin;

public abstract class AbstractActivator implements BundleActivator
{

	private Plugin plugin;

	protected abstract Plugin getPluginInstance();

	public void start(BundleContext context) {
		plugin = getPluginInstance();
		context.registerService(Plugin.class.getName(), plugin, null);
	}

	public void stop(BundleContext context) {
		// service is automatically unregistered
		plugin.stop();
	}

}

