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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
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
	private Thread bootwareThread;
	private Thread startProcessInstanceThread;
	private static Object monitor = new Object();
	private static boolean bootstrappingDone = false;

	public void setActiveEditor(IAction arg0, IEditorPart arg1) {

		if (arg1 instanceof BPELMultipageEditorPart) {
			fEditor = (BPELMultipageEditorPart) arg1;
		} else {
			fEditor = null;
		}
	}

	public void run(IAction arg0) {

		//@reinfuls
		// Do nothing if bootware thread is still alive from previous call.
		if (bootwareThread != null && bootwareThread.isAlive()) {
			MessageDialog.openInformation(
					Display.getDefault().getActiveShell(),
					"Still bootstrapping ...",
					"The bootstrapping process has not finished yet. Please wait until the"
					+ "process is finished and try again.");
			return;
		}

		// Start the bootware plugin in a separate thread so that the UI won't be blocked.
		bootwareThread = new Thread(new Runnable() {

			public void run() {

				try {
					// Get all extension that implement the bootware extension point.
					IExtensionRegistry reg = Platform.getExtensionRegistry();
					IConfigurationElement[] extensions = reg.getConfigurationElementsFor("org.eclipse.bpel.ui.bootware");

					// Call the execute method of each extension.
					for (int i = 0; i < extensions.length; i++) {
						IConfigurationElement element = extensions[i];
						if (element.getAttribute("class") != null) {
							try {
								IBootwarePlugin plugin = (IBootwarePlugin) element.createExecutableExtension("class");
								plugin.execute();
							} catch (CoreException e) {
								e.printStackTrace();
							}
						}
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}

				// Notify startProcessInstanceThread that the bootstrapping process is finished.
				bootstrappingDone = true;
				synchronized(monitor) {
					monitor.notifyAll();
				}
			}
		});

		bootwareThread.start();

		// Start the process instance start code in a separate thread so we can wait
		// for the bootstrapping process to finish without blocking the UI.
		startProcessInstanceThread = new Thread(new Runnable() {

			public void run() {

				// Wait for bootstrapping process to finish
				while(!bootstrappingDone) {
					synchronized(monitor) {
						try {
							monitor.wait();
						} catch(InterruptedException e) {
							e.printStackTrace();
						}
					}
				}

				// Execute the code to start the process instance. It changes the UI and
				// since this is only allowed from the main eclipse thread, this code
				// has to be wrapped in asyncExec().
				Display.getDefault().asyncExec(new Runnable() {

					public void run() {

						fEditor.refreshEditor();

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

				});

			}

		});

		startProcessInstanceThread.start();

	}

	@Override
	public void selectionChanged(IAction arg0, ISelection arg1) {
		// do nothing

	}

}
