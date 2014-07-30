package org.simtech.bootware.local;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.jws.WebService;
import javax.xml.ws.WebServiceException;

import org.simtech.bootware.core.AbstractStateMachine;
import org.simtech.bootware.core.ApplicationInstance;
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
	private static URL remoteBootwareURL;

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
		request = new Request("deploy");
		instance = new ApplicationInstance("test");
		instance.setContext(context);

		stateMachine.fire(SMEvents.REQUEST);

		if (request.isFailing()) {
			throw new DeployException((String) request.getResponse());
		}
		else {
			instanceStore.put(instance.getID(), instance);
			System.out.println(instance.getID());
		}

		final InformationListWrapper endpoints = new InformationListWrapper();
		return endpoints;
	}

	@Override
	public final void undeploy(final HashMap<String, String> endpoints) throws UndeployException {
		request = new Request("undeploy");
		instance = instanceStore.get("test");

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
	public final void setConfiguration(final HashMap<String, ConfigurationWrapper> configurationList) throws SetConfigurationException {
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

		@SuppressWarnings("checkstyle:cyclomaticcomplexity")
		protected void sendToRemote(final String from, final String to, final String fsmEvent) {

			if (url != null) {
				remoteBootwareURL = url;
			}

			try {
				remoteBootwareURL = new URL("http://0.0.0.0:8080/axis2/services/Bootware");
			}
			catch (MalformedURLException e) {
				e.printStackTrace();
			}

			if (remoteBootwareURL == null && !triedProvisioningRemote) {
				eventBus.publish(new CoreEvent(Severity.INFO, "No remote bootware deployed yet. Deploying remote bootware."));
				triedProvisioningRemote = true;
				stateMachine.fire(SMEvents.NOREMOTE);
				return;
			}
			else if (remoteBootwareURL == null && triedProvisioningRemote) {
				eventBus.publish(new CoreEvent(Severity.ERROR, "Remote bootware could not be deployed."));
				stateMachine.fire(SMEvents.FAILURE);
				return;
			}
			eventBus.publish(new CoreEvent(Severity.SUCCESS, "Remote bootware found. Passing on request."));

			eventBus.publish(new CoreEvent(Severity.INFO, "Trying to connect to remote bootware at: " + remoteBootwareURL));

			try {
				final RemoteBootwareService remoteBootware = new RemoteBootwareService(remoteBootwareURL);
				final InformationListWrapper infos = remoteBootware.deploy(instance.getContext());
			}
			catch (WebServiceException e) {
				eventBus.publish(new CoreEvent(Severity.ERROR, "Connecting to remote bootware failed: " + e.getMessage()));
				stateMachine.fire(SMEvents.FAILURE);
			}
			catch (DeployException e) {
				eventBus.publish(new CoreEvent(Severity.ERROR, "Executing deploy on remote bootware failed: " + e.getMessage()));
				stateMachine.fire(SMEvents.FAILURE);
			}

			stateMachine.fire(SMEvents.SUCCESS);
		}

	}

}
