package org.simtech.bootware.core;

import java.util.HashMap;
import java.util.Map;

public class InstanceStore {

	private Map<String, ApplicationInstance> store;

	public InstanceStore() {
		store = new HashMap<String, ApplicationInstance>();
	}

	public final void put(final UserContext context, final ApplicationInstance instance) {
		final String id = context.getApplication() + ":" + context.getResource();
		store.put(id, instance);
	}

	public final ApplicationInstance get(final UserContext context) {
		final String id = context.getApplication() + ":" + context.getResource();
		return store.get(id);
	}

	public final void remove(final UserContext context) {
		final String id = context.getApplication() + ":" + context.getResource();
		store.remove(id);
	}
}
