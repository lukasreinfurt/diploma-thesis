package org.eclipse.bpel.ui.wizards.simtech;

import org.eclipse.bpel.ui.wizards.simtech.table.DocumentTable;
import org.eclipse.bpel.ui.wizards.simtech.table.WSDLLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbench;

/**
 * Was muss man beim Einfügen von SimTech-Fragmenten beachten?
 * <ul>
 * <li>Schauen, ob die WSDLs schon existieren anhand von Name and TargetNS</li>
 * <li>Schauen, ob die XSDs schon existieren anhand von TargetNS</li>
 * <li>Die Variablen müssen in den Process-Scope geschoben werden, damit man sie
 * auch noch außerhalb des Fragmentes verwenden kann. Dazu muss man schauen, ob
 * die Variablen im Prozess schon existieren. Wenn ja, muss ein anderer Name
 * festgelegt werden. Der Wizard könnte abfragen, ob die Vars verschoben werden
 * sollen und wenn ja wohin.</li>
 * <li>Der Wizard könnte fragen, ob die Partner Links verschoben werden sollen
 * und wenn ja, wohin.</li>
 * <li>Man muss die WSDLs und XSDs im BPEL Prozess importieren</li>
 * </ul>
 * 
 * @author sonntamo
 */
public class PageWSDL extends FragmentWizardPage {

	public TableViewer getTableViewer() {
		return this.tableViewer;
	}

	public PageWSDL(IWorkbench workbench, IStructuredSelection selection,
			WizardModel model) {
		super(workbench, selection, model, "Page7", "Import WSDLs",
				"Select the WSDL files to be imported into the process");
	}

	TableViewer tableViewer;

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
		Table table = WSDLLabelProvider.createTable(composite);

		// Create and setup the TableViewer
		TableViewer tableViewer = WSDLLabelProvider.createTableViewer(table);
		DocumentTable docTable = new DocumentTable(DocumentTable.TableType.WSDL, tableViewer);
		
		//@hahnml: Register the current viewer in FragmentUtils and mark the existing files in the model
		FragmentUtils.getUtils(null).markExistingFiles(model.wsdls);
		
		tableViewer.setContentProvider(docTable.getContentProvider());
		tableViewer.setLabelProvider(docTable.getLabelProvider());

		// Set the cell modifier for the viewer
		tableViewer.setCellModifier(docTable.getCellModifier());


		// The input for the table viewer is the instance of ExampleTaskList
		tableViewer.setInput(model.wsdls);

		// set the composite as the control for this page
		setControl(composite);
	}

	/**
	 * @see Listener#handleEvent(Event)
	 */
	public void handleEvent(Event event) {
//		setPageComplete(isPageComplete());
		getWizard().getContainer().updateButtons();
	}

	/**
	 * @see IWizardPage#canFlipToNextPage()
	 */
	public boolean canFlipToNextPage() {
		return true;
	}

}
