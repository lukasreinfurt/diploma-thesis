package org.eclipse.bpel.ui.agora.actions;

import java.util.HashMap;

import org.eclipse.bpel.ui.BPELMultipageEditorPart;
import org.eclipse.bpel.ui.simtech.properties.FileOperations;
import org.eclipse.bpel.ui.wizards.simtech.MetaDataWizard;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

/**
 * Is used to create a new revision of a BPEL project. Copies all files that
 * belong to the project of the opened BPEL file into a newly created revision
 * folder.
 * 
 * @author hahnml, sonntamo
 */
public class CopyProjectAction extends Action implements IEditorActionDelegate {

	private BPELMultipageEditorPart fEditor;

	public void setActiveEditor(IAction arg0, IEditorPart arg1) {

		if (arg1 instanceof BPELMultipageEditorPart) {
			fEditor = (BPELMultipageEditorPart) arg1;
		} else {
			fEditor = null;
		}
	}

	public void run(IAction arg0) {
		if (fEditor != null) {

			// Check if the editor file is in the ProcessFragments project
			if (!fEditor.getEditorFile().getProject().getName()
					.equals("ProcessFragments")) {
				/*
				 * At first we create a wizard that enables the specification of
				 * meta-data for the new process model revision. This meta-data
				 * is used to uniquely identify process models.
				 */
				HashMap<String, String> props = FileOperations
						.loadPropertiesFromDD(fEditor.getEditorFile()
								.getParent());

				MetaDataWizard wizard = new MetaDataWizard(props,
						fEditor.getEditorFile());
				wizard.init(fEditor.getSite().getWorkbenchWindow()
						.getWorkbench(), null);

				// Instantiates the wizard container with the wizard and opens
				// it
				WizardDialog dialog = new WizardDialog(fEditor.getSite()
						.getShell(), wizard);
				dialog.create();
				if (dialog.open() == Dialog.CANCEL)
					return;

				// Get the BPEL project and the parent folder
				IProject project = fEditor.getEditorFile().getProject();
				IContainer folder = fEditor.getEditorFile().getParent();

				String folderNameWithVersion = folder.getName();
				String folderName = folderNameWithVersion.substring(0,
						folderNameWithVersion.length() - 1);

				int version = 0;

				try {
					version = Integer
							.parseInt(String.valueOf(folderNameWithVersion
									.charAt(folderNameWithVersion.length() - 1)));
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}

				++version;
				String newFolderName = folderName + version;

				// Create a new folder in the project with number +1
				IFolder nextRevisionFolder = project.getFolder(newFolderName);
				while (nextRevisionFolder.exists()) {
					++version;
					newFolderName = folderName + version;
					nextRevisionFolder = project.getFolder(newFolderName);
				}

				if (!nextRevisionFolder.exists()) {
					try {
						// Create the new folder
						nextRevisionFolder.create(true, true, null);

						// Copy all files from the current revision folder to
						// the new
						// revision folder, if it doesn't exist yet
						for (IResource resource : folder.members()) {
							// We don't want to copy the log files
							if (!resource.getFileExtension().equals("log")) {
								IPath newFilePath = nextRevisionFolder
										.getFullPath();
								newFilePath = newFilePath.append(resource
										.getName());
								resource.copy(newFilePath, true, null);
							}
						}
					} catch (CoreException e) {
						e.printStackTrace();
						return;
					}
				}

				// Now, we store the properties for the new revision
				HashMap<String, String> newProps = wizard.getProperties();
				FileOperations
						.storePropertiesToDD(nextRevisionFolder, newProps);

				// Open the new revision with the BPEL editor
				// Use the name of the original BPEL file to get the copied one
				IFile newBpelFile = nextRevisionFolder.getFile(fEditor
						.getEditorFile().getName());

				try {
					// Get the active page
					IWorkbenchPage page = PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getActivePage();

					// Get the input resource object of the process model
					IEditorInput input = new FileEditorInput(newBpelFile);

					// Open the new process model file in the BPEL Editor
					page.openEditor(input, "org.eclipse.bpel.ui.bpeleditor");

				} catch (PartInitException e) {
					e.printStackTrace();
				}
			} else {
				MessageDialog
						.openInformation(Display.getDefault().getActiveShell(),
								"Action not available",
								"It is not possible to create revisions in the ProcessFragments project.");
			}
		}
	}

	@Override
	public void selectionChanged(IAction arg0, ISelection arg1) {
		// do nothing

	}
}