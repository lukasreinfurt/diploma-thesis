package org.simtech.bootware.core;

import java.util.HashMap;
import java.util.ServiceLoader;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

import net.engio.mbassy.bus.MBassador;

import org.simtech.bootware.core.plugins.Plugin;

public class PluginManager {

	private HashMap<String, String> config;
	private HashMap<String, Bundle> installedBundles;
	private FrameworkFactory frameworkFactory;
	private Framework framework;
	private BundleContext context;

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

	public void registerSharedObject(Object object) {
		context.registerService(object.getClass().getName(), object, null);
	}

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

	public void unloadPlugin(String path) {
		try {
			installedBundles.get(path).uninstall();
			installedBundles.remove(path);
		} catch (BundleException e) {
			e.printStackTrace();
		}
	}

	public void stop() {
		try {
			framework.stop();
		} catch (BundleException e) {
			e.printStackTrace();
		}
	}
}
