package org.simtech.bootware.core.plugins;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import org.simtech.bootware.core.EventBus;

/**
 * Abstract base plugin that should be used as baseline for all Bootware plugins.
 */
public abstract class AbstractBasePlugin implements Plugin {
	protected BundleContext context;
	protected EventBus eventBus;

	/**
	 * Is executed when the OSGi bundle is started.
	 */
	public abstract void initialize();

	/**
	 * Is executed when the OSGi bundle is stopped.
	 */
	public abstract void shutdown();

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
		initialize();
	}

	/**
	 * Gets a object that has been shared with the plugins via the OSGi service registry.
	 *
	 * @param type The class of the object that should be retrieved.
	 * @return The object.
	 */
	protected final <T> T getSharedObject(Class<T> type) {
		ServiceReference<?> serviceReference = context.getServiceReference(type.getName());
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
