package org.simtech.bootware.core.plugins;

import org.simtech.bootware.core.EventBus;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

/**
 * Abstract base plugin that should be used as baseline for all Bootware plugins.
 */
public class BasePlugin implements Plugin {
	protected BundleContext context;
	protected EventBus eventBus;

	public BasePlugin() {
		context  = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
		eventBus = getSharedObject(EventBus.class);
		eventBus.subscribe(this);
	}

	protected <T> T getSharedObject(Class<T> type) {
		ServiceReference serviceReference = context.getServiceReference(type.getName());
		return type.cast(context.getService(serviceReference));
	}

	public void stop() {
		eventBus.unsubscribe(this);
	}

}
