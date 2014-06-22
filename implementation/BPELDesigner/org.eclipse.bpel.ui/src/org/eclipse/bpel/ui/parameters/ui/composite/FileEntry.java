package org.eclipse.bpel.ui.parameters.ui.composite;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.eclipse.bpel.model.Variable;
import org.eclipse.bpel.ui.parameters.ui.ParameterDialog;
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

import javax.xml.bind.DatatypeConverter;

public class FileEntry extends DialogEntryComponent {
	
	private ArrayList<Text> fileList = new ArrayList<Text>();
	private Label fileLabel;
	private Text fileText;
	private Button fileButton;
	private Button addButton;
	
	private ParameterDialog dialog = null;
	
	public FileEntry(Composite parent, int style, Variable variable, ParameterDialog dialog) {
		super(parent, style, variable);

		this.dialog = dialog;
		this.setLayout(new GridLayout(2, false));
		this.layout();
		
		this.setLayout(new GridLayout(3, false));
		GridData fileData = new GridData();
		fileData.horizontalAlignment = SWT.FILL;
		fileData.grabExcessHorizontalSpace = true;
		fileData.horizontalSpan = 2;
		this.setLayoutData(fileData);
	}

	private byte[] readFile(File file) throws IOException {
		InputStream is = new FileInputStream(file);
		
		long length = file.length();
		if (length > Integer.MAX_VALUE)
			throw new IOException("Input file too long: " + file.getName());
		
		byte[] bytes = new byte[(int)length];
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length-offset)) >= 0) {
			offset += numRead;
		}
		
		if (offset < bytes.length)
			throw new IOException("Unable to read complete file: " + file.getName());
		
		is.close();
		return bytes;
	}
	
	private String encodeFile(byte[] bytes) {
		// @hahnml: Changed to javax.xml.bind.DatatypeConverter due to access restrictions on previously used sun.misc.BASE64Encoder.
		String result = DatatypeConverter.printBase64Binary(bytes);
		return result;
	}
	
	private byte[] decodeFile(String encodedFile) throws IOException {
		// @hahnml: Changed to javax.xml.bind.DatatypeConverter due to access restrictions on previously used sun.misc.BASE64Decoder.
		return DatatypeConverter.parseBase64Binary(encodedFile);
	}
	
	@Override
	public String[] getValues() {
		String[] values = new String[fileList.size()];
		for (int i = 0; i < values.length; i++) {
			String path = fileList.get(i).getText();
			File file = new File(path);
			if (file != null && file.exists() && file.isFile()) {
				String encodedFile = "";
				try {
					byte[] fileByte = readFile(file);
					encodedFile = encodeFile(fileByte);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				values[i] = encodedFile;
			}
		}
		return values;
	}

	@Override
	public void buildEntry(String variableName) {
		fileLabel = new Label(this, SWT.NONE);
		fileLabel.setText(variableName);
		GridData fileLabelData = new GridData();
		fileLabelData.horizontalAlignment = SWT.FILL;
		fileLabelData.widthHint = 50;
		fileLabel.setLayoutData(fileLabelData);
		if (variable.getDocumentation() != null)
			fileLabel.setToolTipText(variable.getDocumentation().getValue());
		
		fileText = new Text(this, SWT.BORDER);
		GridData fileTextData = new GridData();
		fileTextData.horizontalAlignment = SWT.FILL;
		fileTextData.grabExcessHorizontalSpace = true;
		fileText.setLayoutData(fileTextData);
		fileList.add(fileText);
		
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
		GridData newFileData = new GridData();
		newFileData.horizontalAlignment = SWT.FILL;
		newFileData.grabExcessHorizontalSpace = true;
		newFileData.horizontalSpan = 2;
		newFileData.horizontalIndent = 55;
		newFile.setLayoutData(newFileData);
		
		Button deleteButton = new Button(this, SWT.PUSH | SWT.CENTER);
		deleteButton.setText("Delete");
		GridData deleteButtonData = new GridData();
		deleteButtonData.horizontalAlignment = SWT.FILL;
		deleteButton.setLayoutData(deleteButtonData);
		deleteButton.addSelectionListener(new DeleteListener(newFile, deleteButton));
		
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
			
			FileEntry.this.fileList.remove(this.text);
			
			FileEntry.this.layout();
			FileEntry.this.redraw();
			FileEntry.this.update();
			
			Display.getCurrent().update();
			
			dialog.resizeHeight();
			dialog.resizeWidth();
		}
		
	}
}
