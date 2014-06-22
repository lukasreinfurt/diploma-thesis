package org.eclipse.bpel.ui.wizards.simtech;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IWorkbench;

/**
 * 
 * @author sonntamo
 */
public abstract class FragmentWizardPage extends WizardPage implements Listener {

	IWorkbench workbench;
	IStructuredSelection selection;
	WizardModel model;
	
	public FragmentWizardPage(IWorkbench workbench, IStructuredSelection selection, 
			WizardModel model, String pageName, String title, String description) {
		super(pageName);
		this.model = model;
		setTitle(title);
		setDescription(description);
		this.workbench = workbench;
		this.selection = selection;
	}
}
