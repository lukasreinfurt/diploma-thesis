package org.eclipse.bpel.ui.agora.actions;


import org.eclipse.bpel.ui.BPELMultipageEditorPart;
import org.eclipse.bpel.ui.agora.AgoraStates;
import org.eclipse.bpel.ui.agora.manager.MonitorManager;
import org.eclipse.bpel.ui.agora.provider.MonitoringProvider;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

/**
 * @author hahnml
 *
 */
public class DeployNewVersionAction extends Action implements IEditorActionDelegate{

	private BPELMultipageEditorPart fEditor;

	public void setActiveEditor(IAction arg0, IEditorPart arg1) {
		if (arg1 instanceof BPELMultipageEditorPart) {
			fEditor = (BPELMultipageEditorPart) arg1;
		} else {
			fEditor = null;
		}
	}

	public void run(IAction arg0) {
		fEditor.refreshEditor();
		
		//Get the current instance from the opened process model in the editor and and the 
		//corresponding monitor manager
		MonitorManager manager = MonitoringProvider.getInstance().getMonitorManager(fEditor);
	
		AgoraStates state = manager.getApplicationState();

		if (state == AgoraStates.angehalten) {
			manager.migrateInstanceToNewVersion();
		}
	}

	@Override
	public void selectionChanged(IAction arg0, ISelection arg1) {
		// do nothing
	}

}
