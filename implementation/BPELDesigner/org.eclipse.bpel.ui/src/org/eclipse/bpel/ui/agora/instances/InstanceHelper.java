package org.eclipse.bpel.ui.agora.instances;

import org.eclipse.bpel.model.Process;
import org.eclipse.bpel.ui.BPELEditDomain;
import org.eclipse.bpel.ui.BPELMultipageEditorPart;
import org.eclipse.bpel.ui.agora.communication.JMSCommunication;
import org.eclipse.bpel.ui.agora.manager.MonitorManager;
import org.eclipse.bpel.ui.agora.manager.ProcessManager;
import org.eclipse.bpel.ui.agora.manager.XPathMapper;
import org.eclipse.bpel.ui.agora.provider.MonitoringProvider;
import org.eclipse.bpel.ui.uiextensionmodel.BPELStates;
import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.simtech.workflow.ode.auditing.communication.messages.InstanceHistoryRequestMessage;
import org.simtech.workflow.ode.auditing.communication.messages.InstanceInformation;

/**
 * This class holds some helpful methods to use for handling instances.
 * 
 * @author hahnml
 * @author tolevar
 *
 */
public class InstanceHelper {

	public static void openInstance(String processPath, 
			final InstanceInformation instanceInformation, final Process originalModel) {

		// Get the active page
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		// Get the path of the process model
		IPath path = Path.fromOSString(processPath);
		// Get the workspace pace
		IPath workspace = ResourcesPlugin.getWorkspace().getRoot()
				.getLocation();
		// Make the process model path relative to the workspace path
		path = path.makeRelativeTo(workspace);
		// Get the process model file
		IFile file = FileBuffers.getWorkspaceFileAtLocation(path);
		// Get the input resource object of the process model
		IEditorInput input = new FileEditorInput(file);

		try {
			IEditorPart part = page.findEditor(input);
			// Check if this process model is opened already in an BPEL Editor
			if (part != null) {
				// Close the opened editor with save opportunity for the user.
				page.closeEditor(part, true);
			}

			// Open the process model file in the BPEL Editor
			part = page.openEditor(input, "org.eclipse.bpel.ui.bpeleditor");

			if (part instanceof BPELMultipageEditorPart) {
				BPELMultipageEditorPart bpel = (BPELMultipageEditorPart) part;

				((BPELEditDomain) bpel.getEditDomain()).setInstanceState(InstanceHelper
						.mapToUIExtensionState(instanceInformation
						.getState().name()));
				
				//@hahnml: Create and register a new monitor managers
				ProcessManager processManager = MonitoringProvider.getInstance().getProcessManager(bpel);
				MonitorManager manager = processManager.createMonitorManager(processManager, instanceInformation);
				
				if (originalModel != null) {
					manager.setOriginalModel(originalModel);
				}
				
				// Set the MonitorManager data
				manager.setPackageName(
						instanceInformation.getPackageName());
				manager.setApplicationState(
						instanceInformation.getState());
				
				// Synchronize the opened instance activity states
				synchronizeActivityStates(instanceInformation, bpel
						.getProcess());
			}
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}
	
	public static void synchronizeActivityStates(
			final InstanceInformation instanceInformation, Process process) {
		// Request the history of this instance
		InstanceHistoryRequestMessage request = new InstanceHistoryRequestMessage();
		request.setProcessInstanceID(instanceInformation.getInstanceID());

		// Send the request over the queue
		JMSCommunication.getInstance().sendRequest(request);

		// Clear all deprecated states
		XPathMapper.resetAllStates(process);
	}

	public static BPELStates mapToUIExtensionState(String stateName) {
		return BPELStates.valueOf(stateName.toUpperCase());
	}
}
