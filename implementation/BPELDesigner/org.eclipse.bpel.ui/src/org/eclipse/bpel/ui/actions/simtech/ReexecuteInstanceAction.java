package org.eclipse.bpel.ui.actions.simtech;

import org.eclipse.apache.ode.processmanagement.IProcessManagementConstants;
import org.eclipse.apache.ode.processmanagement.ProcessManagementUI;
import org.eclipse.bpel.model.Activity;
import org.eclipse.bpel.model.util.BPELUtils;
import org.eclipse.bpel.ui.BPELUIPlugin;
import org.eclipse.bpel.ui.agora.AgoraStates;
import org.eclipse.bpel.ui.agora.communication.ManagementAPIHandler;
import org.eclipse.bpel.ui.agora.manager.MonitorManager;
import org.eclipse.bpel.ui.agora.ode135.client.TSnapshotInfoList;
import org.eclipse.bpel.ui.agora.provider.MonitoringProvider;
import org.eclipse.bpel.ui.agora.snapshots.wizard.SnapshotWizard;
import org.eclipse.bpel.ui.agora.snapshots.wizard.SnapshotWizard.TargetMethod;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.simtech.workflow.ode.auditing.communication.messages.InstanceInformation;

/**
 * @author hahnml
 * 
 */
public class ReexecuteInstanceAction implements IObjectActionDelegate {

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

						TSnapshotInfoList snapshotInfo = ManagementAPIHandler
								.getSnapshots(instance.getInstanceID(),
										this.activity.getXPath());

						if (!snapshotInfo.getSnapshotInfo().isEmpty()) {
							// create the wizard
							SnapshotWizard wizard = new SnapshotWizard(
									snapshotInfo.getSnapshotInfo(),
									this.activity,
									TargetMethod.REEXECUTE);
							wizard.init(manager.getEditor().getSite()
									.getWorkbenchWindow().getWorkbench(), null);

							// Instantiates the wizard container with the
							// wizard
							// and opens it
							WizardDialog dialog = new WizardDialog(manager
									.getEditor().getSite().getShell(), wizard);
							dialog.create();

							dialog.setMinimumPageSize(600, 300);
							dialog.setPageSize(800, 300);

							dialog.open();
						} else {
							// No snapshots available for the given parameters
							MessageDialog
									.openInformation(
											Display.getDefault()
													.getActiveShell(),
											"No snapshots are available for the given activity and instance",
											"There are no snapshots available for the current running process instance and the selected activity.");
						}
					} else {
						MessageDialog
								.openInformation(
										Display.getDefault().getActiveShell(),
										"Selected ODE version doesn't support reexecution",
										"The ODE version which is selected in the SimTech preferences doesn't support reexecution. You need ODE version 1.3.5 or above to use reexecution in process instances.");
					}
				} else {
					String failureMessage = "";

					switch (manager.getApplicationState()) {
					case gestoppt:
						failureMessage = "The process instance is not running so no reexecution could be started. Please start and suspend a process instance to reexecution from this activity.";
						break;
					case beendet:
						failureMessage = "The process instance is completed so no reexecution can be started. Please restart and suspend the process instance to reexecution from this activity.";
						break;
					case laufend:
						failureMessage = "The process instance is running so no reexecution can be started. Please suspend the process instance to reexecution from this activity.";
						break;
					default:
						failureMessage = "The iteration couldn't be executed, please try again.";
						break;
					}

					MessageDialog.openInformation(Display.getDefault()
							.getActiveShell(),
							"The reexecution couldn't be executed.",
							failureMessage);
				}
			} else {
				MessageDialog
						.openError(
								Display.getDefault().getActiveShell(),
								"No running instance available.",
								"The reexecution of process parts is only possible if an instance is already started.");
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
