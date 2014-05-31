package org.simtech.bootware.core.plugins;

/**
 * Generic plugin interface.
 * <p>
 * This interface is used by {@link AbstractBasePlugin} and should not be used otherwise.
 * If possible, create new plugins by extending from {@link AbstractBasePlugin}.
 */
public interface Plugin {

	/**
	 * Is executed when the OSGi bundle is stopped.
	 */
	void stop();
}
