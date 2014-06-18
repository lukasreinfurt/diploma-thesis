package org.simtech.bootware.core;

/**
 * A utility class that defines state machine events.
 */
@SuppressWarnings("checkstyle:hideutilityclassconstructor")
public class StateMachineEvents {

	public static final String START    = "Start";
	public static final String SUCCESS  = "Success";
	public static final String FAILURE  = "Failure";
	public static final String REQUEST  = "Request";
	public static final String DEPLOY   = "Deploy";
	public static final String UNDEPLOY = "Undeploy";
	public static final String SHUTDOWN = "Shutdown";

	public StateMachineEvents() {}

}
