package org.simtech.bootware.core;

import java.net.URL;
import javax.jws.WebService;

import org.squirrelframework.foundation.fsm.StateMachineBuilderFactory;
import org.squirrelframework.foundation.fsm.UntypedStateMachineImporter;
import org.squirrelframework.foundation.fsm.UntypedStateMachineBuilder;
import org.squirrelframework.foundation.fsm.UntypedStateMachine;
import org.squirrelframework.foundation.fsm.impl.AbstractUntypedStateMachine;
import org.squirrelframework.foundation.fsm.annotation.StateMachineParameters;

import org.squirrelframework.foundation.component.SquirrelProvider;
import org.squirrelframework.foundation.fsm.Visitor;
import org.squirrelframework.foundation.fsm.DotVisitor;

import org.simtech.bootware.core.plugins.AbstractBasePlugin;
import org.simtech.bootware.core.plugins.AbstractInfrastructurePlugin;
import org.simtech.bootware.core.plugins.AbstractConnectionPlugin;
import org.simtech.bootware.core.plugins.AbstractPayloadPlugin;
import org.simtech.bootware.core.events.StateTransitionEvent;
import org.simtech.bootware.core.events.InfoEvent;
import org.simtech.bootware.core.exceptions.LoadPluginException;
import org.simtech.bootware.core.exceptions.UnloadPluginException;
import org.simtech.bootware.core.exceptions.ProvisionInfrastructureException;
import org.simtech.bootware.core.exceptions.DeprovisionInfrastructureException;
import org.simtech.bootware.core.exceptions.ConnectConnectionException;
import org.simtech.bootware.core.exceptions.DisconnectConnectionException;
import org.simtech.bootware.core.exceptions.ProvisionPayloadException;
import org.simtech.bootware.core.exceptions.DeprovisionPayloadException;
import org.simtech.bootware.core.exceptions.StartPayloadException;
import org.simtech.bootware.core.exceptions.StopPayloadException;

/**
 * The main bootware program.
 * <p>
 * Implements a finite state machine using squirrelframework.
 * The whole bootware process is executed by this state machine.
 */
@WebService(endpointInterface = "org.simtech.bootware.core.Bootware")
public class BootwareImpl implements Bootware {

	private static EventBus eventBus;
	private static PluginManager pluginManager;
	private UntypedStateMachineBuilder builder;
	private static UntypedStateMachine stateMachine;

	private static Context context;
	private static Instance instance;
	private static Connection connection;
	private static URL url;
	private static String response;

	private static AbstractInfrastructurePlugin infrastructurePlugin;
	private static AbstractConnectionPlugin connectionPlugin;
	private static AbstractPayloadPlugin payloadPlugin;

	/**
	 * State transition events.
	 */
	private enum FSMEvent {
		Start, Success, Failure, Request, Deploy, Undeploy, Shutdown
	}

	/**
	 * A helper function to simplify the state machine creation.
	 * <p>
	 * Most of the states in the bootware process follow the same pattern.
	 * On state entry, entryMethod is executed.
	 * If the method is successful, a transition to the successState is made.
	 * If the method is unsuccessful, a transition to the failureState is made.
	 * This helper function simplifies the creation of those states.
	 *
	 * @param state The name of the state that is described.
	 * @param entryMethod The method that is executed once state is entered.
	 * @param successState The state to which should be transitioned if entryMethod was successful.
	 * @param tailureState The state to which should be transitioned if entryMethod was unsuccessful.
	 */
	private void buildDefaultTransition(String state,
	                                    String entryMethod,
	                                    String successState,
	                                    String failureState) {
		builder.onEntry(state).callMethod(entryMethod);
		builder.externalTransition().from(state).to(successState).on(FSMEvent.Success);
		builder.externalTransition().from(state).to(failureState).on(FSMEvent.Failure);
	}

	/**
	 * Describes the entryMethods for the bootware process.
	 */
	@StateMachineParameters(stateType=String.class, eventType=FSMEvent.class, contextType=Integer.class)
	static class Machine extends AbstractUntypedStateMachine {

