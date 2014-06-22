package org.eclipse.bpel.ui.parameters.ui.composite;

import java.util.ArrayList;

import org.eclipse.bpel.model.Variable;
import org.eclipse.bpel.ui.parameters.ui.ParameterDialog;
import org.eclipse.bpel.ui.parameters.ui.SelectRMFileDialog;
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

public class RMFileEntry extends DialogEntryComponent {

	private ArrayList<Text> fileList = new ArrayList<Text>();
	private Label fileLabel;
	private Text fileText;
	private Button addButton;

	private ParameterDialog dialog = null;
	
	private String defaultValue;

	public RMFileEntry(Composite parent, int style, Variable variable,
			ParameterDialog dialog) {
		super(parent, style, variable);

		this.dialog = dialog;
		this.setLayout(new GridLayout(4, false));
		this.layout();

		GridData fileData = new GridData();
		fileData.horizontalAlignment = SWT.FILL;
		fileData.grabExcessHorizontalSpace = true;
		fileData.horizontalSpan = 2;
		this.setLayoutData(fileData);
		defaultValue = this.dialog.getParameterHandler().getDefaultValue(this.variable);
	}

	@Override
	public String[] getValues() {
		String[] values = new String[fileList.size()];
		for (int i = 0; i < values.length; i++) {
			String path = fileList.get(i).getText();
			values[i] = path;
		}
		return values;
	}

	@Override
	public void buildEntry(String variableName) {
		fileLabel = new Label(this, SWT.NONE);
		fileLabel.setText(variableName);
		GridData fileLabelData = new GridData();
		fileLabelData.horizontalAlignment = SWT.FILL;
		fileLabelData.widthHint = BPELUtil.calculateLabelWidth(fileLabel, 150);
		fileLabel.setLayoutData(fileLabelData);
		if (variable.getDocumentation() != null)
			fileLabel.setToolTipText(variable.getDocumentation().getValue());

		fileText = new Text(this, SWT.BORDER);
		fileText.setEnabled(false);
		GridData fileTextData = new GridData();
		fileTextData.horizontalAlignment = SWT.FILL;
		fileTextData.grabExcessHorizontalSpace = true;
		fileText.setLayoutData(fileTextData);
		fileText.setText(defaultValue);
		fileList.add(fileText);

		Button selectButton = new Button(this, SWT.PUSH | SWT.CENTER);
		selectButton.setText("Select");
		GridData selectButtonData = new GridData();
		selectButtonData.horizontalAlignment = SWT.FILL;
		selectButton.setLayoutData(selectButtonData);
		selectButton.addSelectionListener(new SelectListener(fileText));

		// new SelectionListener() {
		//
		// @Override
		// public void widgetSelected(SelectionEvent e) {
		// SelectRMFileDialog selectFileDialog = new
		// SelectRMFileDialog(getShell(), SWT.NONE);
		// selectFileDialog.buildShell();
		// selectFileDialog.open();
		// }
		//
		// @Override
		// public void widgetDefaultSelected(SelectionEvent e) {
		// widgetSelected(e);
		// }
		//
		// });

		addButton = new Button(this, SWT.PUSH | SWT.CENTER);
		addButton.setText("Range");
		GridData addButtonData = new GridData();
		addButtonData.horizontalAlignment = SWT.FILL;
		addButton.setLayoutData(addButtonData);
		addButton.addSelectionListener(new AddListener());
	}

	@Override
	public void buildAdditionEntry() {
		Text newFile = new Text(this, SWT.BORDER);
		newFile.setEnabled(false);
		GridData newFileData = new GridData();
		newFileData.horizontalAlignment = SWT.FILL;
		newFileData.grabExcessHorizontalSpace = true;
		newFileData.horizontalSpan = 2;
		newFileData.horizontalIndent = 5 + BPELUtil.calculateLabelWidth(fileLabel, 150);
		newFile.setLayoutData(newFileData);
		newFile.setText(defaultValue);
		if (variable.getDocumentation() != null)
			newFile.setToolTipText(variable.getDocumentation().getValue());

		Button selectButton = new Button(this, SWT.PUSH | SWT.CENTER);
		selectButton.setText("Select");
		GridData selectButtonData = new GridData();
		selectButtonData.horizontalAlignment = SWT.FILL;
		selectButton.setLayoutData(selectButtonData);
		selectButton.addSelectionListener(new SelectListener(newFile));

		Button deleteButton = new Button(this, SWT.PUSH | SWT.CENTER);
		deleteButton.setText("Delete");
		GridData deleteButtonData = new GridData();
		deleteButtonData.horizontalAlignment = SWT.FILL;
		deleteButton.setLayoutData(deleteButtonData);
		deleteButton.addSelectionListener(new DeleteListener(newFile,
				deleteButton, selectButton));

		fileList.add(newFile);

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

	private class SelectListener implements SelectionListener {

		private Text text;

		public SelectListener(Text text) {
			this.text = text;
		}
		
		@Override
		public void widgetSelected(SelectionEvent e) {
			SelectRMFileDialog selectFileDialog = new SelectRMFileDialog(
					text, getShell(), SWT.NONE);
			selectFileDialog.buildShell();
			selectFileDialog.open();
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			widgetSelected(e);

		}

	}

	/**
	 * A listener for adding a new file entry
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
	 */
	private class DeleteListener implements SelectionListener {

		private Text text;
		private Button deleteButton;
		private Button selectButton;

		public DeleteListener(Text text, Button deleteButton, Button selectButton) {
			this.text = text;
			this.deleteButton = deleteButton;
			this.selectButton = selectButton;
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent arg0) {
			widgetSelected(arg0);
		}

		@Override
		public void widgetSelected(SelectionEvent arg0) {
			this.text.dispose();
			this.deleteButton.dispose();
			this.selectButton.dispose();

			RMFileEntry.this.fileList.remove(this.text);

			RMFileEntry.this.layout();
			RMFileEntry.this.redraw();
			RMFileEntry.this.update();

			Display.getCurrent().update();

			dialog.resizeHeight();
			dialog.resizeWidth();
		}

	}
}
