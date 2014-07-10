package org.simtech.bootware.core.plugins;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import org.simtech.bootware.core.ConfigurationWrapper;
import org.simtech.bootware.core.EventBus;

/**
 * Abstract base plugin that should be used as baseline for all Bootware plugins.
 */
public abstract class AbstractBasePlugin implements Plugin {
	protected ConfigurationWrapper config;
	protected BundleContext context;
	protected EventBus eventBus;

	/**
	 * Creates an abstract base plugin.
	 * <p>
	 * Subscribes the plugin to the event bus (even if the plugin doesn't actually handle any events).
	 * Also executes {@link #initialize}.
	 */
	public AbstractBasePlugin() {
		context  = FrameworkUtil.getBundle(this.getClass()).getBundleContext();

		eventBus = getSharedObject(EventBus.class);
		eventBus.subscribe(this);

		config = getSharedObject(ConfigurationWrapper.class);
		initialize(config);
	}

	/**
	 * Is executed when the OSGi bundle is started.
	 */
	public abstract void initialize(ConfigurationWrapper configuration);

	/**
	 * Is executed when the OSGi bundle is stopped.
	 */
	public abstract void shutdown();

	/**
	 * Gets a object that has been shared with the plugins via the OSGi service registry.
	 *
	 * @param type The class of the object that should be retrieved.
	 * @return The object.
	 */
	protected final <T> T getSharedObject(final Class<T> type) {
		final ServiceReference<?> serviceReference = context.getServiceReference(type.getName());
		return type.cast(context.getService(serviceReference));
	}

	/**
	 * Is executed when the OSGi bundle is stopped.
	 * <p>
	 * Calls {@link #shutdown}.
	 * Also unsubscribes the plugin from the event bus.
	 */
	public final void stop() {
		shutdown();
		eventBus.unsubscribe(this);
	}

}
