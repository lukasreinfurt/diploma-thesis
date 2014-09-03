package org.simtech.bootware.remote;

import java.util.Map;

import javax.jws.WebService;

import org.simtech.bootware.core.AbstractStateMachine;
import org.simtech.bootware.core.ApplicationInstance;
import org.simtech.bootware.core.ConfigurationListWrapper;
import org.simtech.bootware.core.InformationListWrapper;
import org.simtech.bootware.core.Request;
import org.simtech.bootware.core.RequestContext;
import org.simtech.bootware.core.UserContext;
import org.simtech.bootware.core.events.CoreEvent;
import org.simtech.bootware.core.events.Severity;
import org.simtech.bootware.core.exceptions.DeployException;
import org.simtech.bootware.core.exceptions.DeprovisionException;
import org.simtech.bootware.core.exceptions.InitializeException;
import org.simtech.bootware.core.exceptions.LoadPluginException;
import org.simtech.bootware.core.exceptions.ProvisionException;
import org.simtech.bootware.core.exceptions.SetConfigurationException;
import org.simtech.bootware.core.exceptions.ShutdownException;
import org.simtech.bootware.core.exceptions.UndeployException;
import org.simtech.bootware.core.exceptions.UnloadPluginException;
import org.simtech.bootware.core.plugins.PluginTypes;
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
		builder.externalTransition().from(SMStates.START).to(SMStates.INITIALIZE).on(SMEvents.START);

		// initialize
		buildDefaultTransition(SMStates.INITIALIZE, "initialize", SMStates.LOAD_EVENT_PLUGINS, SMStates.CLEANUP);
		buildDefaultTransition(SMStates.LOAD_EVENT_PLUGINS, "loadEventPlugins", SMStates.WAIT, SMStates.UNLOAD_EVENT_PLUGINS);

		builder.onEntry(SMStates.WAIT).callMethod("wait");
		builder.externalTransition().from(SMStates.WAIT).to(SMStates.READ_CONTEXT).on(SMEvents.REQUEST);
		builder.externalTransition().from(SMStates.WAIT).to(SMStates.UNLOAD_EVENT_PLUGINS).on(SMEvents.SHUTDOWN);
		builder.externalTransition().from(SMStates.WAIT).to(SMStates.UNLOAD_EVENT_PLUGINS).on(SMEvents.FAILURE);

		buildDefaultTransition(SMStates.READ_CONTEXT, "readContext", SMStates.LOAD_REQUEST_PLUGINS, SMStates.WAIT);

		builder.onEntry(SMStates.LOAD_REQUEST_PLUGINS).callMethod("loadRequestPlugins");
		builder.externalTransition().from(SMStates.LOAD_REQUEST_PLUGINS).to(SMStates.PROVISION_RESOURCE).on(SMEvents.DEPLOY);
		builder.externalTransition().from(SMStates.LOAD_REQUEST_PLUGINS).to(SMStates.DEPROVISION_MIDDLEWARE).on(SMEvents.UNDEPLOY);
		builder.externalTransition().from(SMStates.LOAD_REQUEST_PLUGINS).to(SMStates.UNLOAD_REQUEST_PLUGINS).on(SMEvents.FAILURE);

		// deploy
		buildDefaultTransition(SMStates.PROVISION_RESOURCE, "provisionResource", SMStates.CONNECT, SMStates.DEPROVISION_RESOURCE);
		buildDefaultTransition(SMStates.CONNECT, "connect", SMStates.PROVISION_APPLICATION, SMStates.DISCONNECT);
		buildDefaultTransition(SMStates.PROVISION_APPLICATION, "provisionApplication", SMStates.START_APPLICATION, SMStates.DEPROVISION_APPLICATION);
		buildDefaultTransition(SMStates.START_APPLICATION, "startApplication", SMStates.PROVISION_MIDDLEWARE, SMStates.STOP_APPLICATION);
		buildDefaultTransition(SMStates.PROVISION_MIDDLEWARE, "provisionMiddleware", SMStates.UNLOAD_REQUEST_PLUGINS, SMStates.DEPROVISION_MIDDLEWARE);

		// undeploy
		buildDefaultTransition(SMStates.DEPROVISION_MIDDLEWARE, "deprovisionMiddleware", SMStates.STOP_APPLICATION, SMStates.STOP_APPLICATION);
		buildDefaultTransition(SMStates.STOP_APPLICATION, "stopApplication", SMStates.DEPROVISION_APPLICATION, SMStates.DEPROVISION_APPLICATION);
		buildDefaultTransition(SMStates.DEPROVISION_APPLICATION, "deprovisionApplication", SMStates.DISCONNECT, SMStates.DISCONNECT);
		buildDefaultTransition(SMStates.DISCONNECT, "disconnect", SMStates.DEPROVISION_RESOURCE, SMStates.DEPROVISION_RESOURCE);
		buildDefaultTransition(SMStates.DEPROVISION_RESOURCE, "deprovisionResource", SMStates.UNLOAD_REQUEST_PLUGINS, SMStates.FATAL_ERROR);
		buildDefaultTransition(SMStates.FATAL_ERROR, "fatalError", SMStates.UNLOAD_REQUEST_PLUGINS, SMStates.UNLOAD_REQUEST_PLUGINS);

		// cleanup
		buildDefaultTransition(SMStates.UNLOAD_REQUEST_PLUGINS, "unloadRequestPlugins", SMStates.WAIT, SMStates.WAIT);
		buildDefaultTransition(SMStates.UNLOAD_EVENT_PLUGINS, "unloadEventPlugins", SMStates.CLEANUP, SMStates.CLEANUP);
		buildDefaultTransition(SMStates.CLEANUP, "cleanup", SMStates.END, SMStates.END);

		// end
		builder.onEntry(SMStates.END).callMethod("end");

		stateMachine = builder.newStateMachine(SMEvents.START);
	}

	/**
	 * Implements the deploy operation specified in @see org.simtech.bootware.core.Bootware
	 */
	@Override
	public final InformationListWrapper deploy(final UserContext context) throws DeployException {

		logRequestStart("Received request: deploy");

		final InformationListWrapper informationList = new InformationListWrapper();

		// Return information if instance already exists.
		if (instanceStore.get(context) != null) {
			eventBus.publish(new CoreEvent(Severity.INFO, "Returning information of already active instance."));
			informationList.setInformationList(instanceStore.get(context).getInstanceInformation());
			return informationList;
		}

		request = new Request("deploy");
		instance = new ApplicationInstance("temp");
		instance.setUserContext(context);

		stateMachine.fire(SMEvents.REQUEST);

		if (request.isFailing()) {
			throw new DeployException((String) request.getResponse());
		}
		else {
			instanceStore.put(context, instance);
		}

		logRequestEnd("Finished processing request: deploy");

		informationList.setInformationList(instance.getInstanceInformation());
		return informationList;
	}

	/**
	 * Implements the undeploy operation specified in @see org.simtech.bootware.core.Bootware
	 */
	@Override
	public final void undeploy(final UserContext context) throws UndeployException {

		logRequestStart("Received request: undeploy");

		request = new Request("undeploy");
		instance = instanceStore.get(context);

		if (instance == null) {
			throw new UndeployException("There was no active application that matched this request");
		}

		stateMachine.fire(SMEvents.REQUEST);

		if (request.isFailing()) {
			throw new UndeployException((String) request.getResponse());
		}
		else {
			instanceStore.remove(context);
		}

		logRequestEnd("Finished processing request: undeploy");
	}

	/**
	 * Implements the getActive operation specified in @see org.simtech.bootware.remote.RemoteBootware
	 */
	@Override
	public final InformationListWrapper getActive(final UserContext context) {

		logRequestStart("Received request: getActive");

		final InformationListWrapper informationList = new InformationListWrapper();
		if (instanceStore.get(context) != null) {
			informationList.setInformationList(instanceStore.get(context).getInstanceInformation());
		}

		logRequestEnd("Finished processing request: getActive");

		return informationList;
	}

	/**
	 * Implements the setConfiguration operation specified in @see org.simtech.bootware.core.Bootware
	 */
	@Override
	public final void setConfiguration(final ConfigurationListWrapper configurationListWrapper) throws SetConfigurationException {

		logRequestStart("Received request: setConfiguration");

		defaultConfigurationList = configurationListWrapper.getConfigurationList();

		logRequestEnd("Finished processing request: setConfiguration");
	}

	/**
	 * Implements the shutdown operation specified in @see org.simtech.bootware.core.Bootware
	 */
	@Override
	public final void shutdown() throws ShutdownException {

		logRequestStart("Received request: shutdown");

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
				stateMachine.fire(SMEvents.SHUTDOWN);
			}
		};

		eventBus.publish(new CoreEvent(Severity.INFO, "Shutting down."));
		delayedShutdown.start();

		logRequestEnd("Finished processing request: shutdown");
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
					ProvisionPlugin provisionPlugin = pluginManager.loadPlugin(ProvisionPlugin.class, PluginTypes.PROVISION, context.getCallApplicationPlugin());
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
					pluginManager.unloadPlugin(PluginTypes.PROVISION, context.getCallApplicationPlugin());
				}
				catch (LoadPluginException e) {
					fail("Could not load provision plugins: " + e.getMessage());
					stateMachine.fire(SMEvents.FAILURE);
					return;
				}
				catch (UnloadPluginException e) {
					fail("Could not unload provision plugins: " + e.getMessage());
					stateMachine.fire(SMEvents.FAILURE);
					return;
				}
				catch (InitializeException e) {
					fail("Could not initialize provisision plugins: " + e.getMessage());
					stateMachine.fire(SMEvents.FAILURE);
					return;
				}
				catch (ProvisionException e) {
					fail("Provisioning with provision plugin failed: " + e.getMessage());
					stateMachine.fire(SMEvents.FAILURE);
					return;
				}
			}
			else {
				eventBus.publish(new CoreEvent(Severity.INFO, "No service package reference provided. Skipping provision middleware step."));
			}

			stateMachine.fire(SMEvents.SUCCESS);
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
					ProvisionPlugin provisionPlugin = pluginManager.loadPlugin(ProvisionPlugin.class, PluginTypes.PROVISION, context.getCallApplicationPlugin());
					provisionPlugin.initialize(configurationList);

					// Call provisioning engine
					eventBus.publish(new CoreEvent(Severity.INFO, "Call provision engine."));
					provisionPlugin.deprovision(url.toString(), servicePackageReference);

					// Unload provision plugin
					eventBus.publish(new CoreEvent(Severity.SUCCESS, "Undload provision plugin."));
					provisionPlugin = null;
					pluginManager.unloadPlugin(PluginTypes.PROVISION, context.getCallApplicationPlugin());
				}
				catch (LoadPluginException e) {
					fail("Could not load provision plugin: " + e.getMessage());
					stateMachine.fire(SMEvents.FAILURE);
					return;
				}
				catch (UnloadPluginException e) {
					fail("Could not unload provision plugin: " + e.getMessage());
					stateMachine.fire(SMEvents.FAILURE);
					return;
				}
				catch (InitializeException e) {
					fail("Could not initialize provision plugin: " + e.getMessage());
					stateMachine.fire(SMEvents.FAILURE);
					return;
				}
				catch (DeprovisionException e) {
					fail("Deprovisioning with provision plugin failed: " + e.getMessage());
					stateMachine.fire(SMEvents.FAILURE);
					return;
				}
			}
			else {
				eventBus.publish(new CoreEvent(Severity.INFO, "No service package reference provided. Skipping deprovision middleware step."));
			}

			stateMachine.fire(SMEvents.SUCCESS);
		}

	}

}
