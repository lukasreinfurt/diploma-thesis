package org.simtech.bootware.local;

import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import javax.jws.WebService;

import org.simtech.bootware.core.AbstractStateMachine;
import org.simtech.bootware.core.Context;
import org.simtech.bootware.core.Credentials;
import org.simtech.bootware.core.Endpoints;

import org.squirrelframework.foundation.fsm.StateMachineBuilderFactory;

/**
 * The main bootware program.
 * <p>
 * Implements a finite state machine using squirrelframework.
 * The whole bootware process is executed by this state machine.
 */
@WebService(endpointInterface = "org.simtech.bootware.local.LocalBootware")
public class LocalBootwareImpl extends AbstractStateMachine implements LocalBootware {

	/**
	 * Creates the bootware process as state machine.
	 */
	@SuppressWarnings("checkstyle:multiplestringliterals")
	public LocalBootwareImpl() {
		builder = StateMachineBuilderFactory.create(Machine.class);

		builder.transit().fromAny().toAny().onAny().callMethod("transition");

		// start
		builder.externalTransition().from("Start").to("Initialize").on(FSMEvent.Start);

		// initialize
		buildDefaultTransition("Initialize", "initialize", "Load_Event_Plugins", "Cleanup");
		buildDefaultTransition("Load_Event_Plugins", "loadEventPlugins", "Wait", "Unload_Event_Plugins");

		builder.onEntry("Wait").callMethod("wait");
		builder.externalTransition().from("Wait").to("Read_Context").on(FSMEvent.Request);
		builder.externalTransition().from("Wait").to("Unload_Event_Plugins").on(FSMEvent.Shutdown);
		builder.externalTransition().from("Wait").to("Unload_Event_Plugins").on(FSMEvent.Failure);

		buildDefaultTransition("Read_Context", "readContext", "Load_Request_Plugins", "Return_Response");

		builder.onEntry("Load_Request_Plugins").callMethod("loadRequestPlugins");
		builder.externalTransition().from("Load_Request_Plugins").to("Provision_Infrastructure").on(FSMEvent.Deploy);
		builder.externalTransition().from("Load_Request_Plugins").to("Stop_Payload").on(FSMEvent.Undeploy);
		builder.externalTransition().from("Load_Request_Plugins").to("Unload_Request_Plugins").on(FSMEvent.Failure);

		// deploy
		buildDefaultTransition("Provision_Infrastructure", "provisionInfrastructure", "Connect", "Deprovision_Infrastructure");
		buildDefaultTransition("Connect", "connect", "Provision_Payload", "Disconnect");
		buildDefaultTransition("Provision_Payload", "provisionPayload", "Start_Payload", "Deprovision_Payload");
		buildDefaultTransition("Start_Payload", "startPayload", "Unload_Request_Plugins", "Stop_Payload");

		// undeploy
		buildDefaultTransition("Stop_Payload", "stopPayload", "Deprovision_Payload", "Deprovision_Payload");
		buildDefaultTransition("Deprovision_Payload", "deprovisionPayload", "Disconnect", "Disconnect");
		buildDefaultTransition("Disconnect", "disconnect", "Deprovision_Infrastructure", "Deprovision_Infrastructure");
		buildDefaultTransition("Deprovision_Infrastructure", "deprovisionInfrastructure", "Unload_Request_Plugins", "Fatal_Error");
		buildDefaultTransition("Fatal_Error", "fatalError", "Unload_Request_Plugins", "Unload_Request_Plugins");

		// cleanup
		buildDefaultTransition("Unload_Request_Plugins", "unloadRequestPlugins", "Return_Response", "Return_Response");
		buildDefaultTransition("Return_Response", "returnResponse", "Wait", "Wait");
		buildDefaultTransition("Unload_Event_Plugins", "unloadEventPlugins", "Cleanup", "Cleanup");
		buildDefaultTransition("Cleanup", "cleanup", "End", "End");

		// end
		builder.onEntry("End").callMethod("end");

		stateMachine = builder.newStateMachine("Start");
	}

	@Override
	public final Endpoints deploy(final Context context) {
		LocalBootwareImpl.context = context;
		stateMachine.fire(FSMEvent.Request);
		final Endpoints endpoints = new Endpoints();
		return endpoints;
	}

	@Override
	public final void undeploy(final Endpoints endpoints) {
		final Map<String, URL> endpointsList = endpoints.getEndpointsList();
		final Iterator it = endpointsList.entrySet().iterator();
		while (it.hasNext()) {
			final Map.Entry pairs = (Map.Entry) it.next();
			System.out.println(pairs.getKey() + " = " + pairs.getValue());
		}
	}

	@Override
	public final void setCredentials(final Map<String, Credentials> credentials) {
		final Iterator it = credentials.entrySet().iterator();
		while (it.hasNext()) {
			final Map.Entry pairs = (Map.Entry) it.next();
			System.out.println(pairs.getKey());
			final Credentials credentialsObject = (Credentials) pairs.getValue();
			final Map<String, String> credentialsList = credentialsObject.getCredentialsList();
			final Iterator it2 = credentialsList.entrySet().iterator();
			while (it2.hasNext()) {
				final Map.Entry pairs2 = (Map.Entry) it2.next();
				System.out.println(pairs2.getKey() + " = " + pairs2.getValue());
			}
		}
	}

	/**
	 * Describes the entryMethods for the bootware process.
	 */
	static class Machine extends AbstractMachine {

		public Machine() {}

	}

}
