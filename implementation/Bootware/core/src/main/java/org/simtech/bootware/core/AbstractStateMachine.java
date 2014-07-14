package org.simtech.bootware.core;

import java.net.URL;
import java.util.Map;

import org.simtech.bootware.core.events.CoreEvent;
import org.simtech.bootware.core.events.FSMEvent;
import org.simtech.bootware.core.events.Severity;
import org.simtech.bootware.core.exceptions.ConfigurationException;
import org.simtech.bootware.core.exceptions.ConnectConnectionException;
import org.simtech.bootware.core.exceptions.DeprovisionApplicationException;
import org.simtech.bootware.core.exceptions.DeprovisionResourceException;
import org.simtech.bootware.core.exceptions.DisconnectConnectionException;
import org.simtech.bootware.core.exceptions.LoadPluginException;
import org.simtech.bootware.core.exceptions.ProvisionApplicationException;
import org.simtech.bootware.core.exceptions.ProvisionResourceException;
import org.simtech.bootware.core.exceptions.StartApplicationException;
import org.simtech.bootware.core.exceptions.StopApplicationException;
import org.simtech.bootware.core.exceptions.UnloadPluginException;
import org.simtech.bootware.core.plugins.ApplicationPlugin;
import org.simtech.bootware.core.plugins.CommunicationPlugin;
import org.simtech.bootware.core.plugins.EventPlugin;
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

	protected static ConfigurationWrapper configuration = new ConfigurationWrapper();
	protected static Context context;
	protected static Connection connection;
	protected static CommunicationPlugin communicationPlugin;
	protected static EventBus eventBus;
	protected static ResourcePlugin resourcePlugin;
	protected static Map<String, String> instanceInformation;
	protected static Map<String, ConfigurationWrapper> defaultConfigurationList;
	protected static ApplicationPlugin applicationPlugin;
	protected static PluginManager pluginManager;
	protected static Request request;
	protected static UntypedStateMachine stateMachine;
	protected static URL url;

	private static String resourcePluginPath      = "plugins/resource/";
	private static String communicationPluginPath = "plugins/communication/";
	private static String applicationPluginPath   = "plugins/application/";

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
				eventBus.publish(new FSMEvent(Severity.DEBUG, "Transition from '" + from + "' to '" + to + "' on '" + fsmEvent + "'."));
			}
		}

		protected void initialize(final String from, final String to, final String fsmEvent) {
			// log to local file?
			eventBus      = new EventBus();
			pluginManager = new PluginManager();
			pluginManager.registerSharedObject(eventBus);
			pluginManager.registerSharedObject(configuration);
			stateMachine.fire(StateMachineEvents.SUCCESS);
			//stateMachine.fire(StateMachineEvents.FAILURE);
		}

		protected void loadEventPlugins(final String from, final String to, final String fsmEvent) {
			// log to local file?
			try {
				pluginManager.loadPlugin(EventPlugin.class, "plugins/event/fileLogger-1.0.0.jar");
				pluginManager.loadPlugin(EventPlugin.class, "plugins/event/consoleLogger-1.0.0.jar");
				eventBus.publish(new CoreEvent(Severity.SUCCESS, "Loading event plugins succeeded."));
			}
			catch (LoadPluginException e) {
				// log to local file?
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
			eventBus.publish(new CoreEvent(Severity.INFO, "Generating context."));

			if ("".equals(context.getResourcePlugin())) {
				request.fail("resourceType cannot be empty");
				stateMachine.fire(StateMachineEvents.FAILURE);
				return;
			}

			try {
				configuration.setConfiguration(context.getConfigurationFor(context.getResourcePlugin()).getConfiguration());
				eventBus.publish(new CoreEvent(Severity.SUCCESS, "Context generated."));
			}
			catch (ConfigurationException e) {
				eventBus.publish(new CoreEvent(Severity.ERROR, "Could not generate context: " + e.getMessage()));
				stateMachine.fire(StateMachineEvents.FAILURE);
			}

			stateMachine.fire(StateMachineEvents.SUCCESS);
		}

		protected void loadRequestPlugins(final String from, final String to, final String fsmEvent) {
			eventBus.publish(new CoreEvent(Severity.INFO, "Loading request plugins."));

			try {
				resourcePlugin      = pluginManager.loadPlugin(ResourcePlugin.class, resourcePluginPath + context.getResourcePlugin());
				communicationPlugin = pluginManager.loadPlugin(CommunicationPlugin.class, communicationPluginPath + context.getCommunicationPlugin());
				applicationPlugin   = pluginManager.loadPlugin(ApplicationPlugin.class, applicationPluginPath + context.getApplicationPlugin());
				eventBus.publish(new CoreEvent(Severity.SUCCESS, "Request plugins loaded."));
			}
			catch (LoadPluginException e) {
				eventBus.publish(new CoreEvent(Severity.ERROR, "Could not load request plugins: " + e.getMessage()));
				stateMachine.fire(StateMachineEvents.FAILURE);
			}

			if ("deploy".equals(request.getType())) {
				stateMachine.fire(StateMachineEvents.DEPLOY);
				return;
			}
			else {
				stateMachine.fire(StateMachineEvents.UNDEPLOY);
				return;
			}
		}

		protected void provisionResource(final String from, final String to, final String fsmEvent) {
			eventBus.publish(new CoreEvent(Severity.INFO, "Provisioning resource."));

			try {
				instanceInformation = resourcePlugin.provision();
				eventBus.publish(new CoreEvent(Severity.SUCCESS, "Resource provisioned."));
			}
			catch (ProvisionResourceException e) {
				eventBus.publish(new CoreEvent(Severity.ERROR, "Could not provision resource: " + e.getMessage()));
				stateMachine.fire(StateMachineEvents.FAILURE);
			}

			stateMachine.fire(StateMachineEvents.SUCCESS);
		}

		protected void connect(final String from, final String to, final String fsmEvent) {
			eventBus.publish(new CoreEvent(Severity.INFO, "Connecting to resource."));

			try {
				connection = communicationPlugin.connect(instanceInformation);
				eventBus.publish(new CoreEvent(Severity.SUCCESS, "Connected to resource."));
			}
			catch (ConnectConnectionException e) {
				eventBus.publish(new CoreEvent(Severity.ERROR, "Could not connect to resource: " + e.getMessage()));
				stateMachine.fire(StateMachineEvents.FAILURE);
			}

			stateMachine.fire(StateMachineEvents.SUCCESS);
		}

		protected void provisionApplication(final String from, final String to, final String fsmEvent) {
			eventBus.publish(new CoreEvent(Severity.INFO, "Provisioning application."));

			try {
				applicationPlugin.provision(connection);
				eventBus.publish(new CoreEvent(Severity.SUCCESS, "Application provisioned."));
			}
			catch (ProvisionApplicationException e) {
				eventBus.publish(new CoreEvent(Severity.ERROR, "Could not provision application: " + e.getMessage()));
				stateMachine.fire(StateMachineEvents.FAILURE);
			}

			stateMachine.fire(StateMachineEvents.SUCCESS);
		}

		protected void startApplication(final String from, final String to, final String fsmEvent) {
			eventBus.publish(new CoreEvent(Severity.INFO, "Starting application."));

			try {
				url = applicationPlugin.start(connection);
				eventBus.publish(new CoreEvent(Severity.SUCCESS, "Application started."));
			}
			catch (StartApplicationException e) {
				eventBus.publish(new CoreEvent(Severity.ERROR, "Could not start application: " + e.getMessage()));
				stateMachine.fire(StateMachineEvents.FAILURE);
			}

			stateMachine.fire(StateMachineEvents.SUCCESS);
		}

		protected void stopApplication(final String from, final String to, final String fsmEvent) {
			eventBus.publish(new CoreEvent(Severity.INFO, "Stopping application."));

			try {
				applicationPlugin.stop(connection);
				eventBus.publish(new CoreEvent(Severity.SUCCESS, "Application stopped."));
			}
			catch (StopApplicationException e) {
				eventBus.publish(new CoreEvent(Severity.WARNING, "Could not stop application: " + e.getMessage()));
				stateMachine.fire(StateMachineEvents.FAILURE);
			}

			stateMachine.fire(StateMachineEvents.SUCCESS);
		}

		protected void deprovisionApplication(final String from, final String to, final String fsmEvent) {
			eventBus.publish(new CoreEvent(Severity.INFO, "Deprovisioning application."));

			try {
				applicationPlugin.deprovision(connection);
				eventBus.publish(new CoreEvent(Severity.SUCCESS, "Application deprovisioned."));
			}
			catch (DeprovisionApplicationException e) {
				eventBus.publish(new CoreEvent(Severity.WARNING, "Could not deprovision application: " + e.getMessage()));
				stateMachine.fire(StateMachineEvents.FAILURE);
			}

			stateMachine.fire(StateMachineEvents.SUCCESS);
		}

		protected void disconnect(final String from, final String to, final String fsmEvent) {
			eventBus.publish(new CoreEvent(Severity.INFO, "Disconnecting from resource."));

			try {
				communicationPlugin.disconnect(connection);
				eventBus.publish(new CoreEvent(Severity.SUCCESS, "Disconnected from resource."));
			}
			catch (DisconnectConnectionException e) {
				eventBus.publish(new CoreEvent(Severity.WARNING, "Could not disconnect from resource: " + e.getMessage()));
				stateMachine.fire(StateMachineEvents.FAILURE);
			}

			stateMachine.fire(StateMachineEvents.SUCCESS);
		}

		protected void deprovisionResource(final String from, final String to, final String fsmEvent) {
			eventBus.publish(new CoreEvent(Severity.INFO, "Deprovisioning resource."));

			try {
				if (instanceInformation != null) {
					resourcePlugin.deprovision(instanceInformation);
				}
				// how to handle failure?
				eventBus.publish(new CoreEvent(Severity.SUCCESS, "Resource deprovisioned."));
			}
			catch (DeprovisionResourceException e) {
				eventBus.publish(new CoreEvent(Severity.ERROR, "Could not deprovision resource: " + e.getMessage()));
				stateMachine.fire(StateMachineEvents.FAILURE);
			}

			stateMachine.fire(StateMachineEvents.SUCCESS);
		}

		protected void fatalError(final String from, final String to, final String fsmEvent) {
			stateMachine.fire(StateMachineEvents.SUCCESS);
			//stateMachine.fire(StateMachineEvents.FAILURE);
		}

		protected void unloadRequestPlugins(final String from, final String to, final String fsmEvent) {
			eventBus.publish(new CoreEvent(Severity.INFO, "Unloading request plugins."));

			try {
				resourcePlugin      = null;
				communicationPlugin = null;
				applicationPlugin   = null;
				pluginManager.unloadPlugin(resourcePluginPath + context.getResourcePlugin());
				pluginManager.unloadPlugin(communicationPluginPath + context.getCommunicationPlugin());
				pluginManager.unloadPlugin(applicationPluginPath + context.getApplicationPlugin());
				eventBus.publish(new CoreEvent(Severity.SUCCESS, "Request plugins unloaded."));
			}
			catch (UnloadPluginException e) {
				eventBus.publish(new CoreEvent(Severity.ERROR, "Could not unload request plugins: " + e.getMessage()));
				stateMachine.fire(StateMachineEvents.FAILURE);
			}

			stateMachine.fire(StateMachineEvents.SUCCESS);
		}

		protected void returnResponse(final String from, final String to, final String fsmEvent) {
			stateMachine.fire(StateMachineEvents.SUCCESS);
			//stateMachine.fire(StateMachineEvents.FAILURE);
		}

		protected void unloadEventPlugins(final String from, final String to, final String fsmEvent) {
			eventBus.publish(new CoreEvent(Severity.INFO, "Unloading event plugins."));

			try {
				pluginManager.unloadAllPlugins();
			}
			catch (UnloadPluginException e) {
				eventBus.publish(new CoreEvent(Severity.ERROR, "Could not unload event plugins: " + e.getMessage()));
				stateMachine.fire(StateMachineEvents.FAILURE);
			}
			// log to local file?
			stateMachine.fire(StateMachineEvents.SUCCESS);
		}

		protected void cleanup(final String from, final String to, final String fsmEvent) {
			// log to local file?
			try {
				pluginManager.stop();
			}
			catch (UnloadPluginException e) {
				stateMachine.fire(StateMachineEvents.FAILURE);
			}
			stateMachine.fire(StateMachineEvents.SUCCESS);
		}

		protected void end(final String from, final String to, final String fsmEvent) {
			// log to local file?
			stateMachine.terminate();
		}

	}

}
