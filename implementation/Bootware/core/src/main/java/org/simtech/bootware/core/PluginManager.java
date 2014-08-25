package org.simtech.bootware.core;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

import org.simtech.bootware.core.exceptions.LoadPluginException;
import org.simtech.bootware.core.exceptions.UnloadPluginException;

/**
 * A wrapper layer around the OSGi framework.
 * <p>
 * The plugin manager handles the loading and unloading of plugins.
 */
public class PluginManager {

	private Map<String, String> config;
	private Map<String, Bundle> installedBundles;
	private FrameworkFactory frameworkFactory;
	private Framework framework;
	private BundleContext context;

	/**
	 * Creates a plugin manager with its own OSGi framework instance and starts the framework.
	 * The framework should be stopped with {@link #stop} before the plugin manager is destroyed (e.g. when the application shuts down).
	 */
	public PluginManager() {

		config           = new HashMap<String, String>();
		installedBundles = new HashMap<String, Bundle>();

		// export packages via system bundle to resolve constrains of plugin bundles.
		// Specific version is needed or unresolved constraint occur.
		final String extraPackages = "org.simtech.bootware.core;version=1.0.0,"
		                           + "org.simtech.bootware.core.events;version=1.0.0,"
		                           + "org.simtech.bootware.core.exceptions;version=1.0.0,"
		                           + "org.simtech.bootware.core.filters;version=1.0.0,"
		                           + "org.simtech.bootware.core.plugins;version=1.0.0,"
		                           + "net.engio.mbassy.listener;version=1.1.2,"
		                           + "net.engio.mbassy.common;version=1.1.2";
		config.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, extraPackages);

		// flush plugin cache on each framework start (doesn't seem to work)
		config.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);

		frameworkFactory = ServiceLoader.load(FrameworkFactory.class).iterator().next();
		framework        = frameworkFactory.newFramework(config);

		// start the framework
		try {
			framework.start();
		}
		catch (BundleException e) {
			e.printStackTrace();
		}

		context = framework.getBundleContext();
	}

	/**
	 * Register an object in the OSGi service registry so that in can be accessed from plugins.
	 * @see org.simtech.bootware.core.plugins.AbstractBasePlugin#getSharedObject
	 *
	 * @param object Object to be registered in the registry.
	 */
	public final void registerSharedObject(final Object object) {
		context.registerService(object.getClass().getName(), object, null);
	}

	/**
	 * Load and start a plugin.
	 *
	 * @param path Path to the .jar file that implements the plugin.
	 *
	 * @return The plugin object.
	 *
	 * @throws LoadPluginException If there was an error while loading the plugin.
	 */
	public final <T> T loadPlugin(final Class<T> type, final String path) throws LoadPluginException {

		// Load the plugin and store a reference in installedBundles. Then start it.
		try {
			installedBundles.put(path, context.installBundle("file:" + path));
			installedBundles.get(path).start();
		}
		catch (BundleException e) {
			throw new LoadPluginException(e);
		}

		// Get the plugin object that will be returned.
		final BundleContext bundleContext = installedBundles.get(path).getBundleContext();
		final String pluginName = new File(path).getName();
		final String filter = "(name=" + pluginName + ")";
		final ServiceReference[] serviceReferences;

		try {
			serviceReferences = bundleContext.getServiceReferences(type.getName(), filter);
		}
		catch (InvalidSyntaxException e) {
			throw new LoadPluginException("Invalid filter syntax.");
		}

		if (serviceReferences.length == 0) {
			throw new LoadPluginException("Could not retrieve service reference for plugin '" + pluginName + "'.");
		}

		return type.cast(bundleContext.getService(serviceReferences[0]));
	}

	/**
	 * Unload a loaded plugin.
	 *
	 * @param path Path to the .jar file that implements the plugin.
	 *
	 * @throws UnloadPluginException If there was an error while unloading the plugin.
	 */
	public final void unloadPlugin(final String path) throws UnloadPluginException {
		final Bundle bundle = installedBundles.get(path);
		if (bundle != null) {
			try {
				bundle.uninstall();
				installedBundles.remove(path);
			}
			catch (BundleException e) {
				throw new UnloadPluginException(e);
			}
		}
	}

	/**
	 * Unloads all loaded plugins.
	 *
	 * @throws UnloadPluginException If there was an error while unloading the plugins.
	 */
	public final void unloadAllPlugins() throws UnloadPluginException {
		final Iterator<Map.Entry<String, Bundle>> iterator = installedBundles.entrySet().iterator();
		while (iterator.hasNext()) {
			final Map.Entry<String, Bundle> entry = iterator.next();
			final String key = entry.getKey().toString();
			try {
				installedBundles.get(key).uninstall();
				iterator.remove();
			}
			catch (BundleException e) {
				throw new UnloadPluginException(e);
			}
		}
	}

	/**
	 * Stop the OSGi framework.
	 * <p>
	 * If there are still plugins loaded, they will be unloaded before the framework is stopped.
	 *
	 * @throws UnloadPluginException If there was an error while unloading the plugins.
	 */
	public final void stop() throws UnloadPluginException {
		unloadAllPlugins();
		try {
			framework.stop();
		}
		catch (BundleException e) {
			e.printStackTrace();
		}
	}
}
