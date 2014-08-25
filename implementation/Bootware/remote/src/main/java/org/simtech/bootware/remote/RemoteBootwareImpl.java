package org.simtech.bootware.remote;

import java.util.Map;

import javax.jws.WebService;

import org.simtech.bootware.core.AbstractStateMachine;
import org.simtech.bootware.core.ApplicationInstance;
import org.simtech.bootware.core.ConfigurationListWrapper;
import org.simtech.bootware.core.InformationListWrapper;
import org.simtech.bootware.core.Request;
import org.simtech.bootware.core.RequestContext;
import org.simtech.bootware.core.StateMachineEvents;
import org.simtech.bootware.core.UserContext;
import org.simtech.bootware.core.events.CoreEvent;
import org.simtech.bootware.core.events.Severity;
import org.simtech.bootware.core.exceptions.DeployException;
import org.simtech.bootware.core.exceptions.InitializeException;
import org.simtech.bootware.core.exceptions.LoadPluginException;
import org.simtech.bootware.core.exceptions.SetConfigurationException;
import org.simtech.bootware.core.exceptions.ShutdownException;
import org.simtech.bootware.core.exceptions.UndeployException;
import org.simtech.bootware.core.exceptions.UnloadPluginException;
import org.simtech.bootware.core.plugins.ProvisionPlugin;

import org.squirrelframework.foundation.fsm.StateMachineBuilderFactory;

/**
 * The main remote bootware program.
 * <p>
 * Implements a finite state machine using squirrelframework.
 * The whole bootware process is executed by this state machine.
 */
@WebService(endpointInterface = "org.simtech.bootware.remote.RemoteBootware")
public class RemoteBootwareImpl extends AbstractStateMachine implements RemoteBootware {

	private static String provisionPluginPath = "plugins/provision/";

	/**
	 * Creates the bootware process as state machine.
	 * <p>
	 * Creates the specific state graph of the local bootware and starts the state machine.
	 */
	@SuppressWarnings("checkstyle:multiplestringliterals")
	public RemoteBootwareImpl() {
		builder = StateMachineBuilderFactory.create(Machine.class);

		// Call transition() on any transition.
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

		buildDefaultTransition("Read_Context", "readContext", "Load_Request_Plugins", "Wait");

		builder.onEntry("Load_Request_Plugins").callMethod("loadRequestPlugins");
		builder.externalTransition().from("Load_Request_Plugins").to("Provision_Resource").on(StateMachineEvents.DEPLOY);
		builder.externalTransition().from("Load_Request_Plugins").to("Deprovision_Middleware").on(StateMachineEvents.UNDEPLOY);
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
		buildDefaultTransition("Unload_Request_Plugins", "unloadRequestPlugins", "Wait", "Wait");
		buildDefaultTransition("Unload_Event_Plugins", "unloadEventPlugins", "Cleanup", "Cleanup");
		buildDefaultTransition("Cleanup", "cleanup", "End", "End");

		// end
		builder.onEntry("End").callMethod("end");

		stateMachine = builder.newStateMachine(StateMachineEvents.START);
	}

	/**
	 * Implements the deploy operation specified in @see org.simtech.bootware.core.Bootware
	 */
	@Override
	public final InformationListWrapper deploy(final UserContext context) throws DeployException {
		request = new Request("deploy");
		instance = new ApplicationInstance("temp");
		instance.setUserContext(context);

		stateMachine.fire(StateMachineEvents.REQUEST);

		if (request.isFailing()) {
			throw new DeployException((String) request.getResponse());
		}
		else {
			instanceStore.put(context, instance);
		}

		final InformationListWrapper informationList = new InformationListWrapper();
		informationList.setInformationList(instance.getInstanceInformation());
		return informationList;
	}

	/**
	 * Implements the undeploy operation specified in @see org.simtech.bootware.core.Bootware
	 */
	@Override
	public final void undeploy(final UserContext context) throws UndeployException {
		request = new Request("undeploy");
		instance = instanceStore.get(context);

		if (instance == null) {
			throw new UndeployException("There was no active application that matched this request");
		}

		stateMachine.fire(StateMachineEvents.REQUEST);

		if (request.isFailing()) {
			throw new UndeployException((String) request.getResponse());
		}
		else {
			instanceStore.remove(context);
		}
	}

	/**
	 * Implements the getActive operation specified in @see org.simtech.bootware.remote.RemoteBootware
	 */
	@Override
	public final InformationListWrapper getActive(final UserContext context) {
		final InformationListWrapper informationList = new InformationListWrapper();
		if (instanceStore.get(context) != null) {
			informationList.setInformationList(instanceStore.get(context).getInstanceInformation());
		}
		return informationList;
	}

	/**
	 * Implements the setConfiguration operation specified in @see org.simtech.bootware.core.Bootware
	 */
	@Override
	public final void setConfiguration(final ConfigurationListWrapper configurationListWrapper) throws SetConfigurationException {
		eventBus.publish(new CoreEvent(Severity.INFO, "Setting default configuration."));
		defaultConfigurationList = configurationListWrapper.getConfigurationList();
	}

