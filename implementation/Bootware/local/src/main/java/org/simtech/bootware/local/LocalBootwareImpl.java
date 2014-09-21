package org.simtech.bootware.local;

import java.net.URL;

import javax.jws.WebService;
import javax.xml.ws.WebServiceException;

import org.simtech.bootware.core.AbstractStateMachine;
import org.simtech.bootware.core.ApplicationInstance;
import org.simtech.bootware.core.ConfigurationListWrapper;
import org.simtech.bootware.core.InformationListWrapper;
import org.simtech.bootware.core.Request;
import org.simtech.bootware.core.UserContext;
import org.simtech.bootware.core.events.CoreEvent;
import org.simtech.bootware.core.events.RemoteBootwareStartedEvent;
import org.simtech.bootware.core.events.Severity;
import org.simtech.bootware.core.exceptions.DeployException;
import org.simtech.bootware.core.exceptions.SetConfigurationException;
import org.simtech.bootware.core.exceptions.ShutdownException;
import org.simtech.bootware.core.exceptions.UndeployException;

import org.squirrelframework.foundation.fsm.StateMachineBuilderFactory;

/**
 * The main local bootware program.
 * <p>
 * Implements a finite state machine using squirrelframework.
 * The whole bootware process is executed by this state machine.
 */
@WebService(endpointInterface = "org.simtech.bootware.local.LocalBootware")
public class LocalBootwareImpl extends AbstractStateMachine implements LocalBootware {

	private static Boolean triedProvisioningRemote = false;
	private static UserContext remoteContext;
	private static RemoteBootwareService remoteBootware;
	private static URL remoteBootwareURL;

