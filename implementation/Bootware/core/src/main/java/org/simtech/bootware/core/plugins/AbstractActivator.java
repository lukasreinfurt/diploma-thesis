package org.simtech.bootware.core.plugins;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Abstract activator class that should be used as baseline for all Bootware plugin activators.
 */
public abstract class AbstractActivator implements BundleActivator {

	private Plugin plugin;

	protected abstract Plugin getPluginInstance();

	public final void start(final BundleContext context) {
		plugin = getPluginInstance();
		// We register the service under the name of the first interface that it implements.
		context.registerService(plugin.getClass().getInterfaces()[0].getName(), plugin, null);
	}

	public final void stop(final BundleContext context) {
		// service is automatically unregistered
		plugin.stop();
	}

}

