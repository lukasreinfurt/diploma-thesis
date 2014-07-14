package org.simtech.bootware.local;

import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import javax.jws.WebService;

import org.simtech.bootware.core.AbstractStateMachine;
import org.simtech.bootware.core.ConfigurationWrapper;
import org.simtech.bootware.core.Context;
import org.simtech.bootware.core.InformationListWrapper;
import org.simtech.bootware.core.Request;
import org.simtech.bootware.core.events.CoreEvent;
import org.simtech.bootware.core.events.Severity;
import org.simtech.bootware.core.exceptions.DeployException;
import org.simtech.bootware.core.exceptions.SetConfigurationException;
import org.simtech.bootware.core.exceptions.ShutdownException;
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

	private static Boolean triedProvisioningRemote = false;
	private static URL remoteBootware;

	/**
	 * Creates the bootware process as state machine.
	 */
	@SuppressWarnings("checkstyle:multiplestringliterals")
	public LocalBootwareImpl() {
		builder = StateMachineBuilderFactory.create(Machine.class);

		builder.transit().fromAny().toAny().onAny().callMethod("transition");

		// start
		builder.externalTransition().from("Start").to("Initialize").on(SMEvents.START);

		// initialize
		buildDefaultTransition("Initialize", "initialize", "Load_Event_Plugins", "Cleanup");
		buildDefaultTransition("Load_Event_Plugins", "loadEventPlugins", "Wait", "Unload_Event_Plugins");

		builder.onEntry("Wait").callMethod("wait");
		builder.externalTransition().from("Wait").to("Read_Context").on(SMEvents.REQUEST);
		builder.externalTransition().from("Wait").to("Unload_Event_Plugins").on(SMEvents.SHUTDOWN);
		builder.externalTransition().from("Wait").to("Unload_Event_Plugins").on(SMEvents.FAILURE);

		buildDefaultTransition("Read_Context", "readContext", "Send_To_Remote", "Return_Response");

		builder.onEntry("Send_To_Remote").callMethod("sendToRemote");
		builder.externalTransition().from("Send_To_Remote").to("Load_Request_Plugins").on(SMEvents.NOREMOTE);
		builder.externalTransition().from("Send_To_Remote").to("Return_Response").on(SMEvents.SUCCESS);
		builder.externalTransition().from("Send_To_Remote").to("Return_Response").on(SMEvents.FAILURE);

		builder.onEntry("Load_Request_Plugins").callMethod("loadRequestPlugins");
		builder.externalTransition().from("Load_Request_Plugins").to("Provision_Resource").on(SMEvents.DEPLOY);
		builder.externalTransition().from("Load_Request_Plugins").to("Stop_Application").on(SMEvents.UNDEPLOY);
		builder.externalTransition().from("Load_Request_Plugins").to("Unload_Request_Plugins").on(SMEvents.FAILURE);

		// deploy
		buildDefaultTransition("Provision_Resource", "provisionResource", "Connect", "Deprovision_Resource");
		buildDefaultTransition("Connect", "connect", "Provision_Application", "Disconnect");
		buildDefaultTransition("Provision_Application", "provisionApplication", "Start_Application", "Deprovision_Application");
		buildDefaultTransition("Start_Application", "startApplication", "Unload_Request_Plugins", "Stop_Application");

		// undeploy
		buildDefaultTransition("Stop_Application", "stopApplication", "Deprovision_Application", "Deprovision_Application");
		buildDefaultTransition("Deprovision_Application", "deprovisionApplication", "Disconnect", "Disconnect");
		buildDefaultTransition("Disconnect", "disconnect", "Deprovision_Resource", "Deprovision_Resource");
		buildDefaultTransition("Deprovision_Resource", "deprovisionResource", "Unload_Request_Plugins", "Fatal_Error");
		buildDefaultTransition("Fatal_Error", "fatalError", "Unload_Request_Plugins", "Unload_Request_Plugins");

		// cleanup
		buildDefaultTransition("Unload_Request_Plugins", "unloadRequestPlugins", "Send_To_Remote", "Send_To_Remote");
		buildDefaultTransition("Return_Response", "returnResponse", "Wait", "Wait");
		buildDefaultTransition("Unload_Event_Plugins", "unloadEventPlugins", "Cleanup", "Cleanup");
		buildDefaultTransition("Cleanup", "cleanup", "End", "End");

		// end
		builder.onEntry("End").callMethod("end");

		stateMachine = builder.newStateMachine(SMEvents.START);
	}

	@Override
	public final InformationListWrapper deploy(final Context context) throws DeployException {
		LocalBootwareImpl.context = context;
		request = new Request("deploy");
		stateMachine.fire(SMEvents.REQUEST);
		if (request.isFailing()) {
			throw new DeployException((String) request.getResponse());
		}
		final InformationListWrapper endpoints = new InformationListWrapper();
		return endpoints;
	}

	@Override
	public final void undeploy(final Map<String, String> endpoints) throws UndeployException {
		request = new Request("undeploy");
		final Iterator it = endpoints.entrySet().iterator();

		if (!it.hasNext()) {
			request.fail("Endpoints cannot be empty");
		}

		stateMachine.fire(SMEvents.REQUEST);

		if (request.isFailing()) {
			throw new UndeployException((String) request.getResponse());
		}

		while (it.hasNext()) {
			final Map.Entry pairs = (Map.Entry) it.next();
			System.out.println(pairs.getKey() + " = " + pairs.getValue());
		}
	}

	@Override
	public final void setConfiguration(final Map<String, ConfigurationWrapper> configurationList) throws SetConfigurationException {
		defaultConfigurationList = configurationList;
	}

	@Override
	public final void shutdown() throws ShutdownException {
		stateMachine.fire(SMEvents.SHUTDOWN);
	}

	/**
	 * Describes the entryMethods for the bootware process.
	 */
	static class Machine extends AbstractMachine {

		public Machine() {}

		protected void sendToRemote(final String from, final String to, final String fsmEvent) {
			if (remoteBootware == null && !triedProvisioningRemote) {
				eventBus.publish(new CoreEvent(Severity.INFO, "No remote bootware deployed yet. Deploying remote bootware."));
				triedProvisioningRemote = true;
				stateMachine.fire(SMEvents.NOREMOTE);
				return;
			}
			else if (remoteBootware == null && triedProvisioningRemote) {
				eventBus.publish(new CoreEvent(Severity.ERROR, "Remote bootware could not be deployed."));
				stateMachine.fire(SMEvents.FAILURE);
				return;
			}
			eventBus.publish(new CoreEvent(Severity.SUCCESS, "Remote bootware found. Passing on request."));
			stateMachine.fire(SMEvents.SUCCESS);
		}

	}

}
