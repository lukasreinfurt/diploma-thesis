package org.simtech.bootware.core;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.simtech.bootware.core.events.CoreEvent;
import org.simtech.bootware.core.events.FSMEvent;
import org.simtech.bootware.core.events.Severity;
import org.simtech.bootware.core.exceptions.ConnectConnectionException;
import org.simtech.bootware.core.exceptions.ContextMappingException;
import org.simtech.bootware.core.exceptions.DeprovisionApplicationException;
import org.simtech.bootware.core.exceptions.DeprovisionResourceException;
import org.simtech.bootware.core.exceptions.DisconnectConnectionException;
import org.simtech.bootware.core.exceptions.InitializeException;
import org.simtech.bootware.core.exceptions.InitializePluginManagerException;
import org.simtech.bootware.core.exceptions.LoadPluginException;
import org.simtech.bootware.core.exceptions.ProvisionApplicationException;
import org.simtech.bootware.core.exceptions.ProvisionResourceException;
import org.simtech.bootware.core.exceptions.StartApplicationException;
import org.simtech.bootware.core.exceptions.StopApplicationException;
import org.simtech.bootware.core.exceptions.UnloadPluginException;
import org.simtech.bootware.core.plugins.ApplicationPlugin;
import org.simtech.bootware.core.plugins.CommunicationPlugin;
import org.simtech.bootware.core.plugins.EventPlugin;
import org.simtech.bootware.core.plugins.PluginTypes;
import org.simtech.bootware.core.plugins.ResourcePlugin;

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

	protected static Boolean stopped = false;
	protected static Properties properties = new Properties();

	protected static EventBus eventBus;
	protected static PluginManager pluginManager;
	protected static InstanceStore instanceStore;
	protected static ContextMapper contextMapper;

	protected static ResourcePlugin resourcePlugin;
	protected static CommunicationPlugin communicationPlugin;
	protected static ApplicationPlugin applicationPlugin;

	protected static Map<String, ConfigurationWrapper> defaultConfigurationList = new HashMap<String, ConfigurationWrapper>();
	protected static Map<String, ConfigurationWrapper> configurationList = new HashMap<String, ConfigurationWrapper>();

	protected static Request request;

	protected static UntypedStateMachine stateMachine;
	protected static URL url;

	protected static ApplicationInstance instance;

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
	 * Returns the stopped state of the state machine.
	 *
	 * @return Returns true if the state machine has been stopped, otherwise false.
	 */
	public final Boolean hasStopped() {
		return stopped;
	}

	/**
	 * Describes the entryMethods for the bootware process.
	 */
	@SuppressWarnings({
			"checkstyle:designforextension",
			"checkstyle:classfanoutcomplexity"
			})
	@ContextInsensitive
	@StateMachineParameters(stateType = String.class, eventType = String.class, contextType = Void.class)
	protected abstract static class AbstractMachine extends AbstractUntypedStateMachine {

		/**
		 * Convenience method to call when request should fail.
		 *
		 * @param reason A string containing the reason of the failure.
		 */
		protected void fail(final String reason) {
			request.fail(reason);
			eventBus.publish(new CoreEvent(Severity.ERROR, reason));
		}

		/**
		 * Debug output on each state transition.
		 */
		protected void transition(final String from, final String to, final String fsmEvent) {
			if (eventBus != null) {
				eventBus.publish(new FSMEvent(Severity.DEBUG, "Transition from '" + from + "' to '" + to + "' on '" + fsmEvent + "'."));
			}
		}

		/**
		 * Initializes all objects that are needed for the bootstrapping process.
		 */
		protected void initialize(final String from, final String to, final String fsmEvent) {


			try {
				// Load properties file.
				System.out.println("Loading properties file.");
				final InputStream propFile = new FileInputStream("config.properties");
				properties.load(propFile);

				// Initialize some objects.
				instanceStore = new InstanceStore();
				eventBus      = new EventBus();
				pluginManager = new PluginManager(properties.getProperty("repositoryURL"));
				contextMapper = new ContextMapper(properties.getProperty("repositoryURL"));

				// Register objects that are shared with plugins.
				pluginManager.registerSharedObject(eventBus);
			}
			catch (IOException e) {
				System.out.println("Properties file could not be loaded: " + e.getMessage());
				stateMachine.fire(StateMachineEvents.FAILURE);
				return;
			}
			catch (InitializePluginManagerException e) {
				System.out.println("The plugin manager could not be initialized: " + e.getMessage());
				stateMachine.fire(StateMachineEvents.FAILURE);
				return;
			}

			stateMachine.fire(StateMachineEvents.SUCCESS);
		}

		/**
		 * Load all event plugins.
		 */
		protected void loadEventPlugins(final String from, final String to, final String fsmEvent) {

			// Get all event plugins that should be loaded from properties and load them.
			try {
				final String[] eventPlugins = properties.getProperty("eventPlugins").split(";");

				for (String eventPlugin : eventPlugins) {
					final EventPlugin plugin = pluginManager.loadPlugin(EventPlugin.class, PluginTypes.EVENT, eventPlugin);
					plugin.initialize(configurationList);
				}

				eventBus.publish(new CoreEvent(Severity.SUCCESS, "Loading event plugins succeeded."));
			}
			catch (LoadPluginException e) {
				e.printStackTrace();
				stateMachine.fire(StateMachineEvents.FAILURE);
				return;
			}
			catch (InitializeException e) {
				e.printStackTrace();
				stateMachine.fire(StateMachineEvents.FAILURE);
				return;
			}

			stateMachine.fire(StateMachineEvents.SUCCESS);
		}

		protected void wait(final String from, final String to, final String fsmEvent) {
			// No op. State machine events are handled by web service operations in
			// local and remote bootware implementation.
		}

		/**
		 * Merge two or more configuration lists.
		 * Values in later lists override values in earlier lists.
		 */
		private Map<String, ConfigurationWrapper> mergeConfigurationLists(final Map<String, ConfigurationWrapper>... configurationLists) {

			final Map<String, ConfigurationWrapper> mergedMap = new HashMap<String, ConfigurationWrapper>();

			// Debug output of the input maps.
			/*
			System.out.println(">>>> Input maps:");
			for (Map<String, ConfigurationWrapper> configurationList : configurationLists) {
				for (Map.Entry<String, ConfigurationWrapper> entry : configurationList.entrySet()) {
					System.out.println(entry.getKey());
					final Map<String, String> configuration = entry.getValue().getConfiguration();
					for (Map.Entry<String, String> entry2 : configuration.entrySet()) {
						System.out.println("    " + entry2.getKey() + ": " + entry2.getValue());
					}
				}
			}
			*/

			for (Map<String, ConfigurationWrapper> configurationList : configurationLists) {
				for (Map.Entry<String, ConfigurationWrapper> entry : configurationList.entrySet()) {
					// If there is already a key in the merged map with the same name as
					// the currently looked at entry, merge them both.
					if (mergedMap.containsKey(entry.getKey())) {
						final Map<String, String> configuration = mergedMap.get(entry.getKey()).getConfiguration();
						final Map<String, String> newConfiguration = entry.getValue().getConfiguration();
						configuration.putAll(newConfiguration);
						final ConfigurationWrapper configurationWrapper = new ConfigurationWrapper();
						configurationWrapper.setConfiguration(configuration);
						mergedMap.put(entry.getKey(), configurationWrapper);
					}
					// Else just put the entry in the merged map.
					else {
						mergedMap.put(entry.getKey(), entry.getValue());
					}
				}
			}

			// Debug output of the resulting merged map.
			/*
			System.out.println(">>>> Output map:");
			for (Map.Entry<String, ConfigurationWrapper> entry : mergedMap.entrySet()) {
				System.out.println(entry.getKey());
				final Map<String, String> configuration = entry.getValue().getConfiguration();
				for (Map.Entry<String, String> entry2 : configuration.entrySet()) {
					System.out.println("    " + entry2.getKey() + ": " + entry2.getValue());
				}
			}
			*/

			return mergedMap;
		}

		/**
		 * Map the user context from the request to the actual context.
		 */
		protected void readContext(final String from, final String to, final String fsmEvent) {

			eventBus.publish(new CoreEvent(Severity.INFO, "Generating context."));

			try {
				// map user context to request context
				final UserContext userContext = instance.getUserContext();
				final RequestContext requestContext = contextMapper.map(userContext);

				// use service package reference from user context if given
				final String servicePackageReference = userContext.getServicePackageReference();
				if (servicePackageReference != null && !"".equals(servicePackageReference)) {
					requestContext.setServicePackageReference(servicePackageReference);
				}

				request.setRequestContext(requestContext);

				// merge configurations
				final Map<String, ConfigurationWrapper> userConfigurationList = userContext.getConfigurationList();
				final Map<String, ConfigurationWrapper> requestConfigurationList = requestContext.getConfigurationList();
				configurationList = mergeConfigurationLists(defaultConfigurationList, userConfigurationList, requestConfigurationList);
			}
			catch (ContextMappingException e) {
				fail("Could not map userContext to requestContext: " + e.getMessage());
				stateMachine.fire(StateMachineEvents.FAILURE);
				return;
			}

			stateMachine.fire(StateMachineEvents.SUCCESS);
		}

		/**
		 * Load the request plugins specified in the context.
		 */
		protected void loadRequestPlugins(final String from, final String to, final String fsmEvent) {

			eventBus.publish(new CoreEvent(Severity.INFO, "Loading request plugins."));

			final RequestContext context = request.getRequestContext();

			try {
				resourcePlugin = pluginManager.loadPlugin(ResourcePlugin.class, PluginTypes.RESOURCE, context.getResourcePlugin());
				resourcePlugin.initialize(configurationList);

				communicationPlugin = pluginManager.loadPlugin(CommunicationPlugin.class, PluginTypes.COMMUNICATION, context.getCommunicationPlugin());
				communicationPlugin.initialize(configurationList);

				applicationPlugin = pluginManager.loadPlugin(ApplicationPlugin.class, PluginTypes.APPLICATION, context.getApplicationPlugin());
				applicationPlugin.initialize(configurationList);

				eventBus.publish(new CoreEvent(Severity.SUCCESS, "Request plugins loaded."));
			}
			catch (LoadPluginException e) {
				fail("Could not load request plugins: " + e.getMessage());
				stateMachine.fire(StateMachineEvents.FAILURE);
				return;
			}
			catch (InitializeException e) {
				fail("Could not initialize request plugins: " + e.getMessage());
				stateMachine.fire(StateMachineEvents.FAILURE);
				return;
			}

			// Transition to deploy or undeploy state, depending on the request type.
			if ("deploy".equals(request.getType())) {
				stateMachine.fire(StateMachineEvents.DEPLOY);
				return;
			}
			else {
				stateMachine.fire(StateMachineEvents.UNDEPLOY);
				return;
			}
		}

		/**
		 * Provision the resource described by the resource plugin (e.g. AWS EC2).
		 */
		protected void provisionResource(final String from, final String to, final String fsmEvent) {

			eventBus.publish(new CoreEvent(Severity.INFO, "Provisioning resource."));

			try {
				final Map<String, String> instanceInformation = resourcePlugin.provision();
				instance.setInstanceInformation(instanceInformation);
				eventBus.publish(new CoreEvent(Severity.SUCCESS, "Resource provisioned."));
			}
			catch (ProvisionResourceException e) {
				fail("Could not provision resource: " + e.getMessage());
				stateMachine.fire(StateMachineEvents.FAILURE);
				return;
			}

			stateMachine.fire(StateMachineEvents.SUCCESS);
		}

		/**
		 * Create a connection to the resource using the communication plugin.
		 */
		protected void connect(final String from, final String to, final String fsmEvent) {

			eventBus.publish(new CoreEvent(Severity.INFO, "Connecting to resource."));

			try {
				final Connection connection = communicationPlugin.connect(instance.getInstanceInformation());
				instance.setConnection(connection);
				eventBus.publish(new CoreEvent(Severity.SUCCESS, "Connected to resource."));
			}
			catch (ConnectConnectionException e) {
				fail("Could not connect to resource: " + e.getMessage());
				stateMachine.fire(StateMachineEvents.FAILURE);
				return;
			}

			stateMachine.fire(StateMachineEvents.SUCCESS);
		}

		/**
		 * Provision the application described by the application plugin using the
		 * connection to the resource.
		 */
		protected void provisionApplication(final String from, final String to, final String fsmEvent) {

			eventBus.publish(new CoreEvent(Severity.INFO, "Provisioning application."));

			try {
				applicationPlugin.provision(instance.getConnection());
				eventBus.publish(new CoreEvent(Severity.SUCCESS, "Application provisioned."));
			}
			catch (ProvisionApplicationException e) {
				fail("Could not provision application: " + e.getMessage());
				stateMachine.fire(StateMachineEvents.FAILURE);
				return;
			}

			stateMachine.fire(StateMachineEvents.SUCCESS);
		}

		/**
		 * Start the just provisioned application using the application plugin.
		 */
		protected void startApplication(final String from, final String to, final String fsmEvent) {

			eventBus.publish(new CoreEvent(Severity.INFO, "Starting application."));

			try {
				url = applicationPlugin.start(instance.getConnection());
				instance.getInstanceInformation().put("appURL", url.toString());
				eventBus.publish(new CoreEvent(Severity.SUCCESS, "Application started."));
			}
			catch (StartApplicationException e) {
				fail("Could not start application: " + e.getMessage());
				stateMachine.fire(StateMachineEvents.FAILURE);
				return;
			}

			stateMachine.fire(StateMachineEvents.SUCCESS);
		}

		/**
		 * Stop the running application using the application plugin
		 */
		protected void stopApplication(final String from, final String to, final String fsmEvent) {

			eventBus.publish(new CoreEvent(Severity.INFO, "Stopping application."));

			try {
				applicationPlugin.stop(instance.getConnection());
				eventBus.publish(new CoreEvent(Severity.SUCCESS, "Application stopped."));
			}
			catch (StopApplicationException e) {
				eventBus.publish(new CoreEvent(Severity.WARNING, "Could not stop application: " + e.getMessage()));
				stateMachine.fire(StateMachineEvents.FAILURE);
				return;
			}

			stateMachine.fire(StateMachineEvents.SUCCESS);
		}

		/**
		 * Deprovision the application using the application plugin
		 */
		protected void deprovisionApplication(final String from, final String to, final String fsmEvent) {

			eventBus.publish(new CoreEvent(Severity.INFO, "Deprovisioning application."));

			try {
				applicationPlugin.deprovision(instance.getConnection());
				eventBus.publish(new CoreEvent(Severity.SUCCESS, "Application deprovisioned."));
			}
			catch (DeprovisionApplicationException e) {
				eventBus.publish(new CoreEvent(Severity.WARNING, "Could not deprovision application: " + e.getMessage()));
				stateMachine.fire(StateMachineEvents.FAILURE);
				return;
			}

			stateMachine.fire(StateMachineEvents.SUCCESS);
		}

		/**
		 * Disconnect the connection created by the communication plugin.
		 */
		protected void disconnect(final String from, final String to, final String fsmEvent) {

			eventBus.publish(new CoreEvent(Severity.INFO, "Disconnecting from resource."));

			try {
				communicationPlugin.disconnect(instance.getConnection());
				eventBus.publish(new CoreEvent(Severity.SUCCESS, "Disconnected from resource."));
			}
			catch (DisconnectConnectionException e) {
				eventBus.publish(new CoreEvent(Severity.WARNING, "Could not disconnect from resource: " + e.getMessage()));
				stateMachine.fire(StateMachineEvents.FAILURE);
				return;
			}

			stateMachine.fire(StateMachineEvents.SUCCESS);
		}

		/**
		 * Deprovision the resource using the resource plugin.
		 */
		protected void deprovisionResource(final String from, final String to, final String fsmEvent) {

			eventBus.publish(new CoreEvent(Severity.INFO, "Deprovisioning resource."));

			try {
				if (instance.getInstanceInformation() != null) {
					resourcePlugin.deprovision(instance.getInstanceInformation());
				}
				// how to handle failure?
				eventBus.publish(new CoreEvent(Severity.SUCCESS, "Resource deprovisioned."));
			}
			catch (DeprovisionResourceException e) {
				fail("Could not deprovision resource: " + e.getMessage());
				stateMachine.fire(StateMachineEvents.FAILURE);
				return;
			}

			stateMachine.fire(StateMachineEvents.SUCCESS);
		}

		/**
		 * Handle failure of resource deprovisioning.
		 * Manual intervention might be necessary at this point!
		 */
		protected void fatalError(final String from, final String to, final String fsmEvent) {
			final String warningMessage = "#################### WARNING ####################"
					+ "Deprovisioning of a resource has failed."
					+ "Manual intervention might be necessary to remove any remaining resources."
					+ "Check your resource providers management interface for any remaining resources and manually remove them."
					+ "#################################################";
			eventBus.publish(new CoreEvent(Severity.ERROR, warningMessage));
			stateMachine.fire(StateMachineEvents.SUCCESS);
			//stateMachine.fire(StateMachineEvents.FAILURE);
		}

		/**
		 * Unload all request plugins.
		 */
		protected void unloadRequestPlugins(final String from, final String to, final String fsmEvent) {

			eventBus.publish(new CoreEvent(Severity.INFO, "Unloading request plugins."));

			final RequestContext context = request.getRequestContext();

			try {
				resourcePlugin      = null;
				communicationPlugin = null;
				applicationPlugin   = null;
				pluginManager.unloadPlugin(PluginTypes.RESOURCE, context.getResourcePlugin());
				pluginManager.unloadPlugin(PluginTypes.COMMUNICATION, context.getCommunicationPlugin());
				pluginManager.unloadPlugin(PluginTypes.APPLICATION, context.getApplicationPlugin());
				eventBus.publish(new CoreEvent(Severity.SUCCESS, "Request plugins unloaded."));
			}
			catch (UnloadPluginException e) {
				fail("Could not unload request plugins: " + e.getMessage());
				stateMachine.fire(StateMachineEvents.FAILURE);
				return;
			}

			stateMachine.fire(StateMachineEvents.SUCCESS);
		}

		/**
		 * Unload all event plugins.
		 */
		protected void unloadEventPlugins(final String from, final String to, final String fsmEvent) {

			eventBus.publish(new CoreEvent(Severity.INFO, "Unloading event plugins."));

			try {
				pluginManager.unloadAllPlugins();
			}
			catch (UnloadPluginException e) {
				eventBus.publish(new CoreEvent(Severity.ERROR, "Could not unload event plugins: " + e.getMessage()));
				stateMachine.fire(StateMachineEvents.FAILURE);
				return;
			}

			stateMachine.fire(StateMachineEvents.SUCCESS);
		}

		/**
		 * Run clean up code.
		 */
		protected void cleanup(final String from, final String to, final String fsmEvent) {

			System.out.println("Stopping plugin manager");
			try {
				pluginManager.stop();
			}
			catch (UnloadPluginException e) {
				System.out.println("Stopping plugin manager failed: " + e.getMessage());
				stateMachine.fire(StateMachineEvents.FAILURE);
				return;
			}

			stateMachine.fire(StateMachineEvents.SUCCESS);
		}

		/**
		 * Stop the state machine
		 */
		protected void end(final String from, final String to, final String fsmEvent) {
			stateMachine.terminate();
			stopped = true;
		}

	}

}