	/**
	 * Creates the bootware process as state machine.
	 * <p>
	 * Creates the specific state graph of the local bootware and starts the state machine.
	 */
	@SuppressWarnings("checkstyle:multiplestringliterals")
	public LocalBootwareImpl() {
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
		builder.externalTransition().from(SMStates.LOAD_REQUEST_PLUGINS).to(SMStates.STOP_APPLICATION).on(SMEvents.UNDEPLOY);
		builder.externalTransition().from(SMStates.LOAD_REQUEST_PLUGINS).to(SMStates.UNLOAD_REQUEST_PLUGINS).on(SMEvents.FAILURE);

		// deploy
		buildDefaultTransition(SMStates.PROVISION_RESOURCE, "provisionResource", SMStates.CONNECT, SMStates.DEPROVISION_RESOURCE);
		buildDefaultTransition(SMStates.CONNECT, "connect", SMStates.PROVISION_APPLICATION, SMStates.DISCONNECT);
		buildDefaultTransition(SMStates.PROVISION_APPLICATION, "provisionApplication", SMStates.START_APPLICATION, SMStates.DEPROVISION_APPLICATION);
		buildDefaultTransition(SMStates.START_APPLICATION, "startApplication", SMStates.UNLOAD_REQUEST_PLUGINS, SMStates.STOP_APPLICATION);

		// undeploy
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
	 * Implements the isReady operation specified in @see org.simtech.bootware.core.Bootware
	 */
	@Override
	public final Boolean isReady() {

		logRequestStart("Received request: isReady");

		final String currentState = (String) stateMachine.getCurrentState();

		logRequestEnd("Finished processing request: isReady");

		return SMStates.WAIT.equals(currentState);
	}

	/**
	 * Implements the deploy operation specified in @see org.simtech.bootware.core.Bootware
	 */
	@Override
	public final InformationListWrapper deploy(final UserContext context) throws DeployException {

		logRequestStart("Received request: deploy");

		// Deploy remote bootware if not yet deployed
		if (remoteBootware == null || !remoteBootware.isAvailable()) {
			eventBus.publish(new CoreEvent(Severity.INFO, "Deploying remote bootware."));

			request  = new Request("deploy");
			instance = new ApplicationInstance("remote-bootware");

			// create temporary context for remote bootware request
			remoteContext = new UserContext();
			remoteContext.setResource(context.getResource());
			remoteContext.setApplication("remote-bootware");
			remoteContext.setConfigurationList(context.getConfigurationList());
			instance.setUserContext(remoteContext);

			// execute deploy request
			stateMachine.fire(SMEvents.REQUEST);

			// handle deploy request failure and success
			if (request.isFailing()) {
				throw new DeployException((String) request.getResponse());
			}
			else {
				instanceStore.put(remoteContext, instance);
			}

			// create remote bootware service
			try {
				eventBus.publish(new CoreEvent(Severity.INFO, "Connecting to remote bootware."));
				remoteBootware = new RemoteBootwareService(url);
				eventBus.publish(new RemoteBootwareStartedEvent(Severity.INFO, "Remote bootware started at " + url.toString() + ".", url.toString()));
			}
			catch (WebServiceException e) {
				remoteBootware = null;
				eventBus.publish(new CoreEvent(Severity.ERROR, "Connecting to remote bootware failed: " + e.getMessage()));
				throw new DeployException(e);
			}

			// Wait for remote bootware to be ready.
			eventBus.publish(new CoreEvent(Severity.INFO, "Wait for remote bootware to be ready."));
			final Integer max = 10;
			final Integer wait = 5000;
			for (Integer i = 0; i <= max; i++) {
				if (remoteBootware.isReady()) {
					break;
				}
				eventBus.publish(new CoreEvent(Severity.INFO, "Remote bootware is not ready yet."));
				try {
					Thread.sleep(wait);
				}
				catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
			eventBus.publish(new CoreEvent(Severity.INFO, "Remote bootware is ready."));

			// set default configuration at remote bootware
			try {
				eventBus.publish(new CoreEvent(Severity.INFO, "Passing on default configuration to remote Bootware."));
				final ConfigurationListWrapper wrapper = new ConfigurationListWrapper();
				wrapper.setConfigurationList(defaultConfigurationList);
				remoteBootware.setConfiguration(wrapper);
			}
			catch (SetConfigurationException e) {
				eventBus.publish(new CoreEvent(Severity.ERROR, "Setting default configuration at remote bootware failed: " + e.getMessage()));
				throw new DeployException(e);
			}
		}

		// pass on original request to remote bootware
		eventBus.publish(new CoreEvent(Severity.INFO, "Passing on deploy request to remote bootware."));
		final InformationListWrapper remoteResponse = remoteBootware.deploy(context);

		logRequestEnd("Finished processing request: deploy");

		return remoteResponse;
	}

	/**
	 * Implements the undeploy operation specified in @see org.simtech.bootware.core.Bootware
	 */
	@Override
	public final void undeploy(final UserContext context) throws UndeployException {

		logRequestStart("Received request: undeploy");

		// Set up request object and get instance from instance store.
		request = new Request("undeploy");
		instance = instanceStore.get(context);

		if (instance == null) {
			throw new UndeployException("There was no active application that matched this request");
		}

		// Start the undeploy process.
		stateMachine.fire(SMEvents.REQUEST);

		// Fail if undeploy process failed.
		if (request.isFailing()) {
			throw new UndeployException((String) request.getResponse());
		}
		else {
			instanceStore.remove(context);
		}

		logRequestEnd("Finished processing request: undeploy");
	}

	/**
	 * Implements the setConfiguration operation specified in @see org.simtech.bootware.core.Bootware
	 */
	@Override
	public final void setConfiguration(final ConfigurationListWrapper configurationListWrapper) throws SetConfigurationException {

		logRequestStart("Received request: setConfiguration");

		// Replace default configuration with the new configuration.
		eventBus.publish(new CoreEvent(Severity.INFO, "Setting default configuration."));
		defaultConfigurationList = configurationListWrapper.getConfigurationList();

		// Pass on new configuration to remote bootware if it exists.
		if (remoteBootware != null && remoteBootware.isAvailable()) {
			eventBus.publish(new CoreEvent(Severity.INFO, "Passing on default configuration to remote Bootware."));
			remoteBootware.setConfiguration(configurationListWrapper);
		}

		logRequestEnd("Finished processing request: setConfiguration");
	}

	/**
	 * Implements the shutdown operation specified in @see org.simtech.bootware.core.Bootware
	 */
	@Override
	public final void shutdown() throws ShutdownException {

		logRequestStart("Received request: shutdown");

		// pass on shutdown request to remote bootware
		eventBus.publish(new CoreEvent(Severity.INFO, "Passing on shutdown request to remote bootware."));
		if (remoteBootware != null && remoteBootware.isAvailable()) {
			remoteBootware.shutdown();
		}

		// undeploy remote bootware
		eventBus.publish(new CoreEvent(Severity.INFO, "Undeploy remote bootware."));
		if (remoteContext != null) {
			final ApplicationInstance remoteBootwareInstance = instanceStore.get(remoteContext);
			if (remoteBootwareInstance != null) {
				try {
					undeploy(remoteContext);
				}
				catch (UndeployException e) {
					throw new ShutdownException(e);
				}
			}
		}

		// trigger shutdown in thread after delay so that this method can return
		// before the local bootware is shut down.
		final Thread delayedShutdown = new Thread() {
			public void run() {
				try {
					final Integer time = 2000;
					Thread.sleep(time);
				}
				catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				stateMachine.fire(SMEvents.SHUTDOWN);
			}
		};

		logRequestEnd("Finished processing request: shutdown");

		eventBus.publish(new CoreEvent(Severity.INFO, "Shutting down."));
		delayedShutdown.start();
	}

	/**
	 * Describes the operations that are executed on state entry as defined by
	 * the transition above. The local bootware does not add any new operations
	 * to the default operation defined in @see org.simtech.bootware.core.AbstractStateMachine
	 * so this is empty on purpose.
	 */
	static class Machine extends AbstractMachine {

		public Machine() {}

	}

}
