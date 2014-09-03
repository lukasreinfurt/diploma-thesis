package org.simtech.bootware.core.plugins;

import java.io.File;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Abstract activator class that should be used as baseline for all Bootware plugin activators.
 */
public abstract class AbstractActivator implements BundleActivator {

	private Plugin plugin;

	/**
	 * This method has to be implemented by plugin activators so that this activator
	 * can get an instance of the plugin.
	 */
	protected abstract Plugin getPluginInstance();

	/**
	 * Starts a plugin.
	 *
	 * @param context The plugin bundle context.
	 */
	public final void start(final BundleContext context) {
		// Get an instance of the plugin.
		plugin = getPluginInstance();

		// Create a property object that contains the name of the plugin file under
		// the key "name", so that the plugin manager can later filter the plugins
		// by their file name. Otherwise we can not distinguish between multiple
		// plugins of the same type (e.g. event plugins).
		final Dictionary<String, String> properties = new Hashtable<String, String>();
		try {
			final CodeSource codeSource = plugin.getClass()
			                                    .getProtectionDomain()
			                                    .getCodeSource();
			final String jarPath = codeSource.getLocation()
			                                 .toURI()
			                                 .toString()
			                                 .replaceAll("^file:[/|\\\\]*", "");
			final String jarName = new File(jarPath).getName();
			properties.put("name", jarName);
		}
		catch (URISyntaxException e) {
			// Should never happen.
			e.printStackTrace();
		}

		// We register the service under the name of the first interface that it implements.
		context.registerService(plugin.getClass().getInterfaces()[0].getName(), plugin, properties);
	}

	/**
	 * Stops a plugin.
	 *
	 * @param context The plugin bundle context.
	 */
	public final void stop(final BundleContext context) {
		// service is automatically unregistered
		plugin.stop();
	}

}

