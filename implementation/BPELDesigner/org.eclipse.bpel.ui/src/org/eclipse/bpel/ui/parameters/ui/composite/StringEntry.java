package org.eclipse.bpel.ui.parameters.ui.composite;

import java.util.ArrayList;

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
import org.eclipse.swt.widgets.Text;

/**
 * If a string variable is selected as a parameter
 * this entry is built and inserted in the dialog
 * 
 * @author tolevar
 *
 */
public class StringEntry extends DialogEntryComponent {
	
	private ArrayList<Text> textList = new ArrayList<Text>();
		
	//@hahnml
	private ParameterDialog dialog = null;
	
	private Label stringLabel;
	private Text stringText;
	
	private Button addButton;
	
	private String defaultValue;

	public StringEntry(Composite parent, int style, Variable variable, ParameterDialog dialog) {
		super(parent, style, variable);
		this.dialog = dialog;
		
		this.setLayout(new GridLayout(3, false));
		GridData stringData = new GridData();
		stringData.horizontalAlignment = SWT.FILL;
		stringData.grabExcessHorizontalSpace = true;
		stringData.horizontalSpan = 2;
		this.setLayoutData(stringData);
		defaultValue = this.dialog.getParameterHandler().getDefaultValue(this.variable);
	}
	
	@Override
	public String[] getValues() {
		String[] values = new String[textList.size()];
		for (int i = 0; i < values.length; i++) {
			values[i] = textList.get(i).getText();
		}
		return values;
	}
	
	@Override
	public void buildEntry(String variableName) {
		stringLabel = new Label(this, SWT.NONE);
		stringLabel.setText(variableName);
		GridData stringLabelData = new GridData();
		stringLabelData.horizontalAlignment = SWT.FILL;
		stringLabelData.widthHint = BPELUtil.calculateLabelWidth(stringLabel, 150);
		stringLabel.setLayoutData(stringLabelData);
		if (variable.getDocumentation() != null)
			stringLabel.setToolTipText(variable.getDocumentation().getValue());
		
		stringText = new Text(this, SWT.BORDER);
		GridData stringTextData = new GridData();
		stringTextData.horizontalAlignment = SWT.FILL;
		stringTextData.grabExcessHorizontalSpace = true;
		stringText.setLayoutData(stringTextData);
		stringText.setText(defaultValue);
		textList.add(stringText);
		
		addButton = new Button(this, SWT.PUSH | SWT.CENTER);
		addButton.setText("Range");
		GridData addButtonData = new GridData();
		addButtonData.horizontalAlignment = SWT.FILL;
		addButton.setLayoutData(addButtonData);
		addButton.addSelectionListener(new AddListener());
	}
	
	@Override
	public void buildAdditionEntry() {
		Text newText = new Text(this, SWT.BORDER);
		GridData newTextData = new GridData();
		newTextData.horizontalAlignment = SWT.FILL;
		newTextData.grabExcessHorizontalSpace = true;
		newTextData.horizontalSpan = 2;
		newTextData.horizontalIndent = 5 + BPELUtil.calculateLabelWidth(stringLabel, 150);
		newText.setLayoutData(newTextData);
		newText.setText(defaultValue);
		
		Button deleteButton = new Button(this, SWT.PUSH | SWT.CENTER);
		deleteButton.setText("Delete");
		GridData deleteButtonData = new GridData();
		deleteButtonData.horizontalAlignment = SWT.FILL;
		deleteButton.setLayoutData(deleteButtonData);
		deleteButton.addSelectionListener(new DeleteListener(newText, deleteButton));
		
		textList.add(newText);
		
		this.layout();
		this.redraw();
		this.update();
		
		this.getParent().layout();
		this.getParent().redraw();
		this.getParent().update();

		Display.getCurrent().update();
		
		this.dialog.resizeHeight();
		this.dialog.resizeWidth();
	}
	
	/**
	 * A listener for adding a new string entry
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
	 * Deletes an additional string entry
	 * 
	 * @author tolevar
	 *
	 */
	private class DeleteListener implements SelectionListener {
		
		private Text text;
		private Button deleteButton;
		
		public DeleteListener(Text text, Button deleteButton) {
			this.text = text;
			this.deleteButton = deleteButton;
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent arg0) {
			widgetSelected(arg0);
		}

		@Override
		public void widgetSelected(SelectionEvent arg0) {
			this.text.dispose();
			this.deleteButton.dispose();
			
			StringEntry.this.textList.remove(this.text);
			
			StringEntry.this.layout();
			StringEntry.this.redraw();
			StringEntry.this.update();
			
			Display.getCurrent().update();
			
			dialog.resizeHeight();
			dialog.resizeWidth();
		}
		
	}
}
