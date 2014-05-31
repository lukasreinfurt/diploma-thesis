package org.simtech.bootware.core;

import java.net.URL;

import org.simtech.bootware.core.events.InfoEvent;
import org.simtech.bootware.core.events.StateTransitionEvent;
import org.simtech.bootware.core.exceptions.ConnectConnectionException;
import org.simtech.bootware.core.exceptions.DeprovisionInfrastructureException;
import org.simtech.bootware.core.exceptions.DeprovisionPayloadException;
import org.simtech.bootware.core.exceptions.DisconnectConnectionException;
import org.simtech.bootware.core.exceptions.LoadPluginException;
import org.simtech.bootware.core.exceptions.ProvisionInfrastructureException;
import org.simtech.bootware.core.exceptions.ProvisionPayloadException;
import org.simtech.bootware.core.exceptions.StartPayloadException;
import org.simtech.bootware.core.exceptions.StopPayloadException;
import org.simtech.bootware.core.exceptions.UnloadPluginException;
import org.simtech.bootware.core.plugins.ConnectionPlugin;
import org.simtech.bootware.core.plugins.EventPlugin;
import org.simtech.bootware.core.plugins.InfrastructurePlugin;
import org.simtech.bootware.core.plugins.PayloadPlugin;

import org.squirrelframework.foundation.component.SquirrelProvider;
import org.squirrelframework.foundation.fsm.DotVisitor;
import org.squirrelframework.foundation.fsm.UntypedStateMachine;
import org.squirrelframework.foundation.fsm.UntypedStateMachineBuilder;
import org.squirrelframework.foundation.fsm.annotation.ContextInsensitive;
import org.squirrelframework.foundation.fsm.annotation.StateMachineParameters;
import org.squirrelframework.foundation.fsm.impl.AbstractUntypedStateMachine;

/**
 * The main bootware program.
 * <p>
 * Implements a finite state machine using squirrelframework.
 * The whole bootware process is executed by this state machine.
 */
public abstract class AbstractStateMachine {

	protected static Context context;
	protected static Connection connection;
	protected static ConnectionPlugin connectionPlugin;
	protected static EventBus eventBus;
	protected static InfrastructurePlugin infrastructurePlugin;
	protected static Instance instance;
	protected static PayloadPlugin payloadPlugin;
	protected static PluginManager pluginManager;
	protected static String response;
	protected static UntypedStateMachine stateMachine;
	protected static URL url;

	protected UntypedStateMachineBuilder builder;

	/**
	 * State transition events.
	 */
	protected enum FSMEvent {
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
	protected final void buildDefaultTransition(String state,
	                                            String entryMethod,
	                                            String successState,
	                                            String failureState) {
		builder.onEntry(state).callMethod(entryMethod);
		builder.externalTransition().from(state).to(successState).on(FSMEvent.Success);
		builder.externalTransition().from(state).to(failureState).on(FSMEvent.Failure);
	}

	/**
	 * Starts the state machine.
	 */
	public final void run() {
		stateMachine.fire(FSMEvent.Start);
	}

	/**
	 * Stops the state machine.
	 */
	public final void stop() {
		stateMachine.fire(FSMEvent.Shutdown);
	}

	/**
	 * Exports the state machine as XML.
	 */
	public final void exportXML() {
		System.out.println(stateMachine.exportXMLDefinition(true));
	}

	/**
	 * Exports the state machine as .dot file for GraphViz.
	 */
	public final void exportDot() {
		final DotVisitor visitor = SquirrelProvider.getInstance().newInstance(DotVisitor.class);
		stateMachine.accept(visitor);
		visitor.convertDotFile("bootware");
	}

	/**
	 * Describes the entryMethods for the bootware process.
	 */
	@ContextInsensitive
	@StateMachineParameters(stateType=String.class, eventType=FSMEvent.class, contextType=Void.class)
	static abstract class AbstractMachine extends AbstractUntypedStateMachine {

		protected void transition(String from, String to, FSMEvent fsmEvent) {
			if (eventBus != null) {
				final StateTransitionEvent event = new StateTransitionEvent();
				event.setMessage("From '" + from + "' to '" + to + "' on '" + fsmEvent + "'.");
				eventBus.publish(event);
			}
		}

		protected void initialize(String from, String to, FSMEvent fsmEvent) {
			eventBus      = new EventBus();
			pluginManager = new PluginManager(eventBus);
			pluginManager.registerSharedObject(eventBus);
			stateMachine.fire(FSMEvent.Success);
			//stateMachine.fire(FSMEvent.Failure);
		}

		protected void loadEventPlugins(String from, String to, FSMEvent fsmEvent) {
			try {
				pluginManager.loadPlugin(EventPlugin.class, "plugins/event/fileLogger-1.0.0.jar");
				pluginManager.loadPlugin(EventPlugin.class, "plugins/event/consoleLogger-1.0.0.jar");
			}
			catch (LoadPluginException e) {
				stateMachine.fire(FSMEvent.Failure);
			}
			stateMachine.fire(FSMEvent.Success);
		}

		protected void wait(String from, String to, FSMEvent fsmEvent) {
			//stateMachine.fire(FSMEvent.Request);
			//stateMachine.fire(FSMEvent.Shutdown);
			//stateMachine.fire(FSMEvent.Failure);
		}

