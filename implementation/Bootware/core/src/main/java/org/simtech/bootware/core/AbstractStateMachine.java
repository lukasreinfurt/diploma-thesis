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
//import org.simtech.bootware.core.exceptions.ConfigurationException;
import org.simtech.bootware.core.exceptions.ConnectConnectionException;
import org.simtech.bootware.core.exceptions.ContextMappingException;
import org.simtech.bootware.core.exceptions.DeprovisionApplicationException;
import org.simtech.bootware.core.exceptions.DeprovisionResourceException;
import org.simtech.bootware.core.exceptions.DisconnectConnectionException;
import org.simtech.bootware.core.exceptions.InitializeException;
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

	protected static Boolean stopped = false;
	protected static Properties properties = new Properties();

	protected static EventBus eventBus;
	protected static PluginManager pluginManager;
	protected static InstanceStore instanceStore;

	protected static ResourcePlugin resourcePlugin;
	protected static CommunicationPlugin communicationPlugin;
	protected static ApplicationPlugin applicationPlugin;

	protected static Map<String, ConfigurationWrapper> defaultConfigurationList = new HashMap<String, ConfigurationWrapper>();
	protected static Map<String, ConfigurationWrapper> configurationList = new HashMap<String, ConfigurationWrapper>();

	protected static Request request;

	protected static UntypedStateMachine stateMachine;
	protected static URL url;

	protected static ApplicationInstance instance;

	private static String resourcePluginPath      = "plugins/resource/";
	private static String communicationPluginPath = "plugins/communication/";
	private static String applicationPluginPath   = "plugins/application/";
	private static String eventPluginsPath        = "plugins/event/";

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

	public final Boolean hasStopped() {
		return stopped;
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
	@SuppressWarnings({
			"checkstyle:designforextension",
			"checkstyle:classfanoutcomplexity"
			})
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
			try {
				final InputStream propFile = new FileInputStream("config.properties");
				properties.load(propFile);
			}
			catch (IOException e) {
				System.out.println("Properties file could not be loaded: " + e.getMessage());
				stateMachine.fire(StateMachineEvents.FAILURE);
			}

			instanceStore = new InstanceStore();
			eventBus      = new EventBus();
			pluginManager = new PluginManager();
			pluginManager.registerSharedObject(eventBus);
			pluginManager.registerSharedObject(configurationList);

			stateMachine.fire(StateMachineEvents.SUCCESS);
		}

		protected void loadEventPlugins(final String from, final String to, final String fsmEvent) {
			// log to local file?
			try {
				final String[] eventPlugins = properties.getProperty("eventPlugins").split(";");

				for (String eventPlugin : eventPlugins) {
					final EventPlugin plugin = pluginManager.loadPlugin(EventPlugin.class, eventPluginsPath + eventPlugin);
					plugin.initialize(configurationList);
				}

				eventBus.publish(new CoreEvent(Severity.SUCCESS, "Loading event plugins succeeded."));
			}
			catch (LoadPluginException e) {
				// log to local file?
				e.printStackTrace();
				stateMachine.fire(StateMachineEvents.FAILURE);
			}
			catch (InitializeException e) {
				e.printStackTrace();
				stateMachine.fire(StateMachineEvents.FAILURE);
			}

			stateMachine.fire(StateMachineEvents.SUCCESS);
		}

		protected void wait(final String from, final String to, final String fsmEvent) {
			// No op. State machine events are handled by web service operations in
			// local and remote bootware implementation.
		}

		private Map<String, ConfigurationWrapper> mergeConfigurationLists(final Map<String, ConfigurationWrapper>... configurationLists) {

			final Map<String, ConfigurationWrapper> mergedMap = new HashMap<String, ConfigurationWrapper>();

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

			for (Map<String, ConfigurationWrapper> configurationList : configurationLists) {
				for (Map.Entry<String, ConfigurationWrapper> entry : configurationList.entrySet()) {
					if (mergedMap.containsKey(entry.getKey())) {
						final Map<String, String> configuration = mergedMap.get(entry.getKey()).getConfiguration();
						final Map<String, String> newConfiguration = entry.getValue().getConfiguration();
						configuration.putAll(newConfiguration);
						final ConfigurationWrapper configurationWrapper = new ConfigurationWrapper();
						configurationWrapper.setConfiguration(configuration);
						mergedMap.put(entry.getKey(), configurationWrapper);
					}
					else {
						mergedMap.put(entry.getKey(), entry.getValue());
					}
				}
			}

			System.out.println(">>>> Output map:");
			for (Map.Entry<String, ConfigurationWrapper> entry : mergedMap.entrySet()) {
				System.out.println(entry.getKey());
				final Map<String, String> configuration = entry.getValue().getConfiguration();
				for (Map.Entry<String, String> entry2 : configuration.entrySet()) {
					System.out.println("    " + entry2.getKey() + ": " + entry2.getValue());
				}
			}

			return mergedMap;
		}

		protected void readContext(final String from, final String to, final String fsmEvent) {
			eventBus.publish(new CoreEvent(Severity.INFO, "Generating context."));

			try {
				// map user context to request context
				final UserContext userContext = instance.getUserContext();
				final ContextMapper mapper = new ContextMapper();
				final RequestContext requestContext = mapper.map(userContext);

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
				final String failureMessage = "Could not map userContext to requestContext: " + e.getMessage();
				request.fail(failureMessage);
				eventBus.publish(new CoreEvent(Severity.ERROR, failureMessage));
				stateMachine.fire(StateMachineEvents.FAILURE);
			}

			// handle failure
			// set configuration

			stateMachine.fire(StateMachineEvents.SUCCESS);
		}

		protected void loadRequestPlugins(final String from, final String to, final String fsmEvent) {
			eventBus.publish(new CoreEvent(Severity.INFO, "Loading request plugins."));

			final RequestContext context = request.getRequestContext();

			try {
				resourcePlugin      = pluginManager.loadPlugin(ResourcePlugin.class, resourcePluginPath + context.getResourcePlugin());
				resourcePlugin.initialize(configurationList);
				communicationPlugin = pluginManager.loadPlugin(CommunicationPlugin.class, communicationPluginPath + context.getCommunicationPlugin());
				communicationPlugin.initialize(configurationList);
				applicationPlugin   = pluginManager.loadPlugin(ApplicationPlugin.class, applicationPluginPath + context.getApplicationPlugin());
				applicationPlugin.initialize(configurationList);
				eventBus.publish(new CoreEvent(Severity.SUCCESS, "Request plugins loaded."));
			}
			catch (LoadPluginException e) {
				eventBus.publish(new CoreEvent(Severity.ERROR, "Could not load request plugins: " + e.getMessage()));
				stateMachine.fire(StateMachineEvents.FAILURE);
			}
			catch (InitializeException e) {
				eventBus.publish(new CoreEvent(Severity.ERROR, "Could not initialize request plugins: " + e.getMessage()));
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
				final Map<String, String> instanceInformation = resourcePlugin.provision();
				instance.setInstanceInformation(instanceInformation);
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
				final Connection connection = communicationPlugin.connect(instance.getInstanceInformation());
				instance.setConnection(connection);
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
				applicationPlugin.provision(instance.getConnection());
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
				url = applicationPlugin.start(instance.getConnection());
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
				applicationPlugin.stop(instance.getConnection());
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
				applicationPlugin.deprovision(instance.getConnection());
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
				communicationPlugin.disconnect(instance.getConnection());
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
				if (instance.getInstanceInformation() != null) {
					resourcePlugin.deprovision(instance.getInstanceInformation());
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

			final RequestContext context = request.getRequestContext();

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
			stopped = true;
		}

	}

}
