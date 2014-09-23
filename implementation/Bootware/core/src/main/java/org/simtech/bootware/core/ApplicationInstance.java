package org.simtech.bootware.core;

import java.util.HashMap;
import java.util.Map;

/**
 * Describes an active instance of an Application.
 * <p>
 * This class contains all the information that describes an active application
 * Instance that was deployed by the bootware. The information saved here is
 * sufficient to undeploy the whole application and the resource it's running on
 * by only supplying the original user context that was used to provision the
 * application in the first place.
 */
public class ApplicationInstance {
	private String id;
	private Map<String, String> instanceInformation;
	private UserContext userContext;

	public ApplicationInstance(final String id) {
		this.id = id;
		instanceInformation = new HashMap<String, String>();
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

	public final void setUserContext(final UserContext context) {
		this.userContext = context;
	}

	public final UserContext getUserContext() {
		return userContext;
	}
}
