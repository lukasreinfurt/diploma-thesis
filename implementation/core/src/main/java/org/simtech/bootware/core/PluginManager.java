package org.simtech.bootware.core;

import java.util.Map;
import java.util.HashMap;
import java.util.ServiceLoader;
import java.util.Iterator;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

import net.engio.mbassy.bus.MBassador;

import org.simtech.bootware.core.plugins.Plugin;

/**
 * A wrapper layer around the OSGi framwork.
 */

public class PluginManager {

	private HashMap<String, String> config;
	private HashMap<String, Bundle> installedBundles;
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

		// export packages via system bundle to resolve constrains of plugin bundles
		String extraPackages = "org.simtech.bootware.core," +
		                       "org.simtech.bootware.core.events," +
		                       "org.simtech.bootware.core.filters," +
		                       "org.simtech.bootware.core.plugins," +
		                       "net.engio.mbassy.listener;version=1.1.2," + // specific version is needed or unresolved constraint
		                       "net.engio.mbassy.common;version=1.1.2"; // specific version is needed or unresolved constraint
		config.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, extraPackages);

		frameworkFactory = ServiceLoader.load(FrameworkFactory.class).iterator().next();
		framework        = frameworkFactory.newFramework(config);

		try {
			framework.start();
		} catch (BundleException e) {
			e.printStackTrace();
		}

		context = framework.getBundleContext();
	}

	/**
	 * Register an object in the OSGi service registry so that in can be accessed from plugins.
	 * @see org.simtech.bootware.core.plugins.BasePlugin#getSharedObject
	 *
	 * @param object Object to be registered in the registry.
	 */
	public void registerSharedObject(Object object) {
		context.registerService(object.getClass().getName(), object, null);
	}

	/**
	 * Load and start a plugin.
	 *
	 * @param path Path to the .jar file that implements the plugin.
	 */
	public void loadPlugin(String path) {
		try {
			installedBundles.put(path, context.installBundle("file:" + path));
			installedBundles.get(path).start();
		} catch (BundleException e) {
			e.printStackTrace();
		}
	}

	private Plugin getPlugin(String path) {
		BundleContext bundleContext = installedBundles.get(path).getBundleContext();
		ServiceReference<Plugin> serviceReference = bundleContext.getServiceReference(Plugin.class);
		Plugin plugin = bundleContext.getService(serviceReference);
		return plugin;
	}

	/**
	 * Unload a loaded plugin.
	 *
	 * @param path Path to the .jar file that implements the plugin.
	 */
	public void unloadPlugin(String path) {
		try {
			installedBundles.get(path).uninstall();
			installedBundles.remove(path);
		} catch (BundleException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Unloads all loaded plugins.
	 */
	public void unloadAllPlugins() {
		Iterator iterator = installedBundles.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			try {
				installedBundles.get(entry.getKey()).uninstall();
				iterator.remove();
			} catch (BundleException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Stop the OSGi framework.
	 * If there are still plugins loaded, they will be unloaded before the framework is stopped.
	 */
	public void stop() {
		unloadAllPlugins();
		try {
			framework.stop();
		} catch (BundleException e) {
			e.printStackTrace();
		}
	}
}
