package org.eclipse.bpel.ui.agora.snapshots.wizard;

import java.util.HashMap;
import java.util.List;

import org.eclipse.bpel.model.Activity;
import org.eclipse.bpel.model.util.BPELUtils;
import org.eclipse.bpel.ui.agora.communication.JMSCommunication;
import org.eclipse.bpel.ui.agora.communication.ManagementAPIHandler;
import org.eclipse.bpel.ui.agora.manager.MonitorManager;
import org.eclipse.bpel.ui.agora.ode135.client.TPartnerLinkInfo;
import org.eclipse.bpel.ui.agora.ode135.client.TPartnerLinkRefList;
import org.eclipse.bpel.ui.agora.ode135.client.TSnapshotInfo;
import org.eclipse.bpel.ui.agora.ode135.client.TSnapshotVersion;
import org.eclipse.bpel.ui.agora.ode135.client.TVariableInfo;
import org.eclipse.bpel.ui.agora.ode135.client.TVariableRef;
import org.eclipse.bpel.ui.agora.ode135.client.TVariableRefList;
import org.eclipse.bpel.ui.agora.provider.MonitoringProvider;
import org.eclipse.bpel.ui.agora.snapshots.wizard.table.PartnerLinkInfo;
import org.eclipse.bpel.ui.agora.snapshots.wizard.table.VariableInfo;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.simtech.workflow.ode.auditing.communication.messages.InstanceInformation;
import org.simtech.workflow.ode.auditing.communication.messages.IterationBodyMessage;

/**
 * Wizard to specify which version of a snapshot and which of its variables has
 * to be loaded.
 * 
 * @author hahnml
 */
public class SnapshotWizard extends Wizard implements INewWizard {

	public enum TargetMethod {
		ITERATE, REEXECUTE
	}

	// wizard pages
	SnapshotSelectionPage snapshotSelectionPage;
	SnapshotVersionSelectionPage versionSelectionPage;
	SnapshotVariableSelectionPage variableSelectionPage;
	SnapshotPartnerLinkSelectionPage partnerLinkSelectionPage;

	// the workbench instance
	protected IWorkbench workbench;

	protected TSnapshotInfo snapInfo = null;
	protected List<TSnapshotInfo> snapInfos = null;
	protected Activity activity = null;
	protected TSnapshotVersion version = null;
	protected Long instanceID = null;
	private MonitorManager manager = null;

	private TargetMethod targetMethod = TargetMethod.REEXECUTE;

	public SnapshotWizard(List<TSnapshotInfo> snapshotInfos, Activity activity,
			TargetMethod method) {
		super();

		this.snapInfos = snapshotInfos;
		this.activity = activity;
		this.targetMethod = method;

		manager = MonitoringProvider.getInstance().getMonitorManager(
				BPELUtils.getProcess(activity));
		instanceID = manager.getInstanceInformation().getInstanceID();

		snapshotSelectionPage = new SnapshotSelectionPage("Snapshots", "Select a snapshot",
				"Please select a snapshot which you want to load.");
		versionSelectionPage = new SnapshotVersionSelectionPage("SnapshotVersions",
				"Select snapshot version",
				"Please select the version of the snapshot which you want to load.");
		variableSelectionPage = new SnapshotVariableSelectionPage("SnapshotVariables",
				"Select snapshot variables",
				"Please select all variables of the snapshot which you want to reload.");
		partnerLinkSelectionPage = new SnapshotPartnerLinkSelectionPage("SnapshotPartnerLinks",
				"Select snapshot partnerLinks",
		"Please select all partnerLinks of the snapshot which you want to reload.");
	}

	public void addPages() {
		addPage(snapshotSelectionPage);
		addPage(versionSelectionPage);
		addPage(variableSelectionPage);
		addPage(partnerLinkSelectionPage);
	}