		protected void readContext(String from, String to, FSMEvent fsmEvent) {
			System.out.println("InfrastructureType: " + context.getInfrastructureType());
			System.out.println("ConnectionType: " + context.getConnectionType());
			System.out.println("PayloadType: " + context.getPayloadType());
			stateMachine.fire(FSMEvent.Success);
			//stateMachine.fire(FSMEvent.Failure);
		}

		protected void loadRequestPlugins(String from, String to, FSMEvent fsmEvent) {
			try {
				infrastructurePlugin = pluginManager.loadPlugin(InfrastructurePlugin.class, "plugins/infrastructure/" + context.getInfrastructureType());
				connectionPlugin     = pluginManager.loadPlugin(ConnectionPlugin.class, "plugins/connection/" + context.getConnectionType());
				payloadPlugin        = pluginManager.loadPlugin(PayloadPlugin.class, "plugins/payload/" + context.getPayloadType());
			}
			catch (LoadPluginException e) {
				e.printStackTrace();
				stateMachine.fire(FSMEvent.Failure);
			}
			stateMachine.fire(FSMEvent.Deploy);
			//stateMachine.fire(FSMEvent.Undeploy);
		}

		protected void provisionInfrastructure(String from, String to, FSMEvent fsmEvent) {
			try {
				final Credentials credentials = new Credentials();
				instance = infrastructurePlugin.provision(credentials);
			}
			catch (ProvisionInfrastructureException e) {
				stateMachine.fire(FSMEvent.Failure);
			}
			stateMachine.fire(FSMEvent.Success);
		}

		protected void connect(String from, String to, FSMEvent fsmEvent) {
			try {
				connection = connectionPlugin.connect(instance);
			}
			catch (ConnectConnectionException e) {
				stateMachine.fire(FSMEvent.Failure);
			}
			stateMachine.fire(FSMEvent.Success);
		}

		protected void provisionPayload(String from, String to, FSMEvent fsmEvent) {
			try {
				payloadPlugin.provision(connection);
			}
			catch (ProvisionPayloadException e) {
				stateMachine.fire(FSMEvent.Failure);
			}
			stateMachine.fire(FSMEvent.Success);
		}

		protected void startPayload(String from, String to, FSMEvent fsmEvent) {
			try {
				url = payloadPlugin.start(connection);
			}
			catch (StartPayloadException e) {
				stateMachine.fire(FSMEvent.Failure);
			}
			stateMachine.fire(FSMEvent.Success);
		}

		protected void stopPayload(String from, String to, FSMEvent fsmEvent) {
			try {
				payloadPlugin.stop(connection);
			}
			catch (StopPayloadException e) {
				stateMachine.fire(FSMEvent.Failure);
			}
			stateMachine.fire(FSMEvent.Success);
		}

		protected void deprovisionPayload(String from, String to, FSMEvent fsmEvent) {
			try {
				payloadPlugin.deprovision(connection);
			}
			catch (DeprovisionPayloadException e) {
				stateMachine.fire(FSMEvent.Failure);
			}
			stateMachine.fire(FSMEvent.Success);
		}

		protected void disconnect(String from, String to, FSMEvent fsmEvent) {
			try {
				connectionPlugin.disconnect(connection);
			}
			catch (DisconnectConnectionException e) {
				stateMachine.fire(FSMEvent.Failure);
			}
			stateMachine.fire(FSMEvent.Success);
		}

		protected void deprovisionInfrastructure(String from, String to, FSMEvent fsmEvent) {
			try {
				infrastructurePlugin.deprovision(instance);
			}
			catch (DeprovisionInfrastructureException e) {
				stateMachine.fire(FSMEvent.Failure);
			}
			stateMachine.fire(FSMEvent.Success);
		}

		protected void fatalError(String from, String to, FSMEvent fsmEvent) {
			stateMachine.fire(FSMEvent.Success);
			//stateMachine.fire(FSMEvent.Failure);
		}

		protected void unloadRequestPlugins(String from, String to, FSMEvent fsmEvent) {
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

		protected void returnResponse(String from, String to, FSMEvent fsmEvent) {
			response = "Response";
			stateMachine.fire(FSMEvent.Success);
			//stateMachine.fire(FSMEvent.Failure);
		}

		protected void unloadEventPlugins(String from, String to, FSMEvent fsmEvent) {
			try {
				pluginManager.unloadAllPlugins();
			}
			catch (UnloadPluginException e) {
				stateMachine.fire(FSMEvent.Failure);
			}
			stateMachine.fire(FSMEvent.Success);
		}

		protected void cleanup(String from, String to, FSMEvent fsmEvent) {
			try {
				pluginManager.stop();
			}
			catch (UnloadPluginException e) {
				stateMachine.fire(FSMEvent.Failure);
			}
			stateMachine.fire(FSMEvent.Success);
		}

		protected void end(String from, String to, FSMEvent fsmEvent) {
			stateMachine.terminate();
			final InfoEvent event = new InfoEvent();
			event.setMessage("State machine terminated.");
			eventBus.publish(event);
		}

	}

}
