package org.simtech.bootware.core.plugins;

import java.util.Map;

import org.simtech.bootware.core.ConfigurationWrapper;
import org.simtech.bootware.core.exceptions.InitializeException;

/**
 * Generic plugin interface.
 * <p>
 * This interface is used by {@link AbstractBasePlugin} and should not be used otherwise.
 * If possible, create new plugins by extending from {@link AbstractBasePlugin}.
 */
public interface Plugin {

	void initialize(Map<String, ConfigurationWrapper> configurationList) throws InitializeException;

	/**
	 * Is executed when the OSGi bundle is stopped.
	 */
	void stop();
}
