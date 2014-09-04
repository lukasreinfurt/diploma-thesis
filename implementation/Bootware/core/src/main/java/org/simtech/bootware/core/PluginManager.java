package org.simtech.bootware.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

import org.simtech.bootware.core.events.CoreEvent;
import org.simtech.bootware.core.events.Severity;
import org.simtech.bootware.core.exceptions.InitializePluginManagerException;
import org.simtech.bootware.core.exceptions.LoadPluginException;
import org.simtech.bootware.core.exceptions.UnloadPluginException;

/**
 * A wrapper layer around the OSGi framework.
 * <p>
 * The plugin manager handles the loading and unloading of plugins.
 */
@SuppressWarnings("checkstyle:classfanoutcomplexity")
public class PluginManager {

	private EventBus eventBus;
	private String repositoryURL;
	private Map<String, String> config;
	private Map<String, Bundle> installedBundles;
	private FrameworkFactory frameworkFactory;
	private Framework framework;
	private BundleContext context;

	/**
	 * Creates a plugin manager with its own OSGi framework instance and starts the framework.
	 * The framework should be stopped with {@link #stop} before the plugin manager is destroyed (e.g. when the application shuts down).
	 */
	public PluginManager(final EventBus eventBus, final String repositoryURL) throws InitializePluginManagerException {

		this.eventBus      = eventBus;
		this.repositoryURL = repositoryURL;
		config             = new HashMap<String, String>();
		installedBundles   = new HashMap<String, Bundle>();

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
			throw new InitializePluginManagerException(e);
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
	 * If the plugin doesn't exist already, it will be downloaded from the repository.
	 *
	 * @param type The type of the plugin that should be unloaded.
	 * @param name The name of the plugin that should be unloaded.
	 *
	 * @return The plugin object.
	 *
	 * @throws LoadPluginException If there was an error while loading the plugin.
	 */
	@SuppressWarnings({
		"checkstyle:cyclomaticcomplexity",
		"checkstyle:javancss"
	})
	public final <T> T loadPlugin(final Class<T> clazz, final String type, final String name) throws LoadPluginException {

		final String pluginID = type + "/" + name;
		final File pluginFile = new File("plugins/" + pluginID);

		eventBus.publish(new CoreEvent(Severity.INFO, "Loading plugin " + pluginID + "."));

		// Check if plugin is already available locally.
		// If not, try downloading it from the repository
		if (!pluginFile.exists()) {
			eventBus.publish(new CoreEvent(Severity.INFO, "Plugin " + pluginID + " doesn't exist yet locally."));
			eventBus.publish(new CoreEvent(Severity.INFO, "Downloading from repository at " + repositoryURL + "/getPlugin/" + type + "/" + name));

			// Create client.
			final Client client = ClientBuilder.newBuilder().register(Response.class).build();

			// Send GET request for the plugin to repository
			FileInputStream inputStream = null;
			FileOutputStream outputStream = null;
			try {
				final Response response = client.target(repositoryURL)
				                          .path("/getPlugin/{pluginType}/{pluginName}")
				                          .resolveTemplate("pluginType", type)
				                          .resolveTemplate("pluginName", name)
				                          .request()
				                          .get(Response.class);

				// Save plugin locally if response was okay.
				final Integer ok = 200;
				if (response.getStatus() == ok) {
					// Create local folders if they don't exist already.
					pluginFile.getParentFile().mkdirs();

					// Write response file to local file.
					final File responseFile = response.readEntity(File.class);
					inputStream = new FileInputStream(responseFile);
					outputStream = new FileOutputStream(pluginFile);
					final Integer bufferSize = 4096;
					final byte[] buffer = new byte[bufferSize];
					int len;
					while ((len = inputStream.read(buffer)) > 0) {
						outputStream.write(buffer, 0, len);
					}
				}
				else {
					throw new LoadPluginException("The plugin " + pluginID + " could not be found in the repository.");
				}
			}
			catch (WebApplicationException e) {
				throw new LoadPluginException(e);
			}
			catch (ProcessingException e) {
				throw new LoadPluginException(e);
			}
			catch (FileNotFoundException e) {
				throw new LoadPluginException(e);
			}
			catch (IOException e) {
				throw new LoadPluginException(e);
			}
			finally {
				try {
					if (outputStream != null) {
						outputStream.close();
					}
					if (inputStream != null) {
						inputStream.close();
					}
				}
				catch (IOException e) {
					throw new LoadPluginException(e);
				}
			}
		}
		else {
			eventBus.publish(new CoreEvent(Severity.INFO, "Plugin " + pluginID + " already exist locally."));
		}

		eventBus.publish(new CoreEvent(Severity.INFO, "Starting plugin " + pluginID + "."));

		// Load the plugin and store a reference in installedBundles. Then start it.
		try {
			installedBundles.put(pluginID, context.installBundle("file:" + pluginFile.toString().replace("\\", "/")));
			installedBundles.get(pluginID).start();
		}
		catch (BundleException e) {
			throw new LoadPluginException(e);
		}

		// Get the plugin object that will be returned.
		final BundleContext bundleContext = installedBundles.get(pluginID).getBundleContext();
		final String filter = "(name=" + name + ")";
		final ServiceReference[] serviceReferences;

		try {
			serviceReferences = bundleContext.getServiceReferences(clazz.getName(), filter);
		}
		catch (InvalidSyntaxException e) {
			throw new LoadPluginException("Invalid filter syntax.");
		}

		if (serviceReferences.length == 0) {
			throw new LoadPluginException("Could not retrieve service reference for plugin '" + pluginID + "'.");
		}

		return clazz.cast(bundleContext.getService(serviceReferences[0]));
	}

	/**
	 * Unload a loaded plugin.
	 *
	 * @param type The type of the plugin that should be unloaded.
	 * @param name The name of the plugin that should be unloaded.
	 *
	 * @throws UnloadPluginException If there was an error while unloading the plugin.
	 */
	public final void unloadPlugin(final String type, final String name) throws UnloadPluginException {

		final String pluginID = type + "/" + name;

		eventBus.publish(new CoreEvent(Severity.INFO, "Unloading plugin " + pluginID + "."));

		final Bundle bundle = installedBundles.get(pluginID);

		if (bundle != null) {
			try {
				bundle.uninstall();
				installedBundles.remove(pluginID);
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
				eventBus.publish(new CoreEvent(Severity.INFO, "Unloading plugin " + key + "."));
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
			System.out.println("Stopping OSGi Framework failed: " + e.getMessage());
		}
	}
}
