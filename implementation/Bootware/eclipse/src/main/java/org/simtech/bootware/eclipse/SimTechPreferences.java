package org.simtech.bootware.eclipse;

import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.configuration.MapConfiguration;

import org.eclipse.core.commands.common.CommandException;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import fragmentorcp.FragmentoPlugIn;
import fragmentorcp.views.treeviewer.provider.TodoMockModel;
import fragmentorcp.views.treeviewer.provider.TreeViewerOperator;
import fragmentorcppresenter.presenter.Presenter;

/**
 * A utility class that handles updating the SimTech preferences.
 */
public final class SimTechPreferences {

	private static MessageConsoleStream out;

	// Get the bootware console.
	static {
		final MessageConsole console = Util.findConsole("Bootware");
		out = console.newMessageStream();
	}

	private SimTechPreferences() {}

	/**
	 * Updates the SimTech preferences.
	 *
	 * @param preferences A Map of Strings containing the preferences.
	 */
	public static void update(final Map<String, String> preferences) {
		final MapConfiguration configuration = new MapConfiguration(preferences);
		configuration.setThrowExceptionOnMissing(true);

		updateSimTechSettings(configuration);
		updateOdeSettings(configuration);
		updateFragmentoSettings(configuration);
	}

	/**
	 * Updates the SimTech preferences.
	 *
	 * @param configuration A MapConfiguration object containing the preferences.
	 */
	private static void updateSimTechSettings(final MapConfiguration configuration) {
		try {
			out.println("Updating SimTech preferences.");

			// Get SimTech preference store.
			final IPreferenceStore simTechStore = new ScopedPreferenceStore(new InstanceScope(), "org.eclipse.bpel.ui");

			// Set the new values.
			simTechStore.setValue("ACTIVE_MQ_URL", configuration.getString("activeMQUrl"));
			//simTechStore.setValue("SEND_REQUESTS", true);
			//simTechStore.setValue("USE_EXT_ITERATION", false);
			//simTechStore.setValue("INSTANCE_WAITING_TIME", "200");

			// Restart ActiveMQ connection by executing the already existing restart command.
			out.println("Restarting ActiveMQ connection");
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					try {
						final IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);
						handlerService.executeCommand("org.eclipse.bpel.ui.restartActiveMQ", null);
					}
					catch (CommandException e) {
						out.println(e.getMessage());
						out.println("Restarting ActiveMQ connection failed. Please restart it manually using the command in the SimTech menu.");
					}
				}
			});
		}
		catch (NoSuchElementException e) {
			out.println("There was an error while setting the SimTech preferences: " + e.getMessage());
		}
	}

	/**
	 * Updates the ODE preferences.
	 *
	 * @param configuration A MapConfiguration object containing the preferences.
	 */
	private static void updateOdeSettings(final MapConfiguration configuration) {
		try {
			out.println("Updating ODE preferences.");

			// Get the Ode preference store.
			final IPreferenceStore odeStore = new ScopedPreferenceStore(new InstanceScope(), "org.eclipse.apache.ode.processManagement");

			// Set the new values.
			odeStore.setValue("pref_ode_url", configuration.getString("odeServerUrl"));
			//odeStore.setValue("pref_ode_version", "ODE_Version_134");
		}
		catch (NoSuchElementException e) {
			out.println("There was an error while setting the ODE preferences: " + e.getMessage());
		}
	}

	/**
	 * Updates the Fragmento preferences.
	 *
	 * @param configuration A MapConfiguration object containing the preferences.
	 */
	private static void updateFragmentoSettings(final MapConfiguration configuration) {
		try {
			out.println("Updating Fragmento preferences.");

			// Get Fragmento objects.
			final Presenter presenter = FragmentoPlugIn.getDefault().getPresenter();
			final TreeViewerOperator operator = presenter.getOperator();

			// Set the new values.
			operator.getFragmento().setServiceURI(configuration.getString("fragmentoUrl"));

			// Load fragments from repository.
			out.println("Loading fragments from the repository.");
			if (presenter.isValidUrl(configuration.getString("fragmentoUrl"))) {
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						// This is hackish. The toolbar buttons won't be added.
						// There has to be a better way to do this.
						final TodoMockModel mock = operator.getMock();
						final TreeViewer viewer = operator.getViewer();
						mock.getCategories().clear();
						operator.init();
						viewer.refresh();
						final Integer level = 3;
						viewer.expandToLevel(level);
					}
				});
			}
			else {
				out.println("Could not find Fragmento repository at " + configuration.getString("fragmentoUrl"));
			}
		}
		catch (NoSuchElementException e) {
			out.println("There was an error while setting the Fragmento preferences: " + e.getMessage());
		}
	}

}
