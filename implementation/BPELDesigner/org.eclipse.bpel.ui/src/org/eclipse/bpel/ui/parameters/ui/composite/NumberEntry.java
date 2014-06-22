package org.eclipse.bpel.ui.parameters.ui.composite;

import org.eclipse.bpel.model.Variable;
import org.eclipse.bpel.ui.parameters.ui.ParameterDialog;
import org.eclipse.bpel.ui.util.BPELUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

/**
 * If a number (integer, decimal, double, float) variable is 
 * selected as a parameter this entry is built and inserted in the dialog
 * 
 * @author tolevar
 *
 */
public class NumberEntry extends DialogEntryComponent {
	
	//@hahnml
	private ParameterDialog dialog = null;
	
	private Label numberLabel;
	private Spinner fromSpinner;
	
	private Button addButton;
	private Button deleteButton;
	
	private Label toLabel;
	private Spinner toSpinner;
	private Spinner stepsSpinner;
	private int defaultValue;
	
	public NumberEntry(Composite parent, int style, Variable variable, ParameterDialog dialog) {
		super(parent, style, variable);
	
		this.dialog = dialog;
		
		this.setLayout(new GridLayout(6, false));
		this.layout();
		
		GridData numberData = new GridData();
		numberData.horizontalAlignment = SWT.FILL;
		numberData.grabExcessHorizontalSpace = true;
		numberData.horizontalSpan = 2;
		this.setLayoutData(numberData);
		
		String selection = this.dialog.getParameterHandler().getDefaultValue(variable); 
		defaultValue = "".equals(selection)?0:Integer.valueOf(selection);
	}
	
	public String[] getValues() {
		String[] values;
		if (toSpinner == null) {
			values = new String[1];
			values[0] = fromSpinner.getText();
		} else {
			int from  = Integer.valueOf(fromSpinner.getText());
			int to = Integer.valueOf(toSpinner.getText());
			int elements = (to - from) + 1;
			int interval = stepsSpinner.getSelection();
			int amount;
			if (interval > 1) {
				amount = elements / interval + 1;
			} else {
				amount = elements / interval;
			}
			values = new String[amount];
			for (int j = 0; j < amount; j++) {
				values[j] = String.valueOf(from + (j*interval));
			}
		}
		return values;
	}
	
	public void buildEntry(String variableName) {
		numberLabel = new Label(this, SWT.NONE);
		numberLabel.setText(variableName);
		GridData numberLabelData = new GridData();
		numberLabelData.horizontalAlignment = SWT.FILL;
		numberLabelData.widthHint = 50;
		numberLabelData.widthHint = BPELUtil.calculateLabelWidth(numberLabel, 150);
		numberLabel.setLayoutData(numberLabelData);
		if (variable.getDocumentation() != null)
			numberLabel.setToolTipText(variable.getDocumentation().getValue());
		
		fromSpinner = new Spinner(this, SWT.BORDER);
		GridData fromSpinnerData = new GridData();
		fromSpinnerData.horizontalAlignment = SWT.FILL;
		fromSpinner.setMinimum(0);
		fromSpinner.setMaximum(100000);
		fromSpinner.setSelection(defaultValue);
		fromSpinner.setIncrement(1);
		fromSpinner.setPageIncrement(100);
		fromSpinner.setLayoutData(fromSpinnerData);
		fromSpinner.setToolTipText("The minimum value the process should start with");
		
		addButton = new Button(this, SWT.PUSH | SWT.CENTER);
		addButton.setText("Range");
		GridData addButtonData = new GridData();
		addButtonData.horizontalAlignment = SWT.FILL;
		addButtonData.horizontalSpan = 2;
		addButton.setLayoutData(addButtonData);
		addButton.addSelectionListener(new AddListener());
	}
	
	@Override
	public void buildAdditionEntry() {
		addButton.dispose();
		addButton = null;
		
		toLabel = new Label(this, SWT.NONE);
		toLabel.setText("to");
		
		toSpinner = new Spinner(this, SWT.BORDER);
		GridData toSpinnerData = new GridData();
		toSpinnerData.horizontalAlignment = SWT.FILL;
		toSpinner.setMinimum(0);
		toSpinner.setMaximum(100000);
		toSpinner.setSelection(defaultValue);
		toSpinner.setIncrement(1);
		toSpinner.setPageIncrement(100);
		toSpinner.setLayoutData(toSpinnerData);
		toSpinner.setToolTipText("The maximum value the process should start with");
		
		stepsSpinner = new Spinner(this, SWT.BORDER);
		GridData stepsSpinnerData = new GridData();
		stepsSpinnerData.horizontalAlignment = SWT.FILL;
		stepsSpinner.setMaximum(100000);
		// @hahnml: Changed minimum value of the interval to 1
		stepsSpinner.setMinimum(1);
		stepsSpinner.setPageIncrement(100);
		stepsSpinner.setLayoutData(stepsSpinnerData);
		stepsSpinner.setToolTipText("Sets the step range");
				
		deleteButton = new Button(this, SWT.PUSH | SWT.CENTER);
		deleteButton.setText("Delete");
		GridData deleteButtonData = new GridData();
		deleteButtonData.horizontalAlignment = SWT.FILL;
		deleteButton.setLayoutData(deleteButtonData);
		deleteButton.addSelectionListener(new DeleteListener()); 
		
		//Resize the size of the window and update the view
		this.layout();
		this.redraw();
		this.update();
		
		Display.getCurrent().update();
		
		this.dialog.resizeHeight();
		this.dialog.resizeWidth();
	}
	
	/**
	 * A listener for adding a new spinner so the user can choose 
	 * a number range
	 * 
	 * @author tolevar
	 *
	 */
	private class AddListener implements SelectionListener {

		@Override
		public void widgetDefaultSelected(SelectionEvent arg0) {
			widgetSelected(arg0);
		}

		@Override
		public void widgetSelected(SelectionEvent arg0) {
			buildAdditionEntry();
		}
	}
	
	/**
	 * Deletes the additional entry
	 * 
	 * @author tolevar
	 *
	 */
	private class DeleteListener implements SelectionListener {

		@Override
		public void widgetDefaultSelected(SelectionEvent arg0) {
			widgetSelected(arg0);
		}

		@Override
		public void widgetSelected(SelectionEvent arg0) {
			deleteButton.dispose();
			toLabel.dispose();
			toLabel = null;
			toSpinner.dispose();
			toSpinner = null;
			stepsSpinner.dispose();
			stepsSpinner = null;
			
			addButton = new Button(NumberEntry.this, SWT.PUSH | SWT.CENTER);
			addButton.setText("Range...");
			GridData addButtonData = new GridData();
			addButtonData.horizontalAlignment = SWT.FILL;
			addButtonData.horizontalSpan = 2;
			addButton.setLayoutData(addButtonData);
			addButton.addSelectionListener(new AddListener());
			
			fromSpinner.setIncrement(1);
			
			NumberEntry.this.layout();
			NumberEntry.this.redraw();
			NumberEntry.this.update();
			
			Display.getCurrent().update();
			
			dialog.resizeHeight();
			dialog.resizeWidth();
		}
	}
}
