package org.eclipse.bpel.ui.parameters.ui.composite;

import java.util.Date;

import org.eclipse.bpel.model.Variable;
import org.eclipse.bpel.ui.parameters.ui.ParameterDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

/**
 * If a date time variable is selected as a parameter
 * this entry is built and inserted in the dialog
 * 
 * @author tolevar
 *
 */
public class DateTimeEntry extends DialogEntryComponent {
	
	private Label dateLabel = null;
	private DateTime dateTime = null;
	
	private Label toLabel = null;
	private DateTime toDateTime = null;
	
	private Spinner stepSpinner = null;
	
	private Button rangeButton = null;
	private Button deleteButton = null;
	
	//@hahnml
	private ParameterDialog parameterDialog = null;

	public DateTimeEntry(Composite parent, int style, Variable variable, ParameterDialog dialog) {
		super(parent, style, variable);
		this.parameterDialog = dialog;
		
		this.setLayout(new GridLayout(6, false));
		this.layout();
		
		GridData dateTimeData = new GridData();
		dateTimeData.horizontalAlignment = SWT.FILL;
		dateTimeData.horizontalSpan = 2;
		dateTimeData.grabExcessHorizontalSpace = true;
		this.setLayoutData(dateTimeData);
	}
	
	@Override
	public String[] getValues() {
		String[] values;
		if (toDateTime != null) {
			Date fromDate = new Date(getDateTime().getYear(), getDateTime().getMonth(), getDateTime().getDay());
			Date toDate = new Date(toDateTime.getYear(), toDateTime.getMonth(), toDateTime.getDay());
			int range = stepSpinner.getSelection();
			//Calculate the difference between the two dates and calculate
			//how many date values have to be saved
			long diff = toDate.getTime() - fromDate.getTime();
			int difference;
			difference =(((int) diff / (1000 * 60 * 60 * 24)) / range) + 1;
			
			values = new String[difference];
			for (int i = 0; i < values.length; i++) {
				fromDate = new Date(getDateTime().getYear(), getDateTime().getMonth(), (getDateTime().getDay() + (i * range)));
				String stringDate = fromDate.getYear() + "-" + (fromDate.getMonth() + 1) + "-" + fromDate.getDate();
				values[i] = stringDate;
			}
		} else {
			values = new String[1];
			String date = getDateTime().getYear() + "-" + (getDateTime().getMonth() + 1) + "-" + getDateTime().getDay();
			values[0] = date;
		}
		
		return values;
	}	
	
	@Override
	public void buildEntry(String variableName) {
		dateLabel = new Label(this, SWT.NONE);
		dateLabel.setText(variableName);
		GridData dateLabelData = new GridData();
		dateLabelData.horizontalAlignment = SWT.FILL;
		dateLabelData.widthHint = 50;
		dateLabel.setLayoutData(dateLabelData);
		if (variable.getDocumentation() != null)
			dateLabel.setToolTipText(variable.getDocumentation().getValue());
		
		dateTime = new DateTime(this, SWT.DATE);
		GridData dateTimeData = new GridData();
		dateTimeData.horizontalAlignment = SWT.FILL;
		dateTime.setLayoutData(dateTimeData);
		
		rangeButton = new Button(this, SWT.PUSH | SWT.CENTER);
		GridData rangeButtonData = new GridData();
		rangeButtonData.horizontalAlignment = SWT.FILL;
		rangeButtonData.horizontalSpan = 3;
		rangeButton.setLayoutData(rangeButtonData);
		rangeButton.setText("Range");
		rangeButton.addSelectionListener(new RangeButtonListener());
	}
	
	@Override
	public void buildAdditionEntry() {
		rangeButton.dispose();
		rangeButton = null;
		
		toLabel = new Label(this, SWT.NONE);
		toLabel.setText("till");
		
		toDateTime = new DateTime(this, SWT.DATE);
		GridData toDateTimeData = new GridData();
		toDateTimeData.horizontalAlignment = SWT.FILL;
		toDateTime.setLayoutData(toDateTimeData);
		
		stepSpinner = new Spinner(this, SWT.BORDER);
		GridData stepSpinnerData = new GridData();
		stepSpinnerData.horizontalAlignment = SWT.FILL;
		stepSpinner.setMinimum(0);
		stepSpinner.setMaximum(1000);
		stepSpinner.setPageIncrement(50);
		stepSpinner.setLayoutData(stepSpinnerData);
		stepSpinner.setToolTipText("Sets the step range for the date");
		
		deleteButton = new Button(this, SWT.PUSH | SWT.CENTER);
		GridData deleteButtonData = new GridData();
		deleteButtonData.horizontalAlignment = SWT.FILL;
		deleteButton.setLayoutData(deleteButtonData);
		deleteButton.setText("Delete");
		deleteButton.addSelectionListener(new DeleteButtonListener());
		
		//Resize the size of the window and update the view
		this.layout();
		this.redraw();
		this.update();
				
		Display.getCurrent().update();
				
		this.parameterDialog.resizeHeight();
		this.parameterDialog.resizeWidth();
		
	}

	private DateTime getDateTime() {
		return dateTime;
	}
	
	private class DeleteButtonListener implements SelectionListener {

		@Override
		public void widgetDefaultSelected(SelectionEvent arg0) {
			widgetSelected(arg0);
		}

		@Override
		public void widgetSelected(SelectionEvent arg0) {
			deleteButton.dispose();
			deleteButton = null;
			stepSpinner.dispose();
			stepSpinner = null;
			toDateTime.dispose();
			toDateTime = null;
			toLabel.dispose();
			toLabel = null;
			
			rangeButton = new Button(DateTimeEntry.this, SWT.PUSH | SWT.CENTER);
			GridData rangeButtonData = new GridData();
			rangeButtonData.horizontalAlignment = SWT.FILL;
			rangeButtonData.horizontalSpan = 3;
			rangeButton.setLayoutData(rangeButtonData);
			rangeButton.setText("Range");
			rangeButton.addSelectionListener(new RangeButtonListener());
			
			//Resize the size of the window and update the view
			DateTimeEntry.this.layout();
			DateTimeEntry.this.redraw();
			DateTimeEntry.this.update();
					
			Display.getCurrent().update();
					
			parameterDialog.resizeHeight();
			parameterDialog.resizeWidth();
		}
	}
	
	private class RangeButtonListener implements SelectionListener {

		@Override
		public void widgetDefaultSelected(SelectionEvent arg0) {
			widgetSelected(arg0);
		}

		@Override
		public void widgetSelected(SelectionEvent arg0) {
			buildAdditionEntry();
		}
		
	}
}
