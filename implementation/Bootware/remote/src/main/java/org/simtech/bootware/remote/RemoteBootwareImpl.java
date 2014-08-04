package org.simtech.bootware.remote;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.jws.WebService;

import org.simtech.bootware.core.AbstractStateMachine;
import org.simtech.bootware.core.ApplicationInstance;
import org.simtech.bootware.core.ConfigurationWrapper;
import org.simtech.bootware.core.Context;
import org.simtech.bootware.core.InformationListWrapper;
import org.simtech.bootware.core.Request;
import org.simtech.bootware.core.StateMachineEvents;
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
@WebService(endpointInterface = "org.simtech.bootware.remote.RemoteBootware")
public class RemoteBootwareImpl extends AbstractStateMachine implements RemoteBootware {

	/**
	 * Creates the bootware process as state machine.
	 */
	@SuppressWarnings("checkstyle:multiplestringliterals")
	public RemoteBootwareImpl() {
		builder = StateMachineBuilderFactory.create(Machine.class);

		builder.transit().fromAny().toAny().onAny().callMethod("transition");

		// start
		builder.externalTransition().from("Start").to("Initialize").on(StateMachineEvents.START);

		// initialize
		buildDefaultTransition("Initialize", "initialize", "Load_Event_Plugins", "Cleanup");
		buildDefaultTransition("Load_Event_Plugins", "loadEventPlugins", "Wait", "Unload_Event_Plugins");

		builder.onEntry("Wait").callMethod("wait");
		builder.externalTransition().from("Wait").to("Read_Context").on(StateMachineEvents.REQUEST);
		builder.externalTransition().from("Wait").to("Unload_Event_Plugins").on(StateMachineEvents.SHUTDOWN);
		builder.externalTransition().from("Wait").to("Unload_Event_Plugins").on(StateMachineEvents.FAILURE);

		buildDefaultTransition("Read_Context", "readContext", "Load_Request_Plugins", "Return_Response");

		builder.onEntry("Load_Request_Plugins").callMethod("loadRequestPlugins");
		builder.externalTransition().from("Load_Request_Plugins").to("Provision_Resource").on(StateMachineEvents.DEPLOY);
		builder.externalTransition().from("Load_Request_Plugins").to("Stop_Application").on(StateMachineEvents.UNDEPLOY);
		builder.externalTransition().from("Load_Request_Plugins").to("Unload_Request_Plugins").on(StateMachineEvents.FAILURE);

		// deploy
		buildDefaultTransition("Provision_Resource", "provisionResource", "Connect", "Deprovision_Resource");
		buildDefaultTransition("Connect", "connect", "Provision_Application", "Disconnect");
		buildDefaultTransition("Provision_Application", "provisionApplication", "Start_Application", "Deprovision_Application");
		buildDefaultTransition("Start_Application", "startApplication", "Provision_Middleware", "Stop_Application");
		buildDefaultTransition("Provision_Middleware", "provisionMiddleware", "Unload_Request_Plugins", "Deprovision_Middleware");

		// undeploy
		buildDefaultTransition("Deprovision_Middleware", "deprovisionMiddleware", "Stop_Application", "Stop_Application");
		buildDefaultTransition("Stop_Application", "stopApplication", "Deprovision_Application", "Deprovision_Application");
		buildDefaultTransition("Deprovision_Application", "deprovisionApplication", "Disconnect", "Disconnect");
		buildDefaultTransition("Disconnect", "disconnect", "Deprovision_Resource", "Deprovision_Resource");
		buildDefaultTransition("Deprovision_Resource", "deprovisionResource", "Unload_Request_Plugins", "Fatal_Error");
		buildDefaultTransition("Fatal_Error", "fatalError", "Unload_Request_Plugins", "Unload_Request_Plugins");

		// cleanup
		buildDefaultTransition("Unload_Request_Plugins", "unloadRequestPlugins", "Return_Response", "Return_Response");
		buildDefaultTransition("Return_Response", "returnResponse", "Wait", "Wait");
		buildDefaultTransition("Unload_Event_Plugins", "unloadEventPlugins", "Cleanup", "Cleanup");
		buildDefaultTransition("Cleanup", "cleanup", "End", "End");

		// end
		builder.onEntry("End").callMethod("end");

		stateMachine = builder.newStateMachine(StateMachineEvents.START);
	}

	@Override
	public final InformationListWrapper deploy(final Context context) throws DeployException {
		request = new Request("deploy");
		instance = new ApplicationInstance("test");
		instance.setContext(context);

		stateMachine.fire(StateMachineEvents.REQUEST);

		if (request.isFailing()) {
			throw new DeployException((String) request.getResponse());
		}

		final InformationListWrapper endpoints = new InformationListWrapper();
		return endpoints;
	}

	@Override
	public final void undeploy(final HashMap<String, String> endpoints) throws UndeployException {
		request = new Request("undeploy");
		final Iterator it = endpoints.entrySet().iterator();

		if (!it.hasNext()) {
			request.fail("Endpoints cannot be empty");
		}

		stateMachine.fire(StateMachineEvents.REQUEST);

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
		// undeploy workflow middleware
		eventBus.publish(new CoreEvent(Severity.INFO, "Deprovision Workflow Middleware"));

		// undeploy all provisioning engines
		eventBus.publish(new CoreEvent(Severity.INFO, "Undeploy applications."));

		// trigger shutdown in thread after delay so that this method can return before it
		final Thread delayedShutdown = new Thread() {
			public void run() {
				try {
					final Integer time = 2000;
					Thread.sleep(time);
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
				stateMachine.fire(StateMachineEvents.SHUTDOWN);
			}
		};

		eventBus.publish(new CoreEvent(Severity.INFO, "Shutting down."));
		delayedShutdown.start();
	}

	/**
	 * Describes the entryMethods for the bootware process.
	 */
	static class Machine extends AbstractMachine {

		public Machine() {}

		protected void provisionMiddleware(final String from, final String to, final String fsmEvent) {
			try {
				final Integer time = 10000;
				Thread.sleep(time);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			stateMachine.fire(StateMachineEvents.SUCCESS);
			//stateMachine.fire(StateMachineEvents.FAILURE);
		}

		protected void deprovisionMiddleware(final String from, final String to, final String fsmEvent) {
			stateMachine.fire(StateMachineEvents.SUCCESS);
			//stateMachine.fire(StateMachineEvents.FAILURE);
		}

	}

}
