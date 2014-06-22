package org.eclipse.bpel.ui.agora.handlers;

import java.util.Iterator;

import javax.xml.namespace.QName;

import org.eclipse.bpel.common.extension.model.Extension;
import org.eclipse.bpel.common.extension.model.ExtensionMap;
import org.eclipse.bpel.ui.BPELUIPlugin;
import org.eclipse.bpel.ui.agora.communication.JMSCommunication;
import org.eclipse.bpel.ui.agora.instances.InstanceSelectionDialog;
import org.eclipse.bpel.ui.uiextensionmodel.ProcessExtension;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;
import org.simtech.workflow.ode.auditing.communication.messages.RequestAllInstancesMessage;

/**
 * This class handles the context menu command to open a dialog with a list of
 * all known instances of a bpel process model.
 * 
 * @author hahnml
 * 
 */
public class ShowInstancesDialogHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// We only open the dialog if sending requests is enabled.
		if (BPELUIPlugin.INSTANCE.getPreferenceStore().getBoolean(
				"SEND_REQUESTS")) {
			IStructuredSelection selection = (IStructuredSelection) HandlerUtil
					.getActiveMenuSelection(event);

			String processPath = "";

			if (selection.getFirstElement() instanceof IResource) {
				IResource bpelResource = (IResource) selection
						.getFirstElement();

				if (bpelResource.getFullPath().getFileExtension().equals("bpel")) {
					processPath = ResourcesPlugin.getWorkspace().getRoot()
							.getLocation().toOSString()
							+ bpelResource.getFullPath().toOSString();

					// Get the processName and processVersion out of the
					// *.bpelex
					// file
					String bpelexPath = ResourcesPlugin.getWorkspace()
							.getRoot().getLocation().toOSString()
							+ bpelResource.getFullPath().removeFileExtension()
									.addFileExtension("bpelex");

					ProcessExtension ext = loadProcessExtension(bpelexPath);

					if (ext != null) {
						if (ext.getProcessName() != null) {
							QName processName = QName.valueOf(ext.getProcessName());
							Long processVersion = ext.getProcessVersion();
							
							// Request all registered instances of the selected
							// process
							// model
							RequestAllInstancesMessage request = new RequestAllInstancesMessage();
							request.setProcessName(processName);
							request.setProcessVersion(processVersion);

							// Send the request over the queue
							JMSCommunication.getInstance().sendRequest(request);

							// Open an instance selection dialog
							new InstanceSelectionDialog(processPath);
						} else {
							MessageDialog
									.openError(
											Display.getDefault()
													.getActiveShell(),
											"Loading of the *.bpelex file caused an exception",
											"The process name and process version could not be read from the *.bpelex file.");
						}
					} else {
						MessageDialog
								.openError(
										Display.getDefault().getActiveShell(),
										"Loading of the *.bpelex file caused an exception",
										"The process name and process version could not be read from the *.bpelex file.");
					}
				} else {
					MessageDialog
							.openInformation(
									Display.getDefault().getActiveShell(),
									"No bpel file selected",
									"The selected file is no valid BPEL process model. Please select a *.bpel file.");
				}
			}

		} else {
			// Show a information dialog that the requested information is not
			// available.
			MessageDialog
					.openInformation(
							Display.getDefault().getActiveShell(),
							"Sending requests is disabled",
							"The requested information is not available because sending requests to the connected SimTech Auditing Application is disabled.\n"
									+ "This can be enabled in the SimTech preferences, but you should only activate sending request if you know that an Auditing Application is connected and available over the given ActiveMQ url.");
		}

		return null;
	}

	private ProcessExtension loadProcessExtension(String path) {
		ProcessExtension ext = null;

		// Obtain a new resource set
		ResourceSet resSet = new ResourceSetImpl();

		// Get the resource
		Resource resource = resSet.getResource(URI.createFileURI(path), true);

		// Get the ProcessExtension model element
		ExtensionMap map = (ExtensionMap) resource.getContents().get(0);

		@SuppressWarnings("unchecked")
		Iterator<Extension> iter = map.getExtensions().iterator();
		while (ext == null && iter.hasNext()) {
			Extension obj = iter.next();
			if (obj.getExtensionObject() instanceof ProcessExtension) {
				ext = (ProcessExtension) obj.getExtensionObject();
			}
		}

		return ext;
	}
}
