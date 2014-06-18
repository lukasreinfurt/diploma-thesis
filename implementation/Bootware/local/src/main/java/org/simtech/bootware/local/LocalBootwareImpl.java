package org.simtech.bootware.local;

import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import javax.jws.WebService;

import org.simtech.bootware.core.AbstractStateMachine;
import org.simtech.bootware.core.Context;
import org.simtech.bootware.core.CredentialsWrapper;
import org.simtech.bootware.core.EndpointsWrapper;
import org.simtech.bootware.core.Request;
import org.simtech.bootware.core.exceptions.DeployException;
import org.simtech.bootware.core.exceptions.SetCredentialsException;
import org.simtech.bootware.core.exceptions.UndeployException;

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
		builder.externalTransition().from("Start").to("Initialize").on("Start");

		// initialize
		buildDefaultTransition("Initialize", "initialize", "Load_Event_Plugins", "Cleanup");
		buildDefaultTransition("Load_Event_Plugins", "loadEventPlugins", "Wait", "Unload_Event_Plugins");

		builder.onEntry("Wait").callMethod("wait");
		builder.externalTransition().from("Wait").to("Read_Context").on("Request");
		builder.externalTransition().from("Wait").to("Unload_Event_Plugins").on("Shutdown");
		builder.externalTransition().from("Wait").to("Unload_Event_Plugins").on("Failure");

		buildDefaultTransition("Read_Context", "readContext", "Send_To_Remote", "Return_Response");

		builder.onEntry("Send_To_Remote").callMethod("sendToRemote");
		builder.externalTransition().from("Send_To_Remote").to("Load_Request_Plugins").on("NoRemote");
		builder.externalTransition().from("Send_To_Remote").to("Return_Response").on("Success");
		builder.externalTransition().from("Send_To_Remote").to("Return_Response").on("Failure");

		builder.onEntry("Load_Request_Plugins").callMethod("loadRequestPlugins");
		builder.externalTransition().from("Load_Request_Plugins").to("Provision_Infrastructure").on("Deploy");
		builder.externalTransition().from("Load_Request_Plugins").to("Stop_Payload").on("Undeploy");
		builder.externalTransition().from("Load_Request_Plugins").to("Unload_Request_Plugins").on("Failure");

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
		buildDefaultTransition("Unload_Request_Plugins", "unloadRequestPlugins", "Send_To_Remote", "Send_To_Remote");
		buildDefaultTransition("Return_Response", "returnResponse", "Wait", "Wait");
		buildDefaultTransition("Unload_Event_Plugins", "unloadEventPlugins", "Cleanup", "Cleanup");
		buildDefaultTransition("Cleanup", "cleanup", "End", "End");

		// end
		builder.onEntry("End").callMethod("end");

		stateMachine = builder.newStateMachine("Start");
	}

	@Override
	public final EndpointsWrapper deploy(final Context context) throws DeployException {
		LocalBootwareImpl.context = context;
		request = new Request();
		stateMachine.fire("Request");
		if (request.isFailing()) {
			throw new DeployException((String) request.getResponse());
		}
		final EndpointsWrapper endpoints = new EndpointsWrapper();
		return endpoints;
	}

	@Override
	public final void undeploy(final Map<String, URL> endpoints) throws UndeployException {
		request = new Request();
		final Iterator it = endpoints.entrySet().iterator();

		if (!it.hasNext()) {
			request.fail("Endpoints cannot be empty");
		}

		if (request.isFailing()) {
			throw new UndeployException((String) request.getResponse());
		}

		while (it.hasNext()) {
			final Map.Entry pairs = (Map.Entry) it.next();
			System.out.println(pairs.getKey() + " = " + pairs.getValue());
		}
	}

	@Override
	public final void setCredentials(final Map<String, CredentialsWrapper> credentialsList) throws SetCredentialsException {
		defaultCredentialsList = credentialsList;
	}

	/**
	 * Describes the entryMethods for the bootware process.
	 */
	static class Machine extends AbstractMachine {

		public Machine() {}

		protected void sendToRemote(final String from, final String to, final String fsmEvent) {
			stateMachine.fire("Success");
			//stateMachine.fire("Failure");
		}

	}

}
