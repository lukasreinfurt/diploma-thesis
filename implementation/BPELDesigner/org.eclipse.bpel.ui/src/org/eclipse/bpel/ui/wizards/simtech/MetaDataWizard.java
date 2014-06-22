package org.eclipse.bpel.ui.wizards.simtech;

import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * Wizard to specify meta-data properties for process models. The meta-data
 * is used to uniquely address different process model versions.
 * 
 * @author sonntamo
 */
public class MetaDataWizard extends Wizard implements INewWizard {

	// wizard pages
	NewFileWizardPageMetaData firstPage;

	// the workbench instance
	protected IWorkbench workbench;

	public MetaDataWizard(HashMap<String, String> metaDataProperties, IFile processFile) {
		super();
		firstPage = new NewFileWizardPageMetaData(
				"Meta-Data for new process version", null, metaDataProperties, processFile);
	}

	public void addPages() {
		addPage(firstPage);
	}

	/**
	 * Get the specified properties.
	 * 
	 * @return the user-specified properties
	 */
	public HashMap<String, String> getProperties() {
		return firstPage.getEntries();
	}

	@Override
	public boolean performFinish() {
		return true;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.workbench = workbench;
	}

	public boolean canFinish() {
		return firstPage.isPageComplete();
	}
}
