package org.simtech.bootware.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

import org.simtech.bootware.core.events.ErrorEvent;
import org.simtech.bootware.core.events.SuccessEvent;
import org.simtech.bootware.core.exceptions.LoadPluginException;
import org.simtech.bootware.core.exceptions.UnloadPluginException;

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
		                       "org.simtech.bootware.core.exceptions;version=1.0.0," +
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
	 * @see org.simtech.bootware.core.plugins.AbstractBasePlugin#getSharedObject
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
	public <T> T loadPlugin(Class<T> type, String path) throws LoadPluginException {
		try {
			installedBundles.put(path, context.installBundle("file:" + path));
			installedBundles.get(path).start();
			SuccessEvent event = new SuccessEvent();
			event.setMessage("Successfully loaded plugin: '" + path + "'.");
			eventBus.publish(event);
		} catch (BundleException e) {
			ErrorEvent event = new ErrorEvent();
			event.setMessage("Failed to load plugin: '" + path + "'.");
			eventBus.publish(event);
			throw new LoadPluginException(e);
		}
		BundleContext bundleContext = installedBundles.get(path).getBundleContext();
		ServiceReference<?> serviceReference = bundleContext.getServiceReference(type.getName());
		return type.cast(bundleContext.getService(serviceReference));
	}

	/**
	 * Unload a loaded plugin.
	 *
	 * @param path Path to the .jar file that implements the plugin.
	 */
	public void unloadPlugin(String path) throws UnloadPluginException {
		Bundle bundle = installedBundles.get(path);
		if (bundle != null) {
			try {
				bundle.uninstall();
				installedBundles.remove(path);
				SuccessEvent event = new SuccessEvent();
				event.setMessage("Successfully unloaded plugin: '" + path + "'.");
				eventBus.publish(event);
			} catch (BundleException e) {
				ErrorEvent event = new ErrorEvent();
				event.setMessage("Failed to unload plugin: '" + path + "'.");
				eventBus.publish(event);
				throw new UnloadPluginException(e);
			}
		}
	}

	/**
	 * Unloads all loaded plugins.
	 */
	public void unloadAllPlugins() throws UnloadPluginException {
		Iterator<Map.Entry<String, Bundle>> iterator = installedBundles.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, Bundle> entry = iterator.next();
			String key = entry.getKey().toString();
			try {
				installedBundles.get(key).uninstall();
				iterator.remove();
				SuccessEvent event = new SuccessEvent();
				event.setMessage("Successfully unloaded plugin: '" + key + "'.");
				eventBus.publish(event);
			} catch (BundleException e) {
				ErrorEvent event = new ErrorEvent();
				event.setMessage("Failed to unload plugin: '" + key + "'.");
				eventBus.publish(event);
				throw new UnloadPluginException(e);
			}
		}
	}

	/**
	 * Stop the OSGi framework.
	 * If there are still plugins loaded, they will be unloaded before the framework is stopped.
	 */
	public void stop() throws UnloadPluginException {
		unloadAllPlugins();
		try {
			framework.stop();
		} catch (BundleException e) {
			e.printStackTrace();
		}
	}
}
