package org.eclipse.bpel.ui.simtech.util;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.bpel.ui.simtech.properties.FileOperations;
import org.eclipse.bpel.ui.util.BPELUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * Class which provides a method to get all defined metadata 
 * for all revisions of a single process
 * @author schrotbn
 *
 */
public class MetaDataUtil {
	private static final String BPEL_EXTENSION = "bpel";

	public static HashMap<String, ArrayList<String>> getAllMetaData(QName processName)
			{
		HashMap<String, ArrayList<String>> toReturn = new HashMap<String, ArrayList<String>>();
		List<IFile> dds = new ArrayList<IFile>();
		try {
		dds.addAll(getAllDDs(processName));
		} catch(CoreException ex) {
			
		}
		for (IFile dd : dds) {
			HashMap<String,String> props = FileOperations.loadPropertiesFromDD(dd.getParent());
			
			for (String key : props.keySet()) {
				String value = props.get(key);
				if (toReturn.containsKey(key)) {
					toReturn.get(key).add(value);
				} else {
					ArrayList<String> values = new ArrayList<String>();
					values.add(value);
					toReturn.put(key,values);
				}
			}
		}
		return toReturn;
	}
	
	private static List<IFile> getAllDDs(QName processName) throws CoreException  {
		List<IFile> dds = new ArrayList<IFile>();
		for (IProject project : ResourcesPlugin.getWorkspace().getRoot()
				.getProjects()) {
			if (BPELUtil.isBPELProject(project)) {
				for (IResource res : project.members()) {
					if (BPELUtil.isBPELRevisionFolder(res)) {
						IFolder container = (IFolder) res;
						QName foundProcessName = getProcessName((IFolder)res);
						if (foundProcessName != null && foundProcessName.equals(processName)) {
							IFile dd = container.getFile("deploy.xml");
							dds.add(dd);
						}
						
					}
				}
			}
		}
		return dds;
	}

	public static QName getProcessName(IFolder folder) {
		QName processName = null;
		try {
			for (IResource file : folder.members()) {
				if (file.getAdapter(IFile.class) != null) {
					if (file.getFileExtension().equals(BPEL_EXTENSION)) {
						processName = getProcessName((IFile) file
								.getAdapter(IFile.class));
					}
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return processName;
	}

	public static QName getProcessName(IFile file) {
		QName processName = null;
		if (file.getFileExtension().equals("bpel")) {
			try {
				File bpelFile = new File(file.getLocation().toOSString());
				// Read the file content
				DocumentBuilderFactory dbf = DocumentBuilderFactory
						.newInstance();
				dbf.setNamespaceAware(true);
				DocumentBuilder db = dbf.newDocumentBuilder();
				Element oProcess = db.parse(
						new InputSource(new FileReader(bpelFile)))
						.getDocumentElement();

				String name = oProcess.getAttribute("name");
				String targetNamespace = oProcess
						.getAttribute("targetNamespace");
				processName = new QName(targetNamespace, name);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return processName;
	}
}