	@Override
	public boolean performFinish() {
		InstanceInformation instance = manager.getInstanceInformation();

		if (!snapshotSelectionPage.isLoadSnapshot()) {
			if (this.targetMethod == TargetMethod.ITERATE) {
				List<String> iterationBodyList = manager.getMonitoringHelper().resetStateOfSuccessorActivities(
						activity);

				// Start the "standard" iteration
				ManagementAPIHandler.iterateInInstance(
						instance.getInstanceID(), activity.getXPath());
				
				// send the list of reset activities to the auditing tool
				IterationBodyMessage mes = new IterationBodyMessage();
				mes.setActivityXPathList(iterationBodyList);
				mes.setProcessInstanceID(instance.getInstanceID());
				JMSCommunication.getInstance().sendRequest(mes);
			} else {
				List<String> iterationBodyList = manager.getMonitoringHelper().resetStateOfSuccessorActivities(
						activity);

				// Start the reexecution without loading a snapshot
				ManagementAPIHandler.reexecuteInInstance(
						instance.getInstanceID(), activity.getXPath(), "",
						-1L);
				
				// send the list of reset activities to the auditing tool
				IterationBodyMessage mes = new IterationBodyMessage();
				mes.setActivityXPathList(iterationBodyList);
				mes.setProcessInstanceID(instance.getInstanceID());
				JMSCommunication.getInstance().sendRequest(mes);
			}
		} else {
			Long lVersion = Long.parseLong(version.getVersion());
			if (lVersion != null && lVersion != -1L) {
				List<String> iterationBodyList = manager.getMonitoringHelper().resetStateOfSuccessorActivities(
						activity);

				// Extract the values of the variable list of the third page in
				// a HashMap
				HashMap<TVariableInfo, Boolean> variableMap = new HashMap<TVariableInfo, Boolean>();
				for (VariableInfo vInf : variableSelectionPage.getInfoList()) {
					variableMap.put(vInf.getInfo(), vInf.isSelected());
				}
				
				// Extract the values of the partnerLink list of the fourth page in
				// a HashMap
				HashMap<TPartnerLinkInfo, Boolean> partnerLinkMap = new HashMap<TPartnerLinkInfo, Boolean>();
				for (PartnerLinkInfo pInf : partnerLinkSelectionPage.getInfoList()) {
					partnerLinkMap.put(pInf.getInfo(), pInf.isSelected());
				}

				// Check if all variables and partnerLinks were selected
				if (variableMap.containsValue(Boolean.FALSE) || partnerLinkMap.containsValue(Boolean.FALSE)
						|| this.targetMethod == TargetMethod.ITERATE) {
					// If one variable/partnerLink was not selected we have to use the
					// extended reexecution and specify the variables/partnerLinks to reset
					TVariableRefList variables = new TVariableRefList();
					// Init the internal list
					variables.getVariable();
					for (TVariableInfo info : variableMap.keySet()) {
						if (variableMap.get(info)) {
							variables.getVariable().add(info.getSelf());
						}
					}
					
					TPartnerLinkRefList partnerLinks = new TPartnerLinkRefList();
					// Init the internal list
					partnerLinks.getPartnerLink();
					for (TPartnerLinkInfo info : partnerLinkMap.keySet()) {
						if (partnerLinkMap.get(info)) {
							partnerLinks.getPartnerLink().add(info.getSelf());
						}
					}

					if (this.targetMethod == TargetMethod.REEXECUTE) {
						ManagementAPIHandler.reexecuteExtInInstance(
								instance.getInstanceID(), activity.getXPath(),
								snapInfo.getActivityXPath(), lVersion,
								variables, partnerLinks);
					} else {
						ManagementAPIHandler.iterateExtInInstance(
								instance.getInstanceID(), activity.getXPath(),
								snapInfo.getActivityXPath(), lVersion,
								variables, partnerLinks);
					}
				} else {
					// Start the normal reexecution in case that all variables
					// were selected
					ManagementAPIHandler.reexecuteInInstance(
							instance.getInstanceID(), activity.getXPath(),
							snapInfo.getActivityXPath(), lVersion);
				}
				
				// send the list of reset activities to the auditing tool
				IterationBodyMessage mes = new IterationBodyMessage();
				mes.setActivityXPathList(iterationBodyList);
				mes.setProcessInstanceID(instance.getInstanceID());
				JMSCommunication.getInstance().sendRequest(mes);

			}
		}

		return true;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.workbench = workbench;
	}

	public boolean canFinish() {
		return versionSelectionPage.isPageComplete() || !snapshotSelectionPage.isLoadSnapshot();
	}

	public TargetMethod getTargetMethod() {
		return targetMethod;
	}
}
