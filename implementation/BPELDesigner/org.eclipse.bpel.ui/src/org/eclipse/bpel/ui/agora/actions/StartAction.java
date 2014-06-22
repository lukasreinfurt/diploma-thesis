package org.eclipse.bpel.ui.agora.actions;

import java.util.HashMap;

import org.eclipse.bpel.ui.BPELMultipageEditorPart;
import org.eclipse.bpel.ui.agora.AgoraStates;
import org.eclipse.bpel.ui.agora.manager.MonitorManager;
import org.eclipse.bpel.ui.agora.manager.ProcessManager;
import org.eclipse.bpel.ui.agora.provider.MonitoringProvider;
import org.eclipse.bpel.ui.simtech.properties.FileOperations;
import org.eclipse.bpel.ui.wizards.simtech.MetaDataWizard;
import org.eclipse.core.resources.IContainer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.simtech.workflow.ode.auditing.communication.messages.InstanceInformation;

import org.eclipse.bpel.ui.IBootwarePlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import java.io.PrintWriter;

/**
 * @author aeichel, tolevar
 *
 */
public class StartAction extends Action implements IEditorActionDelegate{

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

		//@reinfuls
		try {
			PrintWriter writer = new PrintWriter("omg123.txt", "UTF-8");

			String response = "";
			StringBuffer buffer = new StringBuffer();
			IExtensionRegistry reg = Platform.getExtensionRegistry();
			IConfigurationElement[] extensions = reg.getConfigurationElementsFor("org.eclipse.bpel.ui.bootware");

			for (int i = 0; i < extensions.length; i++) {
				IConfigurationElement element = extensions[i];
				if (element.getAttribute("class") == null) {
					// handle
				} else {
					try {
						IBootwarePlugin plugin = (IBootwarePlugin) element.createExecutableExtension("class");
						response = plugin.execute();
					} catch (CoreException e) {
						response = e.toString();
					}
				}
				buffer.append(response);
				buffer.append('\n');
			}

			writer.println("Output");
			writer.println(buffer.toString());
			writer.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		//@schrotbn
		IContainer folder = fEditor.getEditorFile().getParent();
		HashMap<String, String> props = FileOperations.loadPropertiesFromDD(folder);
		if (props.size() == 0) {

			MetaDataWizard wizard = new MetaDataWizard(props, fEditor.getEditorFile());
			wizard.init(fEditor.getSite().getWorkbenchWindow()
					.getWorkbench(), null);

			WizardDialog dialog = new WizardDialog(fEditor.getSite()
					.getShell(), wizard);
			dialog.create();
			if (dialog.open() == Dialog.CANCEL)
				return;

			HashMap<String, String> newProps = wizard.getProperties();
			FileOperations
					.storePropertiesToDD(folder, newProps);
		}

		//@hahnml
		ProcessManager processManager = MonitoringProvider.getInstance().getProcessManager(fEditor);
		MonitorManager manager = processManager.getLastStartedInstance();

		//Check if a manager exists already
		if (manager == null) {
			manager = processManager.createMonitorManager(processManager, new InstanceInformation());
			//Update the references to the new MonitorManager instance
			MonitoringProvider.getInstance().changeActiveEditor(fEditor);
		}

		AgoraStates state = manager.getApplicationState();
				switch(state){
				case gestoppt:
					if (processManager.getParameterHandler().initDialog(processManager)) {
						processManager.getParameterHandler().getDialog().open();
					} else {
						processManager.prepareAndStartProcessInstance(null, null);
					}
					break;
				case beendet:
					if (processManager.getParameterHandler().initDialog(processManager)) {
						processManager.getParameterHandler().getDialog().open();
					} else {
						processManager.getParameterHandler().getParameters().clear();
						processManager.prepareAndStartProcessInstance(null, null);
					}
					break;
				case angehalten:
					manager.resume();
					break;
					default:
						// do nothing
				}

	}

	@Override
	public void selectionChanged(IAction arg0, ISelection arg1) {
		// do nothing

	}

}
