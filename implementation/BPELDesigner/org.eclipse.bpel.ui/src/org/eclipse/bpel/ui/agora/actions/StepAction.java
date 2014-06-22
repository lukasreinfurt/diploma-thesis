package org.eclipse.bpel.ui.agora.actions;

import org.eclipse.bpel.model.Activity;
import org.eclipse.bpel.model.BPELExtensibleElement;
import org.eclipse.bpel.model.Link;
import org.eclipse.bpel.model.Process;
import org.eclipse.bpel.ui.BPELMultipageEditorPart;
import org.eclipse.bpel.ui.agora.manager.MonitorManager;
import org.eclipse.bpel.ui.agora.provider.MonitoringProvider;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

public class StepAction extends Action implements IEditorActionDelegate {

	private BPELMultipageEditorPart fEditor;

	public void setActiveEditor(IAction arg0, IEditorPart arg1) {

		if (arg1 instanceof BPELMultipageEditorPart) {
			fEditor = (BPELMultipageEditorPart) arg1;
		} else {
			fEditor = null;
		}
	}

	public void run(IAction action) {
		if (fEditor != null) {
			MonitorManager manager = MonitoringProvider.getInstance()
					.getMonitorManager(fEditor);

			if (manager != null
					&& manager.getDebugManager().getSelectedActivity() != null) {

				// Update the breakpoints on the engine
				manager.getDebugManager().updateBreakpointsOnEngine();

				manager.getDebugManager().releaseBlockingEvent();
			}
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof StructuredSelection) {
			if (((StructuredSelection) selection).getFirstElement() instanceof Activity
					|| ((StructuredSelection) selection).getFirstElement() instanceof Link
					|| ((StructuredSelection) selection).getFirstElement() instanceof Process) {
				if (fEditor != null) {

					// Check if a ProcessManager is registered for this editor
					if (MonitoringProvider.getInstance().getProcessManager(
							fEditor) != null) {

						// Get the MonitorManager of the process, if one exists
						MonitorManager manager = MonitoringProvider
								.getInstance().getMonitorManager(fEditor);

						if (manager != null) {
							manager.getDebugManager()
									.setSelectedActivity(
											(BPELExtensibleElement) ((StructuredSelection) selection)
													.getFirstElement());
						}
					}
				}
			}
		}
	}
}
