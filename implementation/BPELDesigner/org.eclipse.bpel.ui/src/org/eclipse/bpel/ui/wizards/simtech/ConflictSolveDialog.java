package org.eclipse.bpel.ui.wizards.simtech;

import org.eclipse.bpel.model.BPELExtensibleElement;
import org.eclipse.bpel.model.BPELPackage;
import org.eclipse.bpel.model.CorrelationSet;
import org.eclipse.bpel.model.MessageExchange;
import org.eclipse.bpel.model.PartnerLink;
import org.eclipse.bpel.model.Variable;
import org.eclipse.bpel.ui.wizards.simtech.FragmentUtils.Conflict;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ConflictSolveDialog extends TitleAreaDialog {

	private Label fNameLabel;
	private Text fNameText;

	Conflict conflict = null;
	BPELExtensibleElement object = null;
	String newName = "";
	
	EStructuralFeature nameFeature = null;
	
	public String getNewName() {
		return newName;
	}

	public ConflictSolveDialog(Shell parentShell, Conflict conflict) {
		super(parentShell);
		this.conflict = conflict;
		this.object = (BPELExtensibleElement)this.conflict.getObject();

		if (this.object instanceof Variable) {
			this.nameFeature = BPELPackage.Literals.VARIABLE__NAME;
		} else if (this.object instanceof PartnerLink) {
			this.nameFeature = BPELPackage.Literals.PARTNER_LINK__NAME;
		} else if (this.object instanceof CorrelationSet) {
			this.nameFeature = BPELPackage.Literals.CORRELATION_SET__NAME;
		} else if (this.object instanceof MessageExchange) {
			this.nameFeature = BPELPackage.Literals.MESSAGE_EXCHANGE__NAME;
		}
	}

	@Override
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);
		setTitle("Solve the conflicts of the EObject");
		setMessage("Change the name of the EObject to solve the conflict",
				IMessageProvider.INFORMATION);
		return contents;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;

		fNameLabel = new Label(composite, SWT.NONE);
		fNameLabel.setText("New name:");
		
		fNameText = new Text(composite, SWT.BORDER);
		fNameText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_FILL));
		
		fNameText.setText(this.object.eGet(this.nameFeature).toString());

		composite.setLayoutData(gridData);
		
		return composite;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		Button okay = createButton(parent, IDialogConstants.IGNORE_ID,
				IDialogConstants.OK_LABEL, true);

		okay.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				newName = fNameText.getText();
				
				FragmentUtils.getUtils(null).changeElementName(conflict, fNameText.getText());
				
				close();
			}
		});

		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	public boolean isHelpAvailable() {
		return false;
	}

}
