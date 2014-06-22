package org.eclipse.bpel.ui.wizards.simtech;

import org.eclipse.bpel.model.BPELPackage;
import org.eclipse.bpel.ui.wizards.simtech.FragmentUtils.Conflict;
import org.eclipse.bpel.ui.wizards.simtech.table.ConflictTable;
import org.eclipse.bpel.ui.wizards.simtech.table.NamespaceConflictTableLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbench;
import org.eclipse.wst.wsdl.WSDLPackage;

public class PageNamespaceConflicts extends FragmentWizardPage {

	private TableViewer viewer;
	
	public PageNamespaceConflicts(IWorkbench workbench,
			IStructuredSelection selection, WizardModel model) {
		super(workbench, selection, model, "Page5", "Handle Namespace/Prefix conflicts", "Solve Namespace/Prefix conflicts");
	}

	@Override
	public void createControl(Composite parent) {

		// create the composite to hold the widgets
		Composite composite = new Composite(parent, SWT.NULL);

		// Create a composite to hold the children
		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.FILL_BOTH);
		composite.setLayoutData(gridData);

		// Set numColumns to 4 for the buttons
		GridLayout layout = new GridLayout(4, true);
		layout.marginWidth = 4;
		composite.setLayout(layout);

		// Create the table
		Table table = NamespaceConflictTableLabelProvider.createTable(composite);

		// Create and setup the TableViewer
		final TableViewer tableViewer = NamespaceConflictTableLabelProvider
				.createTableViewer(table);
		ConflictTable confTable = new ConflictTable(
				ConflictTable.TableType.NAMESPACE, tableViewer);

		// @hahnml: Register the current viewer in FragmentUtils and this page
		// as listener
		FragmentUtils.getUtils(null).setCurrentListener(this);

		tableViewer.setContentProvider(confTable.getContentProvider());
		tableViewer.setLabelProvider(confTable.getLabelProvider());

		// Set the input for the TableViewer
		tableViewer.setInput(FragmentUtils.getUtils(null).getConflictsOfType(
				WSDLPackage.Literals.NAMESPACE));

		this.viewer = tableViewer;

		Composite buttonComp = new Composite(composite, SWT.NULL);
		FillLayout fillLay = new FillLayout(SWT.HORIZONTAL);
		fillLay.spacing = 5;
		buttonComp.setLayout(fillLay);
		buttonComp.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true,
				false, 3, 1));

		Button solveButton = new Button(buttonComp, SWT.PUSH);
		solveButton.setText("Solve conflict");
		solveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ISelection selection = tableViewer.getSelection();
				if (selection != null
						&& selection instanceof IStructuredSelection) {

					IStructuredSelection sel = (IStructuredSelection) selection;

					if (sel.size() == 1) {
						Conflict conflict = (Conflict) sel.getFirstElement();

						NamespaceConflictSolveDialog dialog = new NamespaceConflictSolveDialog(
								tableViewer.getTable().getShell(), conflict);
						dialog.open();
					}

					// Update the table and the wizard state
					tableViewer.refresh();
					getWizard().getContainer().updateButtons();
				}
			}
		});


		// set the composite as the control for this page
		setControl(composite);
	}
	
	/**
	 * @see Listener#handleEvent(Event)
	 */
	@Override
	public void handleEvent(Event event) {
		if (event.text.equals("Refresh")) {
			this.viewer.refresh();
		}
		
		getWizard().getContainer().updateButtons();
	}

	/**
	 * @see IWizardPage#canFlipToNextPage()
	 */
	public boolean canFlipToNextPage() {
		return !FragmentUtils.getUtils(null).hasConflictsOfType(
				WSDLPackage.Literals.NAMESPACE);
	}

	// @vonstepk This can probably be removed
	//@Override
	//public boolean isPageComplete() {
	//	return !(FragmentUtils.getUtils(null).hasConflictsOfType(WSDLPackage.Literals.NAMESPACE));
	//}
}