		protected void transition(String from, String to, FSMEvent fsmEvent, Integer c) {
			StateTransitionEvent event = new StateTransitionEvent();
			event.setMessage("From '" + from + "' to '" + to + "' on '" + fsmEvent + "'.");
			eventBus.publish(event);
		}

		protected void initialize(String from, String to, FSMEvent fsmEvent, Integer c) {
			stateMachine.fire(FSMEvent.Success);
			//stateMachine.fire(FSMEvent.Failure);
		}

		protected void loadEventPlugins(String from, String to, FSMEvent fsmEvent, Integer c) {
			try {
				pluginManager.loadPlugin(AbstractBasePlugin.class, "plugins/event/fileLogger-1.0.0.jar");
				pluginManager.loadPlugin(AbstractBasePlugin.class, "plugins/event/consoleLogger-1.0.0.jar");
			}
			catch (LoadPluginException e) {
				stateMachine.fire(FSMEvent.Failure);
			}
			stateMachine.fire(FSMEvent.Success);
		}

		protected void wait(String from, String to, FSMEvent fsmEvent, Integer c) {
			//stateMachine.fire(FSMEvent.Request);
			//stateMachine.fire(FSMEvent.Shutdown);
			//stateMachine.fire(FSMEvent.Failure);
		}

		protected void readContext(String from, String to, FSMEvent fsmEvent, Integer c) {
			System.out.println("InfrastructureType: " + context.getInfrastructureType());
			System.out.println("ConnectionType: " + context.getConnectionType());
			System.out.println("PayloadType: " + context.getPayloadType());
			stateMachine.fire(FSMEvent.Success);
			//stateMachine.fire(FSMEvent.Failure);
		}

		protected void loadRequestPlugins(String from, String to, FSMEvent fsmEvent, Integer c) {
			try {
				infrastructurePlugin = pluginManager.loadPlugin(AbstractInfrastructurePlugin.class, "plugins/infrastructure/" + context.getInfrastructureType());
				connectionPlugin     = pluginManager.loadPlugin(AbstractConnectionPlugin.class, "plugins/connection/" + context.getConnectionType());
				payloadPlugin        = pluginManager.loadPlugin(AbstractPayloadPlugin.class, "plugins/payload/" + context.getPayloadType());
			}
			catch (LoadPluginException e) {
				e.printStackTrace();
				stateMachine.fire(FSMEvent.Failure);
			}
			stateMachine.fire(FSMEvent.Deploy);
			//stateMachine.fire(FSMEvent.Undeploy);
		}

		protected void provisionInfrastructure(String from, String to, FSMEvent fsmEvent, Integer c) {
			try {
				Credentials credentials = new Credentials();
				instance = infrastructurePlugin.provision(credentials);
			}
			catch (ProvisionInfrastructureException e) {
				stateMachine.fire(FSMEvent.Failure);
			}
			stateMachine.fire(FSMEvent.Success);
		}

		protected void connect(String from, String to, FSMEvent fsmEvent, Integer c) {
			try {
				connection = connectionPlugin.connect(instance);
			}
			catch (ConnectConnectionException e) {
				stateMachine.fire(FSMEvent.Failure);
			}
			stateMachine.fire(FSMEvent.Success);
		}

		protected void provisionPayload(String from, String to, FSMEvent fsmEvent, Integer c) {
			try {
				payloadPlugin.provision(connection);
			}
			catch (ProvisionPayloadException e) {
				stateMachine.fire(FSMEvent.Failure);
			}
			stateMachine.fire(FSMEvent.Success);
		}

		protected void startPayload(String from, String to, FSMEvent fsmEvent, Integer c) {
			try {
				url = payloadPlugin.start(connection);
			}
			catch (StartPayloadException e) {
				stateMachine.fire(FSMEvent.Failure);
			}
			stateMachine.fire(FSMEvent.Success);
		}

		protected void stopPayload(String from, String to, FSMEvent fsmEvent, Integer c) {
			try {
				payloadPlugin.stop(connection);
			}
			catch (StopPayloadException e) {
				stateMachine.fire(FSMEvent.Failure);
			}
			stateMachine.fire(FSMEvent.Success);
		}

