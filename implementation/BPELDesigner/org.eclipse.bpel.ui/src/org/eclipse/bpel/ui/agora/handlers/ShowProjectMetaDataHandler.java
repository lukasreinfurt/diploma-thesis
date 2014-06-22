package org.eclipse.bpel.ui.agora.handlers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.bpel.apache.ode.deploy.model.dd.DocumentRoot;
import org.eclipse.bpel.apache.ode.deploy.model.dd.ProcessType;
import org.eclipse.bpel.apache.ode.deploy.model.dd.TDeployment;
import org.eclipse.bpel.apache.ode.deploy.model.dd.TMetaData;
import org.eclipse.bpel.apache.ode.deploy.model.dd.util.ddResourceFactoryImpl;
import org.eclipse.bpel.ui.dialogs.ProcessMetaDataDialog;
import org.eclipse.bpel.ui.util.BPELUtil;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ShowProjectMetaDataHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil
				.getActiveMenuSelection(event);

		boolean openDialog = false;
		HashMap<String, TMetaData> metaData = new HashMap<String, TMetaData>();

		if (selection.getFirstElement() instanceof IProject) {
			IProject bpelProject = (IProject) selection.getFirstElement();

			if (BPELUtil.isBPELProject(bpelProject)
					|| BPELUtil.containsBPELRevisionFolders(bpelProject)) {

				try {
					List<IFile> dds = new ArrayList<IFile>();

					for (IResource res : bpelProject.members()) {
						if (BPELUtil.isBPELRevisionFolder(res)) {
							IFolder container = (IFolder) res;
							IFile dd = container.getFile("deploy.xml");

							dds.add(dd);
						}
					}

					// Add all new elements to the map
					metaData.putAll(collectMetaData(dds));

					if (metaData != null && !metaData.isEmpty()) {
						openDialog = true;
					}
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				MessageDialog
						.openInformation(
								Display.getDefault().getActiveShell(),
								"No BPEL project selected",
								"The selected project is no valid BPEL project. Please select a project which contains one or more 'rev' folders.");
			}
		} else if (selection.getFirstElement() instanceof IFolder) {
			IFolder container = (IFolder) selection.getFirstElement();

			if (BPELUtil.isBPELRevisionFolder(container)) {
				IFile dd = container.getFile("deploy.xml");

				metaData = collectMetaData(Arrays.asList(dd));

				if (metaData != null && !metaData.isEmpty()) {
					openDialog = true;
				}
			} else {
				MessageDialog
						.openInformation(
								Display.getDefault().getActiveShell(),
								"No BPEL revision folder selected",
								"The selected folder is no valid BPEL revision folder. Please select a valid 'rev' folder.");
			}
		}

		if (openDialog) {
			// Open a new process meta-data dialog
			new ProcessMetaDataDialog(metaData);
		}

		return null;
	}

	private HashMap<String, TMetaData> collectMetaData(
			List<IFile> deploymentDescriptors) {
		HashMap<String, TMetaData> metaData = new HashMap<String, TMetaData>();

		// Loop through all deployment descriptor files
		for (IFile dd : deploymentDescriptors) {
			Resource resource = null;
			ddResourceFactoryImpl fac = new ddResourceFactoryImpl();
			resource = fac.createResource(URI.createFileURI(dd.getLocation()
					.toOSString()));

			HashMap<QName, String> processFilePaths = getProcessFilePaths(dd);

			try {
				resource.load(Collections.EMPTY_MAP);

				EList<EObject> contents = resource.getContents();
				if (!contents.isEmpty()
						&& contents.get(0) instanceof DocumentRoot) {
					TDeployment deployDescriptor = ((DocumentRoot) contents
							.get(0)).getDeploy();

					// Loop through all process types defined in the deployment
					// descriptor
					for (ProcessType process : deployDescriptor.getProcess()) {
						String processName = process.getName().toString();

						if (processFilePaths.containsKey(process.getName())) {
							processName = processFilePaths.get(process
									.getName());
						}

						// Add the meta data to the map
						metaData.put(processName, process.getMetaData());
					}
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return metaData;
	}

	private HashMap<QName, String> getProcessFilePaths(IFile dd) {
		HashMap<QName, String> paths = new HashMap<QName, String>();

		try {
			for (IResource res : dd.getParent().members()) {

				if (res.getFileExtension().equals("bpel")) {
					String processPath = res.getFullPath().toOSString();
					QName processName = null;

					// Read the file content
					DocumentBuilderFactory dbf = DocumentBuilderFactory
							.newInstance();
					dbf.setNamespaceAware(true);
					File bpelFile = new File(res.getLocation().toOSString());

					try {
						DocumentBuilder db = dbf.newDocumentBuilder();
						Element oProcess = db.parse(
								new InputSource(new FileReader(bpelFile)))
								.getDocumentElement();

						String name = oProcess.getAttribute("name");
						String targetNamespace = oProcess
								.getAttribute("targetNamespace");

						processName = new QName(targetNamespace, name);
					} catch (ParserConfigurationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					if (processName != null) {
						paths.put(processName, processPath);
					}
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return paths;
	}
}
