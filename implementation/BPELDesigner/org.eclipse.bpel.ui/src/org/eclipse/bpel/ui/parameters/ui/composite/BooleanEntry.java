package org.eclipse.bpel.ui.parameters.ui.composite;

import org.eclipse.bpel.model.Variable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * If a boolean variable is selected as a parameter
 * this entry is built and inserted in the dialog
 * 
 * @author tolevar
 *
 */
public class BooleanEntry extends DialogEntryComponent {
	
	private Label booleanLabel;
	private Button booleanButton;
	
	public BooleanEntry(Composite parent, int style, Variable variable) {
		super(parent, style, variable);
		
		this.setLayout(new GridLayout(2, false));
		this.layout();
		
		GridData booleanData = new GridData();
		booleanData.horizontalAlignment = SWT.FILL;
		booleanData.grabExcessHorizontalSpace = true;
		booleanData.horizontalSpan = 2;
		this.setLayoutData(booleanData);
	}
	
	@Override
	public String[] getValues() {
		String[] values = new String[1];
		values[0] = String.valueOf(booleanButton.getSelection());
		return values;
	}
	
	@Override
	public void buildEntry(String variableName) {
		booleanLabel = new Label(this, SWT.NONE);
		booleanLabel.setText(variableName);
		GridData booleanLabelData = new GridData();
		booleanLabelData.horizontalAlignment = SWT.FILL;
		booleanLabelData.widthHint = 50;
		booleanLabel.setLayoutData(booleanLabelData);
		if (variable.getDocumentation() != null)
			booleanLabel.setToolTipText(variable.getDocumentation().getValue());
		
		booleanButton = new Button(this, SWT.CHECK | SWT.LEFT);
		GridData booleanButtonData = new GridData();
		booleanButtonData.horizontalAlignment = GridData.FILL;
		booleanButton.setLayoutData(booleanButtonData);
		booleanButton.setText("Sets the value of " +variableName+" to true or false");
	}
}
