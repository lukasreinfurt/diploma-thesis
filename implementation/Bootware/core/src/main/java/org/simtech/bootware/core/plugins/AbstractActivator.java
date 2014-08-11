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

	protected abstract Plugin getPluginInstance();

	public final void start(final BundleContext context) {
		plugin = getPluginInstance();
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
			e.printStackTrace();
		}
		// We register the service under the name of the first interface that it implements.
		context.registerService(plugin.getClass().getInterfaces()[0].getName(), plugin, properties);
	}

	public final void stop(final BundleContext context) {
		// service is automatically unregistered
		plugin.stop();
	}

}

