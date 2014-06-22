/**
 * 
 */
package org.eclipse.bpel.ui.agora.manager;

import java.util.List;

import org.eclipse.bpel.model.Process;
import org.eclipse.bpel.model.Variable;
import org.eclipse.bpel.model.debug.XPathMapProvider;
import org.eclipse.bpel.ui.BPELMultipageEditorPart;
import org.eclipse.bpel.ui.agora.AgoraStates;
import org.eclipse.bpel.ui.agora.communication.ControllerInputEventHandler;
import org.eclipse.bpel.ui.agora.communication.EngineInstanceOutputEventHandler;
import org.eclipse.bpel.ui.agora.communication.JMSCommunication;
import org.eclipse.bpel.ui.agora.communication.ManagementAPIHandler;
import org.eclipse.bpel.ui.agora.debug.DebugManager;
import org.eclipse.bpel.ui.agora.views.EventModelProvider;
import org.eclipse.bpel.ui.util.ModelDiffHelper;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.simtech.workflow.ode.auditing.agora.BPELStates;
import org.simtech.workflow.ode.auditing.communication.messages.InstanceInformation;

/**
 * The Monitor Manager is the central part of Agora. It handles the internal
 * state of it.
 * 
 * @author aeichel
 * @author hahnml
 */
public class MonitorManager {

	private AgoraStates STATE_FLAG = AgoraStates.gestoppt;

	private ProcessManager processManager;

	private Process process;
	private BPELMultipageEditorPart editor;
	private InstanceInformation instanceInfo;
	private EngineInstanceOutputEventHandler eventHandler;
	private EventModelProvider eventProvider;

	private Process originalModel = null;

	private InstanceManager instanceManager;

	private MonitoringHelper monitoringHelper;

	private MonitorManager(ProcessManager processManager,
			InstanceInformation information) {
		this.processManager = processManager;

		eventHandler = new EngineInstanceOutputEventHandler(this);
		new ControllerInputEventHandler();

		this.instanceInfo = information;
		this.editor = processManager.getEditor();
		this.process = processManager.getProcess();
		this.eventProvider = new EventModelProvider();
		this.monitoringHelper = new MonitoringHelper();
		this.instanceManager = new InstanceManager();
	}

	// @hahnml
	public static MonitorManager create(ProcessManager processManager,
			InstanceInformation information) {
		MonitorManager manager = new MonitorManager(processManager, information);

		// Register the created MonitorManager to the DebugManager
		processManager.getDebugManager().setMonitorManager(manager);

		return manager;
	}

	public void delete() {
		eventProvider.clear();
	}

	// @hahnml
	public InstanceManager getInstanceManager() {
		return this.instanceManager;
	}

	// @hahnml
	public MonitoringHelper getMonitoringHelper() {
		return this.monitoringHelper;
	}

	// @hahnml: Get the instance information from the monitor manager
	public InstanceInformation getInstanceInformation() {
		return this.instanceInfo;
	}
	
	public Process getOriginalModel() {
		return this.originalModel;
	}
	
	public void setOriginalModel(Process originalModel) {
		this.originalModel = originalModel;
	}

	public EngineInstanceOutputEventHandler getEventHandler() {
		return eventHandler;
	}

	public void stop() {
		if (JMSCommunication.getInstance().isInitialized()) {
			if (this.STATE_FLAG.compareTo(AgoraStates.beendet) != 0) {
				ManagementAPIHandler.terminateInstance(instanceInfo
						.getInstanceID());

				// @hahnml: Reset the instance ID in the InstanceInformation
				this.instanceInfo.setInstanceID(null);
			}
			this.STATE_FLAG = AgoraStates.gestoppt;

			this.monitoringHelper.reset();
			XPathMapper.resetAllStates(process);
			this.originalModel = null;
			// @hahnml: Delete the modelChanges.xml file if it exists
			deleteModelChangeFile();

		} else {
			openJMSFailureDialog();
		}
	}

	public void finish() {
		if (JMSCommunication.getInstance().isInitialized()) {

			if (this.STATE_FLAG.compareTo(AgoraStates.beendet) != 0) {
				ManagementAPIHandler.finishInstance(instanceInfo
						.getInstanceID());

				this.originalModel = null;
				// @hahnml: Delete the modelChanges.xml file if it exists
				deleteModelChangeFile();
			}

		} else {
			openJMSFailureDialog();
		}
	}

	/**
	 * @param variableList
	 * @param strings
	 */
	public void startWorkflow(List<Variable> variableList, List<String> strings) {
		// Reset all previous calculated states
		XPathMapper.resetAllStates(process);
		this.monitoringHelper.reset();

		// @hahnml: Delete the modelChanges.xml file if it exists
		deleteModelChangeFile();

		this.originalModel = null;

		if (JMSCommunication.getInstance().isInitialized()) {

			if (this.processManager.isDeployed()) {
				ManagementAPIHandler.invokeWS(editor, strings, variableList);
				this.STATE_FLAG = AgoraStates.laufend;
			} else {
				this.STATE_FLAG = AgoraStates.gestoppt;
			}

		} else {
			openJMSFailureDialog();
		}
	}

	public void resume() {
		if (JMSCommunication.getInstance().isInitialized()) {
			// Update the breakpoints on the engine
			getDebugManager().updateBreakpointsOnEngine();

			this.STATE_FLAG = AgoraStates.laufend;
			ManagementAPIHandler.resumeInstance(instanceInfo.getInstanceID());
		} else {
			openJMSFailureDialog();
		}
	}

