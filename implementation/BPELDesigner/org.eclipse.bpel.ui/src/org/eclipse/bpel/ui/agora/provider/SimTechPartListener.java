package org.eclipse.bpel.ui.agora.provider;

import org.eclipse.bpel.model.logging.BPELLogger;
import org.eclipse.bpel.ui.BPELMultipageEditorPart;
import org.eclipse.bpel.ui.agora.manager.ProcessManager;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;

public class SimTechPartListener implements IPartListener2 {

	@Override
	public void partClosed(IWorkbenchPartReference partRef) {
		if (partRef.getPart(false) instanceof BPELMultipageEditorPart) {
			BPELMultipageEditorPart editor = (BPELMultipageEditorPart) partRef.getPart(false);
			
			// @hahnml: Delete the processManager and all the corresponding
			// monitorManager's
			ProcessManager processManager = MonitoringProvider.getInstance()
					.getProcessManager(editor);
			
			//@hahnml: Shutdown the BPEL logger
			BPELLogger.getLogger().shutdown(editor);

			if (processManager != null) {
				MonitoringProvider.getInstance().deleteProcessManager(
						processManager);
			}
		}
	}

	@Override
	public void partOpened(IWorkbenchPartReference partRef) {
		if (partRef.getPart(false) instanceof BPELMultipageEditorPart) {
			BPELMultipageEditorPart editor = (BPELMultipageEditorPart) partRef.getPart(false);
			
			// @hahnml: Register a new process manager for this editor and initialize the list of breakpoints and the CommandFramework
			// for the view
			ProcessManager processManager = MonitoringProvider.getInstance().createProcessManager(editor);
			processManager.getDebugManager().initialize(
					editor.getDesignEditor().getDebug(),
					editor.getDesignEditor().getCommandFramework());
			//Start the BPEL change logger for this editor
			BPELLogger.getLogger().startup(editor, editor.getProcess().getName(), editor.getEditorFile());
			
			MonitoringProvider.getInstance().changeActiveEditor(editor);
		}
	}
	
	@Override
	public void partActivated(IWorkbenchPartReference partRef) {
		if (partRef.getPart(false) instanceof BPELMultipageEditorPart) {
			BPELMultipageEditorPart editor = (BPELMultipageEditorPart) partRef.getPart(false);
			
			//Change the input in the BPELLogger
			BPELLogger.getLogger().changeActiveSource(editor, editor.getProcess().getName(), editor.getEditorFile());
			
			MonitoringProvider.getInstance().changeActiveEditor(editor);
		}
	}
	
	//Im Moment nicht benötigte Event-Methoden
	@Override
	public void partDeactivated(IWorkbenchPartReference partRef) {
//		if (partRef.getPart(false) instanceof BPELMultipageEditorPart) {
//			BPELMultipageEditorPart editor = (BPELMultipageEditorPart) partRef.getPart(false);
//			
//			System.out.println("BPELEditor " + editor.getPartName() + " is deactivated!");
//			System.out.println(editor);
//		}
	}
	
	@Override
	public void partInputChanged(IWorkbenchPartReference partRef) {
//		if (partRef.getPart(false) instanceof BPELMultipageEditorPart) {
//			BPELMultipageEditorPart editor = (BPELMultipageEditorPart) partRef.getPart(false);
//			
//			System.out.println("BPELEditor " + editor.getPartName() + " has changed input!");
//			System.out.println(editor);
//		}
	}
	
	@Override
	public void partHidden(IWorkbenchPartReference partRef) {
//		if (partRef.getPart(false) instanceof BPELMultipageEditorPart) {
//			BPELMultipageEditorPart editor = (BPELMultipageEditorPart) partRef.getPart(false);
//			
//			System.out.println("BPELEditor " + editor.getPartName() + " is hidden!");
//			System.out.println(editor);
//		}
	}

	@Override
	public void partVisible(IWorkbenchPartReference partRef) {
//		if (partRef.getPart(false) instanceof BPELMultipageEditorPart) {
//			BPELMultipageEditorPart editor = (BPELMultipageEditorPart) partRef.getPart(false);
//			
//			System.out.println("BPELEditor " + editor.getPartName() + " is visible!");
//			System.out.println(editor);
//		}
	}
	
	@Override
	public void partBroughtToTop(IWorkbenchPartReference partRef) {
//		if (partRef.getPart(false) instanceof BPELMultipageEditorPart) {
//			BPELMultipageEditorPart editor = (BPELMultipageEditorPart) partRef.getPart(false);
//			
//			System.out.println("BPELEditor " + editor.getPartName() + " is brought to top!");
//			System.out.println(editor);
//		}
	}
}
