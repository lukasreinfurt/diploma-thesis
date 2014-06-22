package org.eclipse.bpel.ui.wizards.simtech;

import org.eclipse.bpel.ui.wizards.simtech.table.DocumentTable;
import org.eclipse.bpel.ui.wizards.simtech.table.XSDLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbench;

/**
 * 
 * @author sonntamo
 */
public class PageXSD extends FragmentWizardPage implements Listener {

	public PageXSD(IWorkbench workbench, IStructuredSelection selection,
			WizardModel model) {
		super(workbench, selection, model, "Page8", "Import XSDs",
				"Select the XSD files to be imported into the process");
	}

	public void createControl(Composite parent) {

		// create the composite to hold the widgets
		Composite composite = new Composite(parent, SWT.NULL);

		// Create a composite to hold the children
		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.FILL_BOTH);
		composite.setLayoutData(gridData);

		// Set numColumns to 3 for the buttons
		GridLayout layout = new GridLayout(4, true);
		layout.marginWidth = 4;
		composite.setLayout(layout);

		// Create the table
		Table table = XSDLabelProvider.createTable(composite);

		// Create and setup the TableViewer
		TableViewer tableViewer = XSDLabelProvider.createTableViewer(table);
		DocumentTable docTable = new DocumentTable(DocumentTable.TableType.XSD,
				tableViewer);
		
		//@hahnml: Register the current viewer in FragmentUtils and mark the existing files in the model
		FragmentUtils.getUtils(null).markExistingFiles(model.xsds);

		tableViewer.setContentProvider(docTable.getContentProvider());
		tableViewer.setLabelProvider(docTable.getLabelProvider());

		// Set the cell modifier for the viewer
		tableViewer.setCellModifier(docTable.getCellModifier());

		// The input for the table viewer is the instance of ExampleTaskList
		tableViewer.setInput(model.xsds);

		// set the composite as the control for this page
		setControl(composite);
		setPageComplete(true);
	}

	public boolean canFlipToNextPage() {
		// no next page for this path through the wizard
		// @schrotbn needed next page to solve possible Namespace/Prefix conflicts
		return true;
	}

	/*
	 * Process the events: when the user has entered all information the wizard
	 * can be finished
	 */
	public void handleEvent(Event e) {

		setPageComplete(isPageComplete());
		getWizard().getContainer().updateButtons();
	}

	/*
	 * Sets the completed field on the wizard class when all the information is
	 * entered and the wizard can be completed
	 */
	public boolean isPageComplete() {
		return true;
	}

}