	public void suspend() {
		if (JMSCommunication.getInstance().isInitialized()) {
			this.STATE_FLAG = AgoraStates.angehalten;
			ManagementAPIHandler.suspendInstance(instanceInfo.getInstanceID());

			// Buffer the process model before any changes
			// during the instance is suspended are made
			bufferCurrentModel();

		} else {
			openJMSFailureDialog();
		}
	}

	public void migrateInstanceToNewVersion() {
		if (JMSCommunication.getInstance().isInitialized()) {

			try {
				// @hahnml: Delete the modelChanges.xml file if it exists
				deleteModelChangeFile();

				ModelDiffHelper.calculateModelDifferences(this.originalModel,
						this.process, editor.getEditorFile().getLocation()
								.removeLastSegments(1).toOSString(),
						instanceInfo.getInstanceID());

				String packageName = ManagementAPIHandler
						.deployNewVersionOfProcess(this.editor.getEditorFile()
								.getLocation(), instanceInfo.getInstanceID());

				this.instanceInfo.setPackageName(packageName);
			} catch (Exception e) {

				MessageDialog.openError(Display.getDefault().getActiveShell(),
						"Deployment failed.",
						"The deployment of the new version of the process model failed."
								+ "\n\nReason:\n\n" + e.getMessage());
			}

		} else {
			openJMSFailureDialog();
		}
	}

	public void setCompleted() {
		this.STATE_FLAG = AgoraStates.beendet;

		this.originalModel = null;
		// @hahnml: Delete the modelChanges.xml file if it exists
		deleteModelChangeFile();

		// @hahnml: Reset the instance ID in the InstanceInformation
		this.instanceInfo.setInstanceID(null);
	}

	public void setSuspended() {
		this.STATE_FLAG = AgoraStates.angehalten;

		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				// Buffer the process model before any changes
				// during the instance is suspended are made
				bufferCurrentModel();
			}
		});
	}

	public void setTerminated() {
		this.STATE_FLAG = AgoraStates.gestoppt;

		this.originalModel = null;
		// @hahnml: Delete the modelChanges.xml file if it exists
		deleteModelChangeFile();

		// @hahnml: Reset the instance ID in the InstanceInformation
		this.instanceInfo.setInstanceID(null);
	}

	public Process getProcess() {
		return process;
	}

	/**
	 * Sets the application inner state by the actual state of an opened
	 * instance
	 * 
	 * @param instanceState
	 *            to set
	 */
	public void setApplicationState(BPELStates instanceState) {
		switch (instanceState) {
		case Terminated:
			this.STATE_FLAG = AgoraStates.gestoppt;
			break;
		case Executing:
			this.STATE_FLAG = AgoraStates.laufend;
			break;
		case Suspended:
			this.STATE_FLAG = AgoraStates.angehalten;
			break;
		case Completed:
			this.STATE_FLAG = AgoraStates.beendet;
			break;
		case Faulted:
			this.STATE_FLAG = AgoraStates.beendet;
			break;
		default:
			break;
		}
	}

	public AgoraStates getApplicationState() {
		return this.STATE_FLAG;
	}

	public String getPackageName() {
		return this.instanceInfo.getPackageName();
	}

	public void setPackageName(String packageName) {
		this.instanceInfo.setPackageName(packageName);
	}

	public BPELMultipageEditorPart getEditor() {
		return editor;
	}

	/**
	 * Shows an error dialog if the connection to ActiveMQ is not initialized.
	 */
	private void openJMSFailureDialog() {
		MessageDialog
				.openError(
						Display.getDefault().getActiveShell(),
						"The selected action couldn't be executed.",
						"The connection to ActiveMQ is not initialized properly. Please check if the URL is correct and reconnect to ActiveMQ.");
	}

	// @hahnml: Creates a local copy of the BPEL process model to compare it
	// with a later state
	public void bufferCurrentModel() {
		XPathMapProvider.getInstance().getXPathMap(process)
				.setIgnoreModeState(true);

		// Buffer the actual process model to get changes on resume
		this.originalModel = (Process) EcoreUtil.copy(this.process);

		XPathMapProvider.getInstance().getXPathMap(process)
				.setIgnoreModeState(false);
	}

	private void deleteModelChangeFile() {
		// Remove the modelChanges.xml file from the workspace
		final IContainer folder = editor.getEditorFile().getParent();

		try {
			folder.refreshLocal(IResource.DEPTH_ONE, null);
		} catch (CoreException e1) {
			e1.printStackTrace();
		}

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IProject project = editor.getEditorFile().getProject();
		IWorkspaceRunnable operation = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {

				IFile file = project.getFolder(folder.getName()).getFile(
						"modelChanges.xml");
				file.delete(false, monitor);

			}
		};
		try {
			workspace.run(operation, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	public EventModelProvider getEventModelProvider() {
		return this.eventProvider;
	}

	// @hahnml
	public DebugManager getDebugManager() {
		return this.processManager.getDebugManager();
	}

	// @hahnml: Returns if the Manager is in an active monitoring state
	public boolean isActive() {
		boolean isActive = false;

		if (this.STATE_FLAG.equals(AgoraStates.laufend)
				|| this.STATE_FLAG.equals(AgoraStates.angehalten)) {
			isActive = true;
		}

		return isActive;
	}
}
