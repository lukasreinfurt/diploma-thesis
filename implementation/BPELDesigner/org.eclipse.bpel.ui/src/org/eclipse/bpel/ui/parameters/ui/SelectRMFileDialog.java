package org.eclipse.bpel.ui.parameters.ui;

import java.util.List;

import org.eclipse.bpel.ui.parameters.ui.composite.RMFileEntry;
import org.eclipse.simtech.resourceManager.client.ResourceManagementClient;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.ustutt.simtech.resinf.client.resmgr.FileType;

public class SelectRMFileDialog extends org.eclipse.swt.widgets.Dialog {

	private Shell dialogShell;
	String selectedCtxID;
	private Text rmUrlText;
	private Combo ctxIDsCombo;
	private Combo fileCombo;
	private Combo dirCombo;
	private Button okButton;
	private Button cancelButton;
	private Text rmFileText;

	private List<FileType> filesInCtx;
	
	public SelectRMFileDialog(Text rmFileText, Shell parent, int style) {
		super(parent, style);
		this.rmFileText = rmFileText;
	}

	/**
	 * Opens the dialog and resizes the dialog,so it fits the actual width and
	 * height
	 */
	public void open() {
		try {
			buildButtonBar();

			dialogShell.open();
			Display display = dialogShell.getDisplay();
			while (!dialogShell.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Builds the shell
	 */
	public void buildShell() {
		Shell parent = getParent();
		dialogShell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX
				| SWT.APPLICATION_MODAL);

		dialogShell.setLayout(new GridLayout(2, false));
		dialogShell.layout();
		dialogShell.setSize(500, 290);
		dialogShell.setText("Resource Management");
	}

	public void refreshCtxIdCombo() {
		ctxIDsCombo.removeAll();
		List<String> ctxIds = ResourceManagementClient.getInstance(
				rmUrlText.getText())
				.listManagementContexts();
		for (String ctxId : ctxIds) {
			ctxIDsCombo.add(ctxId);
		}
	}

	private void buildButtonBar() {
		Label rmUrlLabel = new Label(dialogShell, SWT.NONE);
		rmUrlLabel.setText("Select Context ID:");
		GridData rmUrlData = new GridData();
		rmUrlData.horizontalAlignment = SWT.FILL;
		rmUrlData.widthHint = 100;
		rmUrlLabel.setLayoutData(rmUrlData);
		
		rmUrlText = new Text(dialogShell, SWT.NONE);
		GridData rmUrlLabelData = new GridData();
		rmUrlData.horizontalAlignment = SWT.FILL;
		rmUrlText.setLayoutData(rmUrlLabelData);
		rmUrlText.setText("http://localhost:8080/axis2/services/ResourceManagerService");
		
		Label ctxIDLabel = new Label(dialogShell, SWT.NONE);
		ctxIDLabel.setText("Select Context ID:");
		GridData ctxIDData = new GridData();
		ctxIDData.horizontalAlignment = SWT.FILL;
		ctxIDData.widthHint = 100;
		ctxIDLabel.setLayoutData(ctxIDData);

		ctxIDsCombo = new Combo(dialogShell, SWT.NONE);
		GridData ctxIDsData = new GridData();
		ctxIDsData.horizontalAlignment = SWT.FILL;
		ctxIDsCombo.setLayoutData(ctxIDsData);
		ctxIDsCombo.setText("context ID ...");
		ctxIDsCombo.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				String ctxId = ctxIDsCombo.getText();
				selectedCtxID = ctxId;
				filesInCtx = ResourceManagementClient.getInstance().readManagementContext(
						ctxId);
				dirCombo.removeAll();
				for (FileType file : filesInCtx) {
					if (file.getType() != null && file.getType().equals("dir")) {
						dirCombo.add(file.getPath());
					}
				}
				dirCombo.select(0);
				setFilesForDir(dirCombo.getText());
				checkOkButton();
			}

		});
		refreshCtxIdCombo();

		Label dirLabel = new Label(dialogShell, SWT.NONE);
		dirLabel.setText("Select Dir:");
		GridData dirLabelData = new GridData();
		dirLabelData.horizontalAlignment = SWT.FILL;
		dirLabelData.widthHint = 100;
		dirLabel.setLayoutData(dirLabelData);

		dirCombo = new Combo(dialogShell, SWT.NONE);
		GridData dirComboData = new GridData();
		dirComboData.horizontalAlignment = SWT.FILL;
		dirCombo.setLayoutData(dirComboData);
		dirCombo.setText("dir ...");
		dirCombo.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				setFilesForDir(dirCombo.getText());
				checkOkButton();
			}

		});
		
		Label fileLabel = new Label(dialogShell, SWT.NONE);
		fileLabel.setText("Select File:");
		GridData fileLabelData = new GridData();
		fileLabelData.horizontalAlignment = SWT.FILL;
		fileLabelData.widthHint = 100;
		fileLabel.setLayoutData(fileLabelData);

		fileCombo = new Combo(dialogShell, SWT.NONE);
		GridData fileComboData = new GridData();
		fileComboData.horizontalAlignment = SWT.FILL;
		fileCombo.setLayoutData(fileComboData);
		fileCombo.setText("file ...");
		fileCombo.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				checkOkButton();
			}

		});

		// OKButton
		okButton = new Button(dialogShell, SWT.PUSH | SWT.CENTER);
		GridData okButtonData = new GridData();
		okButtonData.horizontalAlignment = SWT.END;
		okButton.setLayoutData(okButtonData);
		okButton.setText("OK");
		okButton.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				String file = dirCombo.getText() + fileCombo.getText(); 
				rmFileText.setText(file);
				dialogShell.close();
				dialogShell.dispose();
			}
		});
		okButton.setEnabled(false);
		
		// CancelButton
		cancelButton = new Button(dialogShell, SWT.PUSH | SWT.CENTER);
		GridData cancelButtonData = new GridData();
		cancelButtonData.horizontalAlignment = SWT.BEGINNING;
		cancelButton.setLayoutData(cancelButtonData);
		cancelButton.setText("Cancel");
		cancelButton.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				dialogShell.close();
				dialogShell.dispose();
			}
		});
	}

	private void checkOkButton() {
		if (fileCombo.getText() != null && !fileCombo.getText().equals("")) {					
			okButton.setEnabled(true);
		} else {
			okButton.setEnabled(false);
		}
		
	}
	
	private void setFilesForDir(String dir) {
		fileCombo.removeAll();
		for (FileType file : filesInCtx) {
			if (file.getType() != null && file.getType().equals("file")) {
				String path = file.getPath();
				if (path != null) {
					if (path.startsWith(dir)) {
						path = path.substring(dir.length());
						int ind = path.indexOf("/");
						int lastInd = path.lastIndexOf("\\");
						if (ind == -1) {
							ind = path.indexOf("\\");
							lastInd = path.lastIndexOf("\\");
						}
						if (ind == lastInd) {
							fileCombo.add(path);						
						}
					}
				}
			}
		}
		fileCombo.select(0);
	}
	
	/**
	 * Returns the ok button
	 * 
	 * @return okButton - Button
	 */
	public Button getOkButton() {
		return okButton;
	}

	/**
	 * Returns the cancel button
	 * 
	 * @return cancelButton - Button
	 */
	public Button getCancelButton() {
		return cancelButton;
	}

	/**
	 * Returns the dialog shell
	 * 
	 * @return dialogShell - Shell
	 */
	public Shell getDialogShell() {
		return dialogShell;
	}
}
