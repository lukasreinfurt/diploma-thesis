package org.simtech.bootware.core;

import java.util.HashMap;
import java.util.Map;

public class InstanceStore {

	private Map<String, ApplicationInstance> store;

	public InstanceStore() {
		store = new HashMap<String, ApplicationInstance>();
	}

	public final void put(final String id, final ApplicationInstance instance) {
		store.put(id, instance);
	}

	public final ApplicationInstance get(final String id) {
		return store.get(id);
	}

	public final void remove(final String id) {
		store.remove(id);
	}
}
