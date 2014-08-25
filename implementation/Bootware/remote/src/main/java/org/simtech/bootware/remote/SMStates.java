package org.simtech.bootware.remote;

import org.simtech.bootware.core.StateMachineStates;

/**
 * The state machine states used by the local bootware.
 */
@SuppressWarnings("checkstyle:hideutilityclassconstructor")
public class SMStates extends StateMachineStates {

	public static final String PROVISION_MIDDLEWARE   = "Provision_Middleware";
	public static final String DEPROVISION_MIDDLEWARE = "Deprovision_Middleware";

	public SMStates() {}

}
