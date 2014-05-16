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
import org.simtech.bootware.core.events.SuccessEvent;
import org.simtech.bootware.core.events.ErrorEvent;

/**
 * A wrapper layer around the OSGi framwork.
 */

public class PluginManager {

	private EventBus eventBus;
	private HashMap<String, String> config;
	private HashMap<String, Bundle> installedBundles;
	private FrameworkFactory frameworkFactory;
	private Framework framework;
	private BundleContext context;

	/**
	 * Creates a plugin manager with its own OSGi framework instance and starts the framework.
	 * The framework should be stopped with {@link #stop} before the plugin manager is destroyed (e.g. when the application shuts down).
	 */
	public PluginManager(EventBus eventBus) {

		this.eventBus    = eventBus;
		config           = new HashMap<String, String>();
		installedBundles = new HashMap<String, Bundle>();

		// export packages via system bundle to resolve constrains of plugin bundles.
		// Specific version is needed or unresolved constraint occur.
		String extraPackages = "org.simtech.bootware.core;version=1.0.0," +
		                       "org.simtech.bootware.core.events;version=1.0.0," +
		                       "org.simtech.bootware.core.filters;version=1.0.0," +
		                       "org.simtech.bootware.core.plugins;version=1.0.0," +
		                       "net.engio.mbassy.listener;version=1.1.2," +
		                       "net.engio.mbassy.common;version=1.1.2";
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
			SuccessEvent event = new SuccessEvent();
			event.setMessage("Successfully loaded plugin: " + path + "'.");
			eventBus.publish(event);
		} catch (BundleException e) {
			ErrorEvent event = new ErrorEvent();
			event.setMessage("Failed to load plugin: " + path + "'.");
			eventBus.publish(event);
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
			SuccessEvent event = new SuccessEvent();
			event.setMessage("Successfully unloaded plugin: " + path + "'.");
			eventBus.publish(event);
		} catch (BundleException e) {
			ErrorEvent event = new ErrorEvent();
			event.setMessage("Failed to unload plugin: " + path + "'.");
			eventBus.publish(event);
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
			unloadPlugin(entry.getKey().toString());
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
