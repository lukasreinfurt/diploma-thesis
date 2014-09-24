package org.eclipse.bpel.ui.agora.actions;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

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
	private IBootwarePlugin bootwarePlugin;
	private Thread startProcessInstanceThread;
	private static CountDownLatch bootstrappingDoneLatch;

	public void setActiveEditor(IAction arg0, IEditorPart arg1) {

		if (arg1 instanceof BPELMultipageEditorPart) {
			fEditor = (BPELMultipageEditorPart) arg1;
		} else {
			fEditor = null;
		}
	}

	public void run(IAction arg0) {

		//@reinfuls

		// Load bootware plugin if it is not already loaded.
		if (bootwarePlugin == null) {
			// Get all extension that implement the bootware extension point.
			IExtensionRegistry reg = Platform.getExtensionRegistry();
			IConfigurationElement[] extensions = reg.getConfigurationElementsFor("org.eclipse.bpel.ui.bootware");

			// Load the extension (there should only be one).
			for (int i = 0; i < extensions.length; i++) {
				IConfigurationElement element = extensions[i];
				if (element.getAttribute("class") != null) {
					try {
						bootwarePlugin = (IBootwarePlugin) element.createExecutableExtension("class");
					} catch (CoreException e) {
						e.printStackTrace();
					}
				}
			}
		}
		else {
			if (bootwarePlugin.isShuttingDown()) {
				MessageDialog.openInformation(
					Display.getDefault().getActiveShell(),
					"Bootware shutting down",
					"The Bootware is shutting down at the moment. Please wait until the"
					+ "process is finished and try again.");
				return;
			}
		}

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
				// Execute bootstrapping process.
				if (bootwarePlugin != null) {
					bootwarePlugin.execute();
				}

				// Notify startProcessInstanceThread that the bootstrapping process is finished.
				bootstrappingDoneLatch.countDown();
			}
		});

		// Reset latch.
		bootstrappingDoneLatch = new CountDownLatch(1);
		bootwareThread.start();

		// Start the process instance start code in a separate thread so we can wait
		// for the bootstrapping process to finish without blocking the UI.
		startProcessInstanceThread = new Thread(new Runnable() {

			public void run() {

				// Wait for bootstrapping process to finish
				try {
					bootstrappingDoneLatch.await();
				}
				catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}

				// Execute the code to start the process instance. It changes the UI and
				// since this is only allowed from the main eclipse thread, this code
				// has to be wrapped in syncExec().
				Display.getDefault().syncExec(new Runnable() {

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
