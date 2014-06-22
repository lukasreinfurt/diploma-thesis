package org.eclipse.bpel.ui.actions.simtech;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.bpel.common.ui.palette.PaletteCategory;
import org.eclipse.bpel.ui.BPELEditor;
import org.eclipse.bpel.ui.BPELMultipageEditorPart;
import org.eclipse.bpel.ui.palette.simtech.FragmentUIObjectFactory;
import org.eclipse.bpel.ui.simtech.gateway.FragmentImpl;
import org.eclipse.bpel.ui.simtech.gateway.Gateway;
import org.eclipse.bpel.ui.util.BPELCreationToolEntry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

/**
 * This action is executed if the "reload fragments" button of the toolbar is
 * pressed. It invokes the fragments registry und adds all registered fragments
 * to the palette.
 * 
 * @author sonntamo
 */
public class ReloadFragmentsAction extends Action implements
		IEditorActionDelegate {
	private BPELMultipageEditorPart fEditor;

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		if (targetEditor instanceof BPELMultipageEditorPart) {
			fEditor = (BPELMultipageEditorPart) targetEditor;
		} else {
			fEditor = null;
		}
	}

	public void run(IAction action) {
		registerFragment();
	}

	public void selectionChanged(IAction action, ISelection selection) {

		if (fEditor == null) {
			action.setEnabled(false);
		} else {
			action.setEnabled(fEditor.getActiveEditor() instanceof BPELEditor);
		}

	}

	/**
	 * Creates a dialog that requests the user for parameters to specify the location 
	 * of the fragment registry.
	 * 
	 * @author Mirko Sonntag
	 */
	public class SpecifyFragmentEndpointBox {

		/**
		 * Result of the shell. The user may have cancelled the invocation of 
		 * the fragment registry or not.
		 */
		boolean cancelled = true;

		/**
		 * Creates the window for the specification of the fragment repository 
		 * endpoint.
		 */
		public void run() {
			Display display = Display.getCurrent();
			Shell shell = new Shell(display.getActiveShell());
			shell.setText("Specify Fragment Registry Endpoint");
			createContents(shell);
			shell.pack();
			shell.open();
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
		}

		/**
		 * This label shows error messages.
		 */
		Label errors;
		
		/**
		 * This is the button that starts the invocation of the fragment 
		 * repository.
		 */
		Button reload;
		
		/**
		 * Creates the window's contents.
		 * 
		 * @param shell
		 *            the parent shell
		 */
		private void createContents(final Shell shell) {
			shell.setLayout(new GridLayout(2, false));
			
			// Create the label for error messages
			errors = new Label(shell, SWT.NONE);
		    GridData gridData = new GridData(GridData.FILL_BOTH);
		    gridData.horizontalSpan = 2;
			Color color = new Color(shell.getDisplay(), 255, 0, 0);
			errors.setForeground(color);
			errors.setLayoutData(gridData);
			
		    // Create the address input
			new Label(shell, SWT.NONE).setText("Address:");
			final Text addressInput = new Text(shell, SWT.BORDER);
			addressInput.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			addressInput.setText(address);

			// Create the operation input
			new Label(shell, SWT.NONE).setText("Operation:");
			final Text operationInput = new Text(shell, SWT.BORDER);
			operationInput
					.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			operationInput.setText(operation);

			// Create the reload fragments button 
			reload = new Button(shell, SWT.PUSH);
			reload.setText("Reload Fragments");
			
			// listener for reload fragments button
			reload.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					address = addressInput.getText();
					operation = operationInput.getText();
					cancelled = false;
					shell.close();
				}
			});
			
			// check if address and operation are valid
			check(address, operation);

			// Create the cancel button
			Button cancel = new Button(shell, SWT.PUSH);
			cancel.setText("Cancel");
			
			// listener for cancel button
			cancel.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					cancelled = true;
					shell.close();
				}
			});
			
			// key listeners are used to check the user input during typing
			addressInput.addKeyListener(new KeyListener(){

				public void keyPressed(KeyEvent e) {
				}
	
				public void keyReleased(KeyEvent e) {
					check(addressInput.getText(), operationInput.getText());
				}
				
			});
			operationInput.addKeyListener(new KeyListener(){

				public void keyPressed(KeyEvent e) {
				}
			
				public void keyReleased(KeyEvent e) {
					check(addressInput.getText(), operationInput.getText());
				}
			});
		}
		
		/**
		 * Checks whether the address and operation of the fragment registry are 
		 * specified correctly by the user.
		 * <p>If not, the violations are displayed and the "reload fragments" button 
		 * is disabled.</p> 
		 * 
		 * @param address
		 * @param operation
		 */
		final void check(String address, String operation) {
			ArrayList<String> errMsgs = new ArrayList<String>();
			if (!address.startsWith("http://")) {
				errMsgs.add("Address must start with 'http://'");
			}
			if (!operation.startsWith("urn:")) {
				errMsgs.add("Operation must start with 'urn:'");
			}
			String result = "";
			for (String errMsg : errMsgs) {
				result += errMsg + "; ";
			}
			errors.setText(result);
			if (!"".equals(result)) {
				reload.setEnabled(false);	
			} else {
				reload.setEnabled(true);
			}
		}
		
	}

	/**
	 * Default address of fragment registry.
	 */
	private String address = "http://localhost:8080/axis2/services/FragmentRepositoryService.FragmentRepositoryServiceHttpSoap12Endpoint/";

	/**
	 * Default operation (i.e. soap:action) of fragment registry.
	 */
	private String operation = "urn:getRegisteredFragments";

	/**
	 * Used to avoid several instances of the "specify fragment endpoint" box. 
	 */
	private static boolean boxOpen = false;

	/**
	 * Fetches registered fragments from the fragment registry via a Web service
	 * call and hooks all found fragments into the palette under "Fragments"
	 * category.
	 */
	@SuppressWarnings(value = { "rawtypes" })
	private void registerFragment() {
		
		if (!boxOpen) {
			boxOpen = true;
			SpecifyFragmentEndpointBox box = new SpecifyFragmentEndpointBox();
			box.run();
			boxOpen = false;
			if (!box.cancelled) {

				try {

					ArrayList<FragmentImpl> fragmentList = Gateway
							.getRegisteredFragments(address, operation);

					if (fragmentList != null && fragmentList.size() > 0) {

						// Find the "Fragments" category of the palette
						BPELEditor editor = fEditor.getDesignEditor();
						PaletteRoot pal = editor.getPaletteRoot();
						PaletteCategory cat = null;
						for (Object obj : pal.getChildren()) {

							if (obj instanceof PaletteCategory) {
								cat = (PaletteCategory) obj;

								// We add the fragments to the "Fragments"
								// category of the
								// palette
								if ("bpel.fragments"
										.equals(cat.getCategoryId())) {
									cat = (PaletteCategory) obj;

									// remove all entries from the palette
									List list = cat.getChildren();
									while (list.size() > 0) {
										list.remove(0);
									}
								}
							}
						}

						// iterate over all registered fragments
						for (FragmentImpl fragment : fragmentList) {

							// the object factory is used to create a new
							// fragment instance
							// when the fragment is dragged and dropped into a
							// BPEL process
							FragmentUIObjectFactory factory = new FragmentUIObjectFactory(
									editor, fragment);

							// We add the fragment to the "Fragments" category
							// of the
							// palette
							if ("bpel.fragments".equals(cat.getCategoryId())) {
								cat.add(new BPELCreationToolEntry(fragment
										.getName(), fragment.getDescription(),
										factory));
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();

					Status status = new Status(IStatus.ERROR, "My Plug-in ID",
							0, e.getMessage(), null);

					// Display the dialog
					ErrorDialog.openError(
							Display.getCurrent().getActiveShell(),
							"JFace Error",
							"Invocation of fragments registry went wrong.",
							status);
				}
			}
		}

	}

}
