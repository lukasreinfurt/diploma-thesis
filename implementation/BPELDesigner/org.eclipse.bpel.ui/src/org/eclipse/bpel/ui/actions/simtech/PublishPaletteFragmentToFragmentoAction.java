package org.eclipse.bpel.ui.actions.simtech;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.bpel.fragment.extraction.wizard.PublishFragmentWizard;
import org.eclipse.bpel.fragment.extraction.wizard.pages.FragmentListWizardPage;
import org.eclipse.bpel.fragment.extraction.wizard.pages.FragmentWizardPage;
import org.eclipse.bpel.ui.simtech.gateway.ConfigHandler;
import org.eclipse.bpel.ui.util.BPELCreationToolEntry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.gef.ui.palette.editparts.PaletteEditPart;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;

import fragmentorcp.FragmentoPlugIn;
import fragmentorcp.wizards.pages.fragment.ArtefactWithMetaData;

public class PublishPaletteFragmentToFragmentoAction extends Action {

	private PaletteViewer viewer = null;
	private BPELCreationToolEntry selectedEntry = null;

	public PublishPaletteFragmentToFragmentoAction(PaletteViewer paletteViewer) {
		super("Publish to Fragmento");
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

			// Check if Fragmento is available
			FragmentoPlugIn plugin = FragmentoPlugIn.getDefault();

			if (plugin != null) {

				// Get the Fragmento repository path
				Path path = new Path(plugin.getPreferenceStore().getString(
						FragmentoPlugIn.FRAGMENTO_EXPORT_PATH));
				// Append the name of the fragment
				IPath fragmentPath = path.append(this.selectedEntry.getLabel());

				File fragment = new File(fragmentPath.toOSString());

				ConfigHandler cfgHandler = null;

				String authorName = "";
				String iconFilePath = "";
				String ddFilePath = "";
				String fragmentFilePath = "";
				List<String> wsdlFilePaths = new ArrayList<String>();
				List<String> xsdFilePaths = new ArrayList<String>();

				if (fragment.exists() && fragment.isDirectory()) {
					File config = new File(fragmentPath.addTrailingSeparator()
							.append("config.xml").toOSString());
					cfgHandler = new ConfigHandler(config);
					cfgHandler.loadPropertiesFromXMLFile();

					for (String fileName : fragment.list()) {
						if (fileName.equals("ApacheODE-DD.xml")) {
							ddFilePath = fragmentPath.addTrailingSeparator()
									.append(fileName).toOSString();
						} else if (fileName.endsWith(".bpel")) {
							fragmentFilePath = fragmentPath
									.addTrailingSeparator().append(fileName)
									.toOSString();
						} else if (fileName.endsWith(".wsdl")) {
							wsdlFilePaths.add(fragmentPath
									.addTrailingSeparator().append(fileName)
									.toOSString());
						} else if (fileName.endsWith(".xsd")) {
							xsdFilePaths.add(fragmentPath
									.addTrailingSeparator().append(fileName)
									.toOSString());
						}
					}

					iconFilePath = cfgHandler
							.getProperty(ConfigHandler.PROPERTY_ICON);
					authorName = cfgHandler
							.getProperty(ConfigHandler.PROPERTY_AUTHOR);
				}

				if (cfgHandler != null) {
					PublishFragmentWizard wizard = new PublishFragmentWizard(
							fragmentPath.toOSString());

					WizardDialog dialog = new WizardDialog(Display.getCurrent()
							.getActiveShell(), wizard);
					dialog.create();
					
					// Initialize all pages with the available data from the
					// file system
					FragmentWizardPage fragmentPage = (FragmentWizardPage) wizard
							.getPage("Fragment Page");
					if (fragmentPage != null) {
						fragmentPage.setFragmentName(cfgHandler
								.getProperty(ConfigHandler.PROPERTY_NAME));
						fragmentPage.setAuthorName(authorName);
						fragmentPage
								.setDescription(cfgHandler
										.getProperty(ConfigHandler.PROPERTY_DESCRIPTION));
						fragmentPage.setFileLocation(fragmentFilePath);
					}

					FragmentWizardPage iconPage = (FragmentWizardPage) wizard
							.getPage("Icon Page");
					if (iconPage != null) {
						iconPage.setFileLocation(iconFilePath);
						iconPage.setFragmentName(iconFilePath.substring(
								iconFilePath.lastIndexOf(System.getProperty("file.separator")) + 1,
								iconFilePath.lastIndexOf(".")));
					}

					FragmentWizardPage ddPage = (FragmentWizardPage) wizard
							.getPage("Deployment Descriptor Page");
					if (ddPage != null) {
						ddPage.setFileLocation(ddFilePath);
						ddPage.setFragmentName(ddFilePath.substring(
								ddFilePath.lastIndexOf(System.getProperty("file.separator")) + 1,
								ddFilePath.lastIndexOf(".")));
					}

					FragmentListWizardPage wsdlPage = (FragmentListWizardPage) wizard
							.getPage("WSDL Page");
					if (wsdlPage != null) {
						for (String wsdl : wsdlFilePaths) {
							ArtefactWithMetaData art = new ArtefactWithMetaData();
							art.setAuthor(authorName);
							art.setFileLocation(wsdl);
							art.setName(wsdl.substring(
									wsdl.lastIndexOf(System.getProperty("file.separator")) + 1,
									wsdl.lastIndexOf(".")));
							
							wsdlPage.getArtefacts().add(art);
						}
					}

					FragmentListWizardPage xsdPage = (FragmentListWizardPage) wizard
							.getPage("XSD Page");
					if (xsdPage != null) {
						for (String xsd : xsdFilePaths) {
							ArtefactWithMetaData art = new ArtefactWithMetaData();
							art.setAuthor(authorName);
							art.setFileLocation(xsd);
							art.setName(xsd.substring(
									xsd.lastIndexOf(System.getProperty("file.separator")) + 1,
									xsd.lastIndexOf(".")));
							
							xsdPage.getArtefacts().add(art);
						}
					}

					
					dialog.open();
				}
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
