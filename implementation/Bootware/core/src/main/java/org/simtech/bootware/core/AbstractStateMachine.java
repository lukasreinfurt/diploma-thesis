package org.simtech.bootware.core;

import java.net.URL;
import java.util.Map;

import org.simtech.bootware.core.events.InfoEvent;
import org.simtech.bootware.core.events.StateTransitionEvent;
import org.simtech.bootware.core.exceptions.ConfigurationException;
import org.simtech.bootware.core.exceptions.ConnectConnectionException;
import org.simtech.bootware.core.exceptions.DeprovisionPayloadException;
import org.simtech.bootware.core.exceptions.DeprovisionResourceException;
import org.simtech.bootware.core.exceptions.DisconnectConnectionException;
import org.simtech.bootware.core.exceptions.LoadPluginException;
import org.simtech.bootware.core.exceptions.ProvisionPayloadException;
import org.simtech.bootware.core.exceptions.ProvisionResourceException;
import org.simtech.bootware.core.exceptions.StartPayloadException;
import org.simtech.bootware.core.exceptions.StopPayloadException;
import org.simtech.bootware.core.exceptions.UnloadPluginException;
import org.simtech.bootware.core.plugins.ConnectionPlugin;
import org.simtech.bootware.core.plugins.EventPlugin;
import org.simtech.bootware.core.plugins.PayloadPlugin;
import org.simtech.bootware.core.plugins.ResourcePlugin;

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
	protected static ResourcePlugin resourcePlugin;
	protected static Instance instance;
	protected static Map<String, ConfigurationWrapper> defaultConfigurationList;
	protected static PayloadPlugin payloadPlugin;
	protected static PluginManager pluginManager;
	protected static Request request;
	protected static UntypedStateMachine stateMachine;
	protected static URL url;

	private static String resourcePluginPath   = "plugins/resource/";
	private static String connectionPluginPath = "plugins/connection/";
	private static String payloadPluginPath    = "plugins/payload/";

	protected UntypedStateMachineBuilder builder;

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
	 * @param failureState The state to which should be transitioned if entryMethod was unsuccessful.
	 */
	protected final void buildDefaultTransition(final String state,
	                                            final String entryMethod,
	                                            final String successState,
	                                            final String failureState) {
		builder.onEntry(state).callMethod(entryMethod);
		builder.externalTransition().from(state).to(successState).on(StateMachineEvents.SUCCESS);
		builder.externalTransition().from(state).to(failureState).on(StateMachineEvents.FAILURE);
	}

	/**
	 * Starts the state machine.
	 */
	public final void run() {
		stateMachine.fire("Start");
	}

	/**
	 * Stops the state machine.
	 */
	public final void stop() {
		stateMachine.fire(StateMachineEvents.SHUTDOWN);
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
	@SuppressWarnings("checkstyle:designforextension")
	@ContextInsensitive
	@StateMachineParameters(stateType = String.class, eventType = String.class, contextType = Void.class)
	protected abstract static class AbstractMachine extends AbstractUntypedStateMachine {

		protected void transition(final String from, final String to, final String fsmEvent) {
			if (eventBus != null) {
				final StateTransitionEvent event = new StateTransitionEvent();
				event.setMessage("From '" + from + "' to '" + to + "' on '" + fsmEvent + "'.");
				eventBus.publish(event);
			}
		}

		protected void initialize(final String from, final String to, final String fsmEvent) {
			eventBus      = new EventBus();
			pluginManager = new PluginManager();
			pluginManager.registerSharedObject(eventBus);
			stateMachine.fire(StateMachineEvents.SUCCESS);
			//stateMachine.fire(StateMachineEvents.FAILURE);
		}

		protected void loadEventPlugins(final String from, final String to, final String fsmEvent) {
			try {
				pluginManager.loadPlugin(EventPlugin.class, "plugins/event/fileLogger-1.0.0.jar");
				pluginManager.loadPlugin(EventPlugin.class, "plugins/event/consoleLogger-1.0.0.jar");
			}
			catch (LoadPluginException e) {
				stateMachine.fire(StateMachineEvents.FAILURE);
			}
			stateMachine.fire(StateMachineEvents.SUCCESS);
		}

		protected void wait(final String from, final String to, final String fsmEvent) {
			//stateMachine.fire(StateMachineEvents.REQUEST);
			//stateMachine.fire(StateMachineEvents.SHUTDOWN);
			//stateMachine.fire(StateMachineEvents.FAILURE);
		}

		protected void readContext(final String from, final String to, final String fsmEvent) {
			if ("".equals(context.getResourcePlugin())) {
				request.fail("resourceType cannot be empty");
				stateMachine.fire(StateMachineEvents.FAILURE);
			}
			System.out.println("ResourceType: " + context.getResourcePlugin());
			System.out.println("ConnectionType: " + context.getConnectionPlugin());
			System.out.println("PayloadType: " + context.getPayloadPlugin());
			stateMachine.fire(StateMachineEvents.SUCCESS);
			//stateMachine.fire(StateMachineEvents.FAILURE);
		}

		protected void loadRequestPlugins(final String from, final String to, final String fsmEvent) {
			try {
				resourcePlugin   = pluginManager.loadPlugin(ResourcePlugin.class, resourcePluginPath + context.getResourcePlugin());
				connectionPlugin = pluginManager.loadPlugin(ConnectionPlugin.class, connectionPluginPath + context.getConnectionPlugin());
				payloadPlugin    = pluginManager.loadPlugin(PayloadPlugin.class, payloadPluginPath + context.getPayloadPlugin());
			}
			catch (LoadPluginException e) {
				stateMachine.fire(StateMachineEvents.FAILURE);
			}
			stateMachine.fire(StateMachineEvents.DEPLOY);
			//stateMachine.fire(StateMachineEvents.UNDEPLOY);
		}

		protected void provisionResource(final String from, final String to, final String fsmEvent) {
			try {
				final ConfigurationWrapper configuration = context.getConfigurationFor(context.getResourcePlugin());
				instance = resourcePlugin.provision(configuration);
			}
			catch (ConfigurationException e) {
				System.out.println(e.toString());
				stateMachine.fire(StateMachineEvents.FAILURE);
			}
			catch (ProvisionResourceException e) {
				stateMachine.fire(StateMachineEvents.FAILURE);
			}
			stateMachine.fire(StateMachineEvents.SUCCESS);
		}

		protected void connect(final String from, final String to, final String fsmEvent) {
			try {
				connection = connectionPlugin.connect(instance);
			}
			catch (ConnectConnectionException e) {
				stateMachine.fire(StateMachineEvents.FAILURE);
			}
			stateMachine.fire(StateMachineEvents.SUCCESS);
		}

		protected void provisionPayload(final String from, final String to, final String fsmEvent) {
			try {
				payloadPlugin.provision(connection);
			}
			catch (ProvisionPayloadException e) {
				stateMachine.fire(StateMachineEvents.FAILURE);
			}
			stateMachine.fire(StateMachineEvents.SUCCESS);
		}

		protected void startPayload(final String from, final String to, final String fsmEvent) {
			try {
				url = payloadPlugin.start(connection);
			}
			catch (StartPayloadException e) {
				stateMachine.fire(StateMachineEvents.FAILURE);
			}
			stateMachine.fire(StateMachineEvents.SUCCESS);
		}

		protected void stopPayload(final String from, final String to, final String fsmEvent) {
			try {
				payloadPlugin.stop(connection);
			}
			catch (StopPayloadException e) {
				stateMachine.fire(StateMachineEvents.FAILURE);
			}
			stateMachine.fire(StateMachineEvents.SUCCESS);
		}

		protected void deprovisionPayload(final String from, final String to, final String fsmEvent) {
			try {
				payloadPlugin.deprovision(connection);
			}
			catch (DeprovisionPayloadException e) {
				stateMachine.fire(StateMachineEvents.FAILURE);
			}
			stateMachine.fire(StateMachineEvents.SUCCESS);
		}

		protected void disconnect(final String from, final String to, final String fsmEvent) {
			try {
				connectionPlugin.disconnect(connection);
			}
			catch (DisconnectConnectionException e) {
				stateMachine.fire(StateMachineEvents.FAILURE);
			}
			stateMachine.fire(StateMachineEvents.SUCCESS);
		}

		protected void deprovisionResource(final String from, final String to, final String fsmEvent) {
			try {
				resourcePlugin.deprovision(instance);
			}
			catch (DeprovisionResourceException e) {
				stateMachine.fire(StateMachineEvents.FAILURE);
			}
			stateMachine.fire(StateMachineEvents.SUCCESS);
		}

		protected void fatalError(final String from, final String to, final String fsmEvent) {
			stateMachine.fire(StateMachineEvents.SUCCESS);
			//stateMachine.fire(StateMachineEvents.FAILURE);
		}

		protected void unloadRequestPlugins(final String from, final String to, final String fsmEvent) {
			try {
				resourcePlugin = null;
				connectionPlugin     = null;
				payloadPlugin        = null;
				pluginManager.unloadPlugin(resourcePluginPath + context.getResourcePlugin());
				pluginManager.unloadPlugin(connectionPluginPath + context.getConnectionPlugin());
				pluginManager.unloadPlugin(payloadPluginPath + context.getPayloadPlugin());
			}
			catch (UnloadPluginException e) {
				stateMachine.fire(StateMachineEvents.FAILURE);
			}
			stateMachine.fire(StateMachineEvents.SUCCESS);
		}

		protected void returnResponse(final String from, final String to, final String fsmEvent) {
			stateMachine.fire(StateMachineEvents.SUCCESS);
			//stateMachine.fire(StateMachineEvents.FAILURE);
		}

		protected void unloadEventPlugins(final String from, final String to, final String fsmEvent) {
			try {
				pluginManager.unloadAllPlugins();
			}
			catch (UnloadPluginException e) {
				stateMachine.fire(StateMachineEvents.FAILURE);
			}
			stateMachine.fire(StateMachineEvents.SUCCESS);
		}

		protected void cleanup(final String from, final String to, final String fsmEvent) {
			try {
				pluginManager.stop();
			}
			catch (UnloadPluginException e) {
				stateMachine.fire(StateMachineEvents.FAILURE);
			}
			stateMachine.fire(StateMachineEvents.SUCCESS);
		}

		protected void end(final String from, final String to, final String fsmEvent) {
			stateMachine.terminate();
			final InfoEvent event = new InfoEvent();
			event.setMessage("State machine terminated.");
			eventBus.publish(event);
		}

	}

}
