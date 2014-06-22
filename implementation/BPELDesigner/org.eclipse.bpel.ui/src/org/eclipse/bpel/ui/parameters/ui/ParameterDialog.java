package org.eclipse.bpel.ui.parameters.ui;

import org.eclipse.bpel.ui.parameters.handler.ParameterHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;


/**
* This code was edited or generated using CloudGarden's Jigloo
* SWT/Swing GUI Builder, which is free for non-commercial
* use. If Jigloo is being used commercially (ie, by a corporation,
* company or business for any purpose whatever) then you
* should purchase a license for each developer using Jigloo.
* Please visit www.cloudgarden.com for details.
* Use of Jigloo implies acceptance of these licensing terms.
* A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
* THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
* LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
* 
* The parameter dialog. Here the user can fill his values for the 
* parameter variables
* 
* @author tolevar
*/
public class ParameterDialog extends org.eclipse.swt.widgets.Dialog {

	private Shell dialogShell;
	
	private Button okButton;
	private Button cancelButton;
	
	//@hahnml
	private ParameterHandler parameterHandler;

	public ParameterDialog(Shell parent, int style, ParameterHandler parameterHandler) {
		super(parent, style);
		this.parameterHandler = parameterHandler;
	}

	public ParameterHandler getParameterHandler() {
		return this.parameterHandler;
	}
	/**
	 * Opens the dialog and resizes the dialog,so it fits the actual
	 * width and height
	 */
	public void open() {
		try {	
			buildButtonBar();
			resizeHeight();
			resizeWidth();
			
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
		dialogShell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE| SWT.MAX | SWT.APPLICATION_MODAL);

		dialogShell.setLayout(new GridLayout(2, false));
		dialogShell.layout();
		dialogShell.setSize(450, 290);
		dialogShell.setText("Parameter Variable");
	}
	
	private void buildButtonBar() {
		//OKButton
		okButton = new Button(dialogShell, SWT.PUSH | SWT.CENTER);
		GridData okButtonData = new GridData();
		okButtonData.horizontalAlignment = SWT.END;
		okButton.setLayoutData(okButtonData);
		okButton.setText("OK");
		okButton.addListener(SWT.Selection, this.parameterHandler);
		
		//CancelButton
		cancelButton = new Button(dialogShell, SWT.PUSH | SWT.CENTER);
		GridData cancelButtonData = new GridData();
		cancelButtonData.horizontalAlignment = SWT.BEGINNING;
		cancelButton.setLayoutData(cancelButtonData);
		cancelButton.setText("Cancel");
		cancelButton.addListener(SWT.Selection, this.parameterHandler);
	}
	
	/**
	 * Resizes the height of the dialog
	 */
	public void resizeHeight() {
		Point point = dialogShell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		if (point.y >= dialogShell.getSize().y) {
			dialogShell.setSize(dialogShell.getSize().x, point.y);
		} else {
			dialogShell.setSize(point);
		}
	}
	
	/**
	 * Resizes the widht of the dialog
	 */
	public void resizeWidth() {
		Point point = dialogShell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		if (point.x >= dialogShell.getSize().x) {
			dialogShell.setSize(point.x, dialogShell.getSize().y);
		} else {
			dialogShell.setSize(point);
		}
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