		protected void deprovisionPayload(String from, String to, FSMEvent fsmEvent, Integer c) {
			try {
				payloadPlugin.deprovision(connection);
			}
			catch (DeprovisionPayloadException e) {
				stateMachine.fire(FSMEvent.Failure);
			}
			stateMachine.fire(FSMEvent.Success);
		}

		protected void disconnect(String from, String to, FSMEvent fsmEvent, Integer c) {
			try {
				connectionPlugin.disconnect(connection);
			}
			catch (DisconnectConnectionException e) {
				stateMachine.fire(FSMEvent.Failure);
			}
			stateMachine.fire(FSMEvent.Success);
		}

		protected void deprovisionInfrastructure(String from, String to, FSMEvent fsmEvent, Integer c) {
			try {
				infrastructurePlugin.deprovision(instance);
			}
			catch (DeprovisionInfrastructureException e) {
				stateMachine.fire(FSMEvent.Failure);
			}
			stateMachine.fire(FSMEvent.Success);
		}

		protected void fatalError(String from, String to, FSMEvent fsmEvent, Integer c) {
			try {
				Thread.sleep(1000);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			stateMachine.fire(FSMEvent.Success);
			//stateMachine.fire(FSMEvent.Failure);
		}

		protected void unloadRequestPlugins(String from, String to, FSMEvent fsmEvent, Integer c) {
			try {
				infrastructurePlugin = null;
				connectionPlugin     = null;
				payloadPlugin        = null;
				pluginManager.unloadPlugin("plugins/infrastructure/" + context.getInfrastructureType());
				pluginManager.unloadPlugin("plugins/connection/" + context.getConnectionType());
				pluginManager.unloadPlugin("plugins/payload/" + context.getPayloadType());
			}
			catch (UnloadPluginException e) {
				stateMachine.fire(FSMEvent.Failure);
			}
			stateMachine.fire(FSMEvent.Success);
		}

		protected void returnResponse(String from, String to, FSMEvent fsmEvent, Integer c) {
			response = "Response";
			stateMachine.fire(FSMEvent.Success);
			//stateMachine.fire(FSMEvent.Failure);
		}

		protected void unloadEventPlugins(String from, String to, FSMEvent fsmEvent, Integer c) {
			try {
				pluginManager.unloadAllPlugins();
			}
			catch (UnloadPluginException e) {
				stateMachine.fire(FSMEvent.Failure);
			}
			stateMachine.fire(FSMEvent.Success);
		}

		protected void cleanup(String from, String to, FSMEvent fsmEvent, Integer c) {
			try {
				pluginManager.stop();
			}
			catch (UnloadPluginException e) {
				stateMachine.fire(FSMEvent.Failure);
			}
			stateMachine.fire(FSMEvent.Success);
		}

		protected void end(String from, String to, FSMEvent fsmEvent, Integer c) {
			stateMachine.terminate(10);
			InfoEvent event = new InfoEvent();
			event.setMessage("State machine terminated.");
			eventBus.publish(event);
		}

	}

	/**
	 * Creates the bootware process as state machine.
	 *
	 * @param eventBus The event bus to be used by the state machine.
	 */
	public BootwareImpl() {
		eventBus      = new EventBus();
		pluginManager = new PluginManager(eventBus);
		pluginManager.registerSharedObject(eventBus);
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

	/**
	 * Starts the state machine.
	 */
	public void run() {
		stateMachine.fire(FSMEvent.Start, 10);
	}

	/**
	 * Stops the state machine.
	 */
	public void stop() {
		stateMachine.fire(FSMEvent.Shutdown, 10);
	}

	/**
	 * Exports the state machine as XML.
	 */
	public void exportXML() {
		System.out.println(stateMachine.exportXMLDefinition(true));
	}

	/**
	 * Exports the state machine as .dot file for GraphViz.
	 */
	public void exportDot() {
		DotVisitor visitor = SquirrelProvider.getInstance().newInstance(DotVisitor.class);
		stateMachine.accept(visitor);
		visitor.convertDotFile("bootware");
	}

	@Override
	public String deploy(Context context) {
		this.context = context;
		stateMachine.fire(FSMEvent.Request);
		return response;
	}
}
