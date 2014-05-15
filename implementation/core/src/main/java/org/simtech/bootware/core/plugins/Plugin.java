package org.simtech.bootware.core.plugins;

/**
 * Generic plugin interface.
 * <p>
 * This interface is used by {@link BasePlugin} and should not be used otherwise.
 * If possible, create new plugins by extending from {@link BasePlugin}.
 */
public interface Plugin {

	/**
	 * Is executed when the OSGi bundle is stopped and can be used for clean up.
	 */
	public void stop();
}
