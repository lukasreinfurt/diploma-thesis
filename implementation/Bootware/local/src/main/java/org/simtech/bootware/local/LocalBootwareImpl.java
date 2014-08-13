package org.simtech.bootware.local;

import java.net.URL;
import java.util.HashMap;

import javax.jws.WebService;
import javax.xml.ws.WebServiceException;

import org.simtech.bootware.core.AbstractStateMachine;
import org.simtech.bootware.core.ApplicationInstance;
import org.simtech.bootware.core.ConfigurationWrapper;
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
 * The main bootware program.
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
	 */
	@SuppressWarnings("checkstyle:multiplestringliterals")
	public LocalBootwareImpl() {
		builder = StateMachineBuilderFactory.create(Machine.class);

		builder.transit().fromAny().toAny().onAny().callMethod("transition");

		// start
		builder.externalTransition().from("Start").to("Initialize").on(SMEvents.START);

		// initialize
		buildDefaultTransition("Initialize", "initialize", "Load_Event_Plugins", "Cleanup");
		buildDefaultTransition("Load_Event_Plugins", "loadEventPlugins", "Wait", "Unload_Event_Plugins");

		builder.onEntry("Wait").callMethod("wait");
		builder.externalTransition().from("Wait").to("Read_Context").on(SMEvents.REQUEST);
		builder.externalTransition().from("Wait").to("Unload_Event_Plugins").on(SMEvents.SHUTDOWN);
		builder.externalTransition().from("Wait").to("Unload_Event_Plugins").on(SMEvents.FAILURE);

		buildDefaultTransition("Read_Context", "readContext", "Load_Request_Plugins", "Return_Response");

		builder.onEntry("Load_Request_Plugins").callMethod("loadRequestPlugins");
		builder.externalTransition().from("Load_Request_Plugins").to("Provision_Resource").on(SMEvents.DEPLOY);
		builder.externalTransition().from("Load_Request_Plugins").to("Stop_Application").on(SMEvents.UNDEPLOY);
		builder.externalTransition().from("Load_Request_Plugins").to("Unload_Request_Plugins").on(SMEvents.FAILURE);

		// deploy
		buildDefaultTransition("Provision_Resource", "provisionResource", "Connect", "Deprovision_Resource");
		buildDefaultTransition("Connect", "connect", "Provision_Application", "Disconnect");
		buildDefaultTransition("Provision_Application", "provisionApplication", "Start_Application", "Deprovision_Application");
		buildDefaultTransition("Start_Application", "startApplication", "Unload_Request_Plugins", "Stop_Application");

		// undeploy
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

		stateMachine = builder.newStateMachine(SMEvents.START);
	}

	@Override
	public final InformationListWrapper deploy(final UserContext context) throws DeployException {
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
				instanceStore.put(context, instance);
			}

			// create remote bootware service
			try {
				eventBus.publish(new CoreEvent(Severity.INFO, "Connecting to remote bootware."));
				remoteBootware = new RemoteBootwareService(url);
				eventBus.publish(new RemoteBootwareStartedEvent(Severity.INFO, "Remote bootware started at " + url.toString() + ".", url.toString()));
			}
			catch (WebServiceException e) {
				eventBus.publish(new CoreEvent(Severity.ERROR, "Connecting to remote bootware failed: " + e.getMessage()));
				throw new DeployException(e);
			}
		}

		// pass on original request to remote bootware
		eventBus.publish(new CoreEvent(Severity.INFO, "Passing on deploy request to remote bootware."));
		return remoteBootware.deploy(context);
	}

	@Override
	public final void undeploy(final UserContext context) throws UndeployException {
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
	}

	@Override
	public final void setConfiguration(final HashMap<String, ConfigurationWrapper> configurationList) throws SetConfigurationException {
		defaultConfigurationList = configurationList;
	}

	@Override
	public final void shutdown() throws ShutdownException {
		// pass on shutdown request to remote bootware
		eventBus.publish(new CoreEvent(Severity.INFO, "Passing on shutdown request to remote bootware."));
		if (remoteBootware != null && remoteBootware.isAvailable()) {
			remoteBootware.shutdown();
		}

		// undeploy remote bootware
		eventBus.publish(new CoreEvent(Severity.INFO, "Undeploy remote bootware."));
		final ApplicationInstance remoteBootwareInstance = instanceStore.get(remoteContext);
		if (remoteBootwareInstance != null) {
			try {
				undeploy(remoteContext);
			}
			catch (UndeployException e) {
				throw new ShutdownException(e);
			}
		}

		// trigger shutdown in thread after delay so that this method can return before it
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

		eventBus.publish(new CoreEvent(Severity.INFO, "Shutting down."));
		delayedShutdown.start();
	}

	/**
	 * Describes the entryMethods for the bootware process.
	 */
	static class Machine extends AbstractMachine {

		public Machine() {}

	}

}
