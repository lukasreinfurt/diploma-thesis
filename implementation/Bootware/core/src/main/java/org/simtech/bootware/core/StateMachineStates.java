package org.simtech.bootware.core;

/**
 * A utility class that defines the default state machine states.
 */
@SuppressWarnings("checkstyle:hideutilityclassconstructor")
public class StateMachineStates {

	public static final String START                   = "Start";
	public static final String INITIALIZE              = "Initialize";
	public static final String LOAD_EVENT_PLUGINS      = "Load_Event_Plugins";
	public static final String WAIT                    = "Wait";
	public static final String READ_CONTEXT            = "Read_Context";
	public static final String LOAD_REQUEST_PLUGINS    = "Load_Request_Plugins";
	public static final String PROVISION_RESOURCE      = "Provision_Resource";
	public static final String CONNECT                 = "Connect";
	public static final String PROVISION_APPLICATION   = "Provision_Application";
	public static final String START_APPLICATION       = "Start_Application";
	public static final String STOP_APPLICATION        = "Stop_Application";
	public static final String DEPROVISION_APPLICATION = "Deprovision_Application";
	public static final String DISCONNECT              = "Disconnect";
	public static final String DEPROVISION_RESOURCE    = "Deprovision_Resource";
	public static final String FATAL_ERROR             = "Fatal_Error";
	public static final String UNLOAD_REQUEST_PLUGINS  = "Unload_Request_Plugins";
	public static final String UNLOAD_EVENT_PLUGINS    = "Unload_Event_Plugins";
	public static final String CLEANUP                 = "Cleanup";
	public static final String END                     = "End";

	public StateMachineStates() {}

}
