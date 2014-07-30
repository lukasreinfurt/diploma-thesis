package org.simtech.bootware.core;

import java.util.Map;

public class ApplicationInstance {
	private String id;
	private Map<String, String> instanceInformation;
	private Connection connection;
	private Context context;

	public ApplicationInstance(final String id) {
		this.id = id;
	}

	public final String getID() {
		return id;
	}

	public final void setInstanceInformation(final Map<String, String> instanceInformation) {
		this.instanceInformation = instanceInformation;
	}

	public final Map<String, String> getInstanceInformation() {
		return instanceInformation;
	}

	public final void setConnection(final Connection connection) {
		this.connection = connection;
	}

	public final Connection getConnection() {
		return connection;
	}

	public final void setContext(final Context context) {
		this.context = context;
	}

	public final Context getContext() {
		return context;
	}
}
