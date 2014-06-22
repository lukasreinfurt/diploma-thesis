package org.eclipse.bpel.ui.wizards.simtech;

import org.eclipse.bpel.ui.wizards.simtech.FragmentUtils.Conflict;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wst.wsdl.Namespace;

/**
 * Dialog to solve a namespace/prefix conflict
 * @author schrotbn
 *
 */
public class NamespaceConflictSolveDialog extends TitleAreaDialog {
		
	/**
	 * Textfield for the new prefix
	 */
	private Text fPrefixText;
	
	/**
	 * Textfield for the new namespace
	 */
	private Text fURIText;
	
	/**
	 * The new prefix
	 */
	private String newPrefix;
	
	/**
	 * The new namespace
	 */
	private String newNamespace;
	
	
	/**
	 * The corresponding conflict
	 */
	private Conflict conflict;
	
	
	public NamespaceConflictSolveDialog(Shell parentShell, Conflict conflict) {
		super(parentShell);
		this.conflict = conflict;
		Namespace ns = (Namespace)conflict.getObject();
		this.newPrefix = ns.getPrefix();
		this.newNamespace = ns.getURI();
	}
	
	@Override
	public void create() {
		setHelpAvailable(false);
		super.create();
		this.getShell().setText("Solve Namespace conflict");
		setTitle("Change the fragment Prefix/URI to solve this conflict");
		setMessage("Please specify the new prefix or URI for the conflicting fragment namespace.");
		
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

		Label prefixLabel = new Label(composite, SWT.NONE);
		prefixLabel.setText("New Prefix:");
		
		fPrefixText = new Text(composite, SWT.BORDER);
		fPrefixText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_FILL));
		
		fPrefixText.setText(this.newPrefix);

		Label URILabel = new Label(composite, SWT.NONE);
		URILabel.setText("New URI");
		
		fURIText = new Text(composite, SWT.BORDER);
		fURIText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL 
				| GridData.HORIZONTAL_ALIGN_FILL));
		fURIText.setText(this.newNamespace);
			
		
		composite.setLayoutData(gridData);
		
		return composite;
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID,
				IDialogConstants.OK_LABEL, true);

		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected void okPressed() { 
		newPrefix = fPrefixText.getText();
		newNamespace = fURIText.getText();
		
		Namespace ns = (Namespace)conflict.getObject();
		FragmentUtils.getUtils(null).changeNamespace(conflict, newPrefix, newNamespace, ns.getPrefix(), ns.getURI());
		
		close();
	}
	
}