	/**
	 * Implements the shutdown operation specified in @see org.simtech.bootware.core.Bootware
	 */
	@Override
	public final void shutdown() throws ShutdownException {
		// undeploy workflow middleware
		eventBus.publish(new CoreEvent(Severity.INFO, "Deprovision Workflow Middleware"));

		// undeploy all provisioning engines
		eventBus.publish(new CoreEvent(Severity.INFO, "Undeploy active applications."));
		final ApplicationInstance[] activeApplications = instanceStore.getAll();
		for (ApplicationInstance activeApplication : activeApplications) {
			try {
				undeploy(activeApplication.getUserContext());
			}
			catch (UndeployException e) {
				throw new ShutdownException(e);
			}
		}

		// trigger shutdown in thread after delay so that this method can return
		// before the remote bootware is shut down.
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
	 * Describes the operations that are executed on state entry as defined by
	 * the transition above. The remote bootware does add the provisionMiddleware
	 * and deprovisionMiddleware operations to the default operation defined in
	 * @see org.simtech.bootware.core.AbstractStateMachine
	 */
	static class Machine extends AbstractMachine {

		public Machine() {}

		/**
		 * Provisions the workflow middleware by calling the provisioning engine
		 * with the service package reference.
		 */
		protected void provisionMiddleware(final String from, final String to, final String fsmEvent) {

			final RequestContext context = request.getRequestContext();
			final String servicePackageReference = context.getServicePackageReference();

			// Only call provisioning engine if service package reference is provided
			if (servicePackageReference != null && !"".equals(servicePackageReference)) {
				try {
					// Load and initialize provision plugin
					eventBus.publish(new CoreEvent(Severity.SUCCESS, "Load provision plugin."));
					ProvisionPlugin provisionPlugin = pluginManager.loadPlugin(ProvisionPlugin.class, provisionPluginPath + context.getCallApplicationPlugin());
					provisionPlugin.initialize(configurationList);

					// Call provisioning engine
					eventBus.publish(new CoreEvent(Severity.INFO, "Call provisioning engine."));
					final Map<String, String> informationList = provisionPlugin.provision(url.toString(), servicePackageReference);

					// Combine response with the instance information returned earlier
					// by the provision resource step.
					final Map<String, String> instanceInformation = instance.getInstanceInformation();
					instanceInformation.putAll(informationList);

					// Unload provision plugin
					eventBus.publish(new CoreEvent(Severity.SUCCESS, "Unload provision plugin"));
					provisionPlugin = null;
					pluginManager.unloadPlugin(provisionPluginPath + context.getCallApplicationPlugin());
				}
				catch (LoadPluginException e) {
					fail("Could not load provision plugins: " + e.getMessage());
					stateMachine.fire(StateMachineEvents.FAILURE);
					return;
				}
				catch (UnloadPluginException e) {
					fail("Could not unload provision plugins: " + e.getMessage());
					stateMachine.fire(StateMachineEvents.FAILURE);
					return;
				}
				catch (InitializeException e) {
					fail("Could not initialize provisision plugins: " + e.getMessage());
					stateMachine.fire(StateMachineEvents.FAILURE);
					return;
				}
			}
			else {
				eventBus.publish(new CoreEvent(Severity.INFO, "No service package reference provided. Skipping provision middleware step."));
			}

			stateMachine.fire(StateMachineEvents.SUCCESS);
		}

		/**
		 * Provisions the workflow middleware by calling the provisioning engine
		 * with the service package reference.
		 */
		protected void deprovisionMiddleware(final String from, final String to, final String fsmEvent) {

			final RequestContext context = request.getRequestContext();
			final String servicePackageReference = context.getServicePackageReference();

			// Only call provisioning engine if service package reference is provided
			if (servicePackageReference != null && !"".equals(servicePackageReference)) {
				try {
					// Load and initialize provision plugin
					eventBus.publish(new CoreEvent(Severity.SUCCESS, "Load provision plugin."));
					ProvisionPlugin provisionPlugin = pluginManager.loadPlugin(ProvisionPlugin.class, provisionPluginPath + context.getCallApplicationPlugin());
					provisionPlugin.initialize(configurationList);

					// Call provisioning engine
					eventBus.publish(new CoreEvent(Severity.INFO, "Call provision engine."));
					provisionPlugin.deprovision(url.toString(), servicePackageReference);

					// Unload provision plugin
					eventBus.publish(new CoreEvent(Severity.SUCCESS, "Undload provision plugin."));
					provisionPlugin = null;
					pluginManager.unloadPlugin(provisionPluginPath + context.getCallApplicationPlugin());
				}
				catch (LoadPluginException e) {
					fail("Could not load provision plugins: " + e.getMessage());
					stateMachine.fire(StateMachineEvents.FAILURE);
					return;
				}
				catch (UnloadPluginException e) {
					fail("Could not unload provision plugins: " + e.getMessage());
					stateMachine.fire(StateMachineEvents.FAILURE);
					return;
				}
				catch (InitializeException e) {
					fail("Could not initialize provision plugins: " + e.getMessage());
					stateMachine.fire(StateMachineEvents.FAILURE);
					return;
				}
			}
			else {
				eventBus.publish(new CoreEvent(Severity.INFO, "No service package reference provided. Skipping deprovision middleware step."));
			}

			stateMachine.fire(StateMachineEvents.SUCCESS);
		}

	}

}
