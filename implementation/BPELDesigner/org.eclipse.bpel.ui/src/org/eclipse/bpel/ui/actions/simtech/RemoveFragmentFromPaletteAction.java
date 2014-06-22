package org.eclipse.bpel.ui.actions.simtech;

import java.io.File;

import org.eclipse.bpel.ui.util.BPELCreationToolEntry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.gef.ui.palette.editparts.PaletteEditPart;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;

import fragmentorcp.FragmentoPlugIn;

public class RemoveFragmentFromPaletteAction extends Action {

	private PaletteViewer viewer = null;
	private BPELCreationToolEntry selectedEntry = null;

	public RemoveFragmentFromPaletteAction(PaletteViewer paletteViewer) {
		super("Remove palette entry");
		this.viewer = paletteViewer;
	}

	@Override
	public boolean isEnabled() {
		boolean enabled = false;

		if (this.viewer != null && this.viewer.getSelection() != null) {
			if (this.viewer.getSelectionManager().getSelection() instanceof IStructuredSelection) {
				IStructuredSelection selection = (IStructuredSelection) this.viewer
						.getSelectionManager().getSelection();

				if (selection.getFirstElement() instanceof PaletteEditPart) {
					PaletteEditPart part = (PaletteEditPart) selection
							.getFirstElement();
					if (part.getModel() instanceof BPELCreationToolEntry) {
						BPELCreationToolEntry entry = (BPELCreationToolEntry) part
								.getModel();

						if (entry.getParent().getLabel().equals("Fragments")) {
							enabled = true;
							selectedEntry = entry;
						} else {
							selectedEntry = null;
						}
					}
				}
			}
		}

		return enabled;
	}

	@Override
	public void run() {
		if (this.selectedEntry != null) {

			// Remove the folder of the fragment from the fragment repository
			// folder
			FragmentoPlugIn plugin = FragmentoPlugIn.getDefault();

			if (plugin != null) {
				// Get the Fragmento repository path
				Path path = new Path(plugin.getPreferenceStore().getString(
						FragmentoPlugIn.FRAGMENTO_EXPORT_PATH));
				// Append the name of the fragment
				IPath fragmentPath = path.append(this.selectedEntry.getLabel());

				File fragment = new File(fragmentPath.toOSString());

				if (fragment.exists() && fragment.isDirectory()) {
					// Delete all containing files
					for (File file : fragment.listFiles()) {
						file.delete();
					}

					// Delete the folder
					fragment.delete();
				}

				// Remove the fragment from the palette
				this.selectedEntry.getParent().remove(selectedEntry);
			} else {
				// TODO: Open MessageDialog that Fragmento is not initialized...
				MessageDialog
						.openError(Display.getCurrent().getActiveShell(),
								"Fragmento is not initialized correctly",
								"Please open the Fragmento Repository View to initialize Fragmento.");
			}
		}
	}
}
