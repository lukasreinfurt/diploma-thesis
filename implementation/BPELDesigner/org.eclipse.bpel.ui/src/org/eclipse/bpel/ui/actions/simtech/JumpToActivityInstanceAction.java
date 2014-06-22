package org.eclipse.bpel.ui.actions.simtech;

import org.eclipse.apache.ode.processmanagement.IProcessManagementConstants;
import org.eclipse.apache.ode.processmanagement.ProcessManagementUI;
import org.eclipse.bpel.model.Activity;
import org.eclipse.bpel.model.util.BPELUtils;
import org.eclipse.bpel.ui.BPELUIPlugin;
import org.eclipse.bpel.ui.agora.AgoraStates;
import org.eclipse.bpel.ui.agora.communication.ManagementAPIHandler;
import org.eclipse.bpel.ui.agora.manager.MonitorManager;
import org.eclipse.bpel.ui.agora.provider.MonitoringProvider;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.simtech.workflow.ode.auditing.communication.messages.InstanceInformation;

/**
 * @author hahnml
 * 
 */
public class JumpToActivityInstanceAction implements IObjectActionDelegate {

	private Activity activity;

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {

	}

	@Override
	public void run(IAction action) {
		if (this.activity != null) {
			MonitorManager manager = MonitoringProvider.getInstance()
					.getMonitorManager(BPELUtils.getProcess(activity));

			if (manager != null) {
				InstanceInformation instance = manager.getInstanceInformation();

				if (manager.getApplicationState() == AgoraStates.angehalten) {

					// Check if the specified ODE version supports iterations
					if (ProcessManagementUI.getDefault().getPreferenceStore()
							.getString(IProcessManagementConstants.PREF_ODE_VERSION)
							.equals(ManagementAPIHandler.ODE_VERSION_135)) {
						manager.getMonitoringHelper()
								.resetStateOfSuccessorActivities(activity);

						// Start the iteration
						ManagementAPIHandler.jumpToInInstance(
								instance.getInstanceID(),
								this.activity.getXPath());
					} else {
						MessageDialog
								.openInformation(
										Display.getDefault().getActiveShell(),
										"Selected ODE version doesn't support jump to functionality",
										"The ODE version which is selected in the SimTech preferences doesn't support jump to. You need ODE version 1.3.5 or above to use jump to in process instances.");
					}
				} else {
					String failureMessage = "";

					switch (manager.getApplicationState()) {
					case gestoppt:
						failureMessage = "The process instance is not running so no \"jumpTo\" could be started. Please start and suspend a process instance to jump to this activity.";
						break;
					case beendet:
						failureMessage = "The process instance is completed so no \"jumpTo\" can be started. Please restart and suspend the process instance to jump to this activity.";
						break;
					case laufend:
						failureMessage = "The process instance is running so no \"jumpTo\" can be started. Please suspend the process instance to jump to this activity.";
						break;
					default:
						failureMessage = "The \"jumpTo\" couldn't be executed, please try again.";
						break;
					}

					MessageDialog
							.openInformation(Display.getDefault()
									.getActiveShell(),
									"The jump to couldn't be executed.",
									failureMessage);
				}
			} else {
				MessageDialog
						.openError(
								Display.getDefault().getActiveShell(),
								"No running instance available.",
								"Jumping to a selected activity is only possible if an instance is already started.");
			}
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof StructuredSelection) {
			if (((StructuredSelection) selection).getFirstElement() instanceof Activity) {
				activity = (Activity) ((StructuredSelection) selection)
						.getFirstElement();
			}
		}
	}

}
