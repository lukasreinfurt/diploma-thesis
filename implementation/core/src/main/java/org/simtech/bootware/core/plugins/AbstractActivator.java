package org.simtech.bootware.core.plugins;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import org.simtech.bootware.core.plugins.Plugin;

/**
 * Abstract activator class that should be used as baseline for all Bootware plugin activators.
 */

public abstract class AbstractActivator implements BundleActivator {

	private Plugin plugin;

	protected abstract Plugin getPluginInstance();

	public void start(BundleContext context) {
		plugin = getPluginInstance();
		// TODO: This should be done differently.
		//
		// Problem:
		// The class used here to register the service has to be used later
		// to retrieve the service again. We use the parent class of the plugin here
		// because that's all we know at runtime (we don't know the actual plugin
		// class), but what if the hierarchy gets deeper?
		//
		// Solutions:
		// - Read class name from abstract method.
		// - Use properties and filters
		// - figure out a way to pass in a class into this method
		context.registerService(plugin.getClass().getSuperclass().getName(), plugin, null);
	}

	public void stop(BundleContext context) {
		// service is automatically unregistered
		plugin.stop();
	}

}

