package org.simtech.bootware.core;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Stores @see org.simtech.bootware.core.ApplicationInstance
 */

public class InstanceStore {

	private Map<String, ApplicationInstance> store;

	/**
	 * Creates a new instance store.
	 */
	public InstanceStore() {
		store = new HashMap<String, ApplicationInstance>();
	}

	/**
	 * Puts an application instance into the instance store.
	 *
	 * @param context The key under which the application instance is stored.
	 * @param instance The application instance to be stored.
	 */
	public final void put(final UserContext context, final ApplicationInstance instance) {
		final String id = context.getApplication() + ":" + context.getResource();
		store.put(id, instance);
	}

	/**
	 * Gets an application instance from the instance store.
	 *
	 * @param context The key under which the application instance was stored.
	 *
	 * @return The stored application instance. Null if there was no instance stored under the given key.
	 */
	public final ApplicationInstance get(final UserContext context) {
		final String id = context.getApplication() + ":" + context.getResource();
		return store.get(id);
	}

	/**
	 * Gets all application instances stored in the instance store.
	 *
	 * @return A potentially empty array containing all application instances stored in the instance store.
	 */
	public final ApplicationInstance[] getAll() {
		final Object[] array = store.values().toArray();
		return Arrays.copyOf(array, array.length, ApplicationInstance[].class);
	}

	/**
	 * Removes the application instance that was stored under the give key.
	 *
	 * @param context The key under which the application instance was stored.
	 */
	public final void remove(final UserContext context) {
		final String id = context.getApplication() + ":" + context.getResource();
		store.remove(id);
	}
}
