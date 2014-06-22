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
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.gef.palette.PaletteEntry;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

import fragmentorcp.FragmentoPlugIn;


/**
 * This action is executed if the "reload fragments" button of the toolbar is
 * pressed. It searches the fragment repository path on file system and adds all
 * fragments to the palette.
 * 
 * @author sonntamo
 * @author hahnml
 */
public class ReloadFragmentsFromFilesystemAction extends Action implements
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
	 * Creates a dialog that requests the user for the path of the fragment
	 * repository on the file system.
	 * 
	 * @author Mirko Sonntag
	 * @author hahnml
	 */
	public class SpecifyFragmentPathBox {

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
			shell.setText("Specify Fragment Registry Path");
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
		 * This is the button to browse the file system.
		 */
		Button browse;

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
			shell.setLayout(new GridLayout(3, false));

			// Create the address input
			new Label(shell, SWT.NONE).setText("Path:");
			final Text addressInput = new Text(shell, SWT.BORDER);
			addressInput.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			addressInput.setText(repositoryPath);
			addressInput.setEditable(false);

			browse = new Button(shell, SWT.PUSH);
			browse.setText("Browse...");

			browse.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					DirectoryDialog directoryDialog = new DirectoryDialog(shell);

					directoryDialog.setFilterPath(System
							.getProperty("user.dir"));
					directoryDialog
							.setMessage("Please select the fragment repository directory");

					String dir = directoryDialog.open();
					if (dir != null) {
						repositoryPath = dir;
					}
				}
			});

			// Create the reload fragments button
			reload = new Button(shell, SWT.PUSH);
			reload.setText("Reload Fragments");

			// listener for reload fragments button
			reload.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					cancelled = false;
					shell.close();
				}
			});

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
		}
	}

	/**
	 * Default path of the fragment repository folder.
	 */
	String repositoryPath = System.getProperty("java.io.tmpdir");

	/**
	 * Used to avoid several instances of the "specify fragment endpoint" box.
	 */
	private static boolean boxOpen = false;

	/**
	 * Fetches registered fragments from the local fragment repository and hooks all found fragments into the palette under "Fragments"
	 * category.
	 */
	@SuppressWarnings(value = { "rawtypes" })
	private void registerFragment() {

		if (!boxOpen) {
			SpecifyFragmentPathBox box = null;
			if (Platform.getBundle("FragmentoRCP") == null) {
				boxOpen = true;
				box = new SpecifyFragmentPathBox();
				box.run();
			} else {
				repositoryPath = FragmentoPlugIn.getDefault()
						.getPreferenceStore()
						.getString("FRAGMENTO_EXPORT_PATH");
			}
			boxOpen = false;
			// "box == null" means FragmentoRCP is available
			if ((box != null && !box.cancelled) || box == null) {

				try {

					ArrayList<FragmentImpl> fragmentList = Gateway
							.getRegisteredFragments(repositoryPath);

					if (fragmentList != null) {

						// Find the "Fragments" category of the palette
						BPELEditor editor = fEditor.getDesignEditor();
						PaletteRoot pal = editor.getPaletteRoot();
						PaletteCategory cat = null;
						for (Object obj : pal.getChildren()) {

							if (obj instanceof PaletteCategory) {
								cat = (PaletteCategory) obj;

								// We add the fragments to the "Fragments"
								// category of the palette
								if ("bpel.fragments"
										.equals(cat.getCategoryId())) {
									cat = (PaletteCategory) obj;

									// remove all entries from the palette
									List list = cat.getChildren();
									while (list.size() > 0) {
										cat.remove((PaletteEntry)list.get(0));
									}
									break;
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
							// of the palette
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
							0, e.getMessage(), e);

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
