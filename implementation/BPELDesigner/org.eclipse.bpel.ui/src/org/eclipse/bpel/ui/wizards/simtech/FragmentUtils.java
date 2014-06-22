package org.eclipse.bpel.ui.wizards.simtech;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.namespace.QName;

import org.eclipse.bpel.model.BPELPackage;
import org.eclipse.bpel.model.CorrelationSet;
import org.eclipse.bpel.model.Extension;
import org.eclipse.bpel.model.MessageExchange;
import org.eclipse.bpel.model.PartnerLink;
import org.eclipse.bpel.model.Process;
import org.eclipse.bpel.model.Variable;
import org.eclipse.bpel.model.adapters.INamespaceMap;
import org.eclipse.bpel.model.proxy.PartnerLinkTypeProxy;
import org.eclipse.bpel.model.simtech.BundleDocument;
import org.eclipse.bpel.model.simtech.WSDL;
import org.eclipse.bpel.model.util.BPELConstants;
import org.eclipse.bpel.model.util.BPELUtils;
import org.eclipse.bpel.ui.BPELEditor;
import org.eclipse.bpel.ui.BPELUIPlugin;
import org.eclipse.bpel.ui.agora.instances.InstanceHelper;
import org.eclipse.bpel.ui.agora.manager.MonitorManager;
import org.eclipse.bpel.ui.agora.provider.MonitoringProvider;
import org.eclipse.bpel.ui.util.BPELUtil;
import org.eclipse.bpel.ui.wizards.simtech.documents.WSDLImpl;
import org.eclipse.bpel.ui.wizards.simtech.documents.XSDImpl;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.wsdl.Namespace;
import org.eclipse.wst.wsdl.WSDLFactory;
import org.eclipse.wst.wsdl.WSDLPackage;
import org.simtech.workflow.ode.auditing.communication.messages.InstanceInformation;

/**
 * Provides some useful methods to check if files exist or process model objects
 * are in conflict.
 * 
 * @author hahnml
 * 
 */
public class FragmentUtils {

	public enum ConflictType {
		SOLVED, DUPLICATE, CONFLICT
	}

	public class Conflict {
		private EObject object = null;
		private EObject additionalInfo = null;
		private ConflictType type = ConflictType.SOLVED;

		public Conflict(EObject object, ConflictType type) {
			this.object = object;
			this.type = type;
		}

		public EObject getObject() {
			return object;
		}

		public EObject getAdditionalInfo() {
			return additionalInfo;
		}

		public void setAdditionalInfo(EObject additionalInfo) {
			this.additionalInfo = additionalInfo;
		}

		public void setEObject(EObject object) {
			this.object = object;
		}

		public ConflictType getConflictType() {
			return type;
		}

		public void setConflictType(ConflictType type) {
			this.type = type;
		}
	}

	private static FragmentUtils instance = null;
	private WizardModel model = null;
	private Process fragmentProcess = null;

	private HashMap<BundleDocument, Boolean> existingFileMap = new HashMap<BundleDocument, Boolean>();

	private HashMap<EClass, List<Conflict>> conflictMap = new HashMap<EClass, List<Conflict>>();
	private List<EObject> removedEObjects = new ArrayList<EObject>();

	private ArrayList<BundleDocument> existingFiles = new ArrayList<BundleDocument>();
	private Listener listener;

	private FragmentUtils(WizardModel model) {
		this.model = model;

		File[] wsdls = getFilesInProject(model.process, ".wsdl");
		File[] xsds = getFilesInProject(model.process, ".xsd");

		// Initialize the conflict HashMap
		this.conflictMap.put(BPELPackage.Literals.VARIABLE,
				new ArrayList<Conflict>());
		this.conflictMap.put(BPELPackage.Literals.PARTNER_LINK,
				new ArrayList<Conflict>());
		this.conflictMap.put(BPELPackage.Literals.CORRELATION_SET,
				new ArrayList<Conflict>());
		this.conflictMap.put(BPELPackage.Literals.MESSAGE_EXCHANGE,
				new ArrayList<Conflict>());
		this.conflictMap.put(BPELPackage.Literals.EXTENSION,
				new ArrayList<Conflict>());
		this.conflictMap.put(WSDLPackage.Literals.NAMESPACE,
				new ArrayList<Conflict>());

		if (this.model.fragment instanceof Process) {
			this.fragmentProcess = (Process) this.model.fragment;
		}

		this.addExistingWSDLs(wsdls);
		this.addExistingXSDs(xsds);
	}

	public static FragmentUtils getUtils(WizardModel model) {
		// Create a new instance if none exists or a WizardModel is specified
		if (instance == null || model != null) {
			instance = new FragmentUtils(model);
		}

		return instance;
	}

	public Process getFragmentProcess() {
		return this.fragmentProcess;
	}

	public HashMap<EClass, List<Conflict>> getConflictMap() {
		return conflictMap;
	}

	public boolean hasConflictsOfType(EClass clazz) {
		boolean hasConflicts = false;

		if (this.conflictMap.containsKey(clazz)) {
			hasConflicts = !this.conflictMap.get(clazz).isEmpty();
		}

		return hasConflicts;
	}

	public List<Conflict> getConflictsOfType(EClass clazz) {
		return this.conflictMap.get(clazz);
	}

	public void changeElementName(Conflict conflict, String name) {
		// Variable
		if (conflict.getObject() instanceof Variable) {
			if (name != null) {
				// Set the new name to the element
				((Variable) conflict.getObject()).setName(name);
			}
		}

		// MessageExchange
		else if (conflict.getObject() instanceof MessageExchange) {
			if (name != null) {
				// Set the new name to the element
				((MessageExchange) conflict.getObject()).setName(name);
			}
		}

		// CorrelationSet
		else if (conflict.getObject() instanceof CorrelationSet) {
			if (name != null) {
				// Set the new name to the element
				((CorrelationSet) conflict.getObject()).setName(name);
			}
		}

		// PartnerLink
		else if (conflict.getObject() instanceof PartnerLink) {
			if (name != null) {
				// Set the new name to the element
				((PartnerLink) conflict.getObject()).setName(name);
			}
		}

		// Check if the conflictMap contains a list of the elements class
		if (this.conflictMap.containsKey(conflict.getObject().eClass())) {
			// Remove the element from the list
			this.conflictMap.get(conflict.getObject().eClass())
					.remove(conflict);
		}

		// Fire a event for the update of the wizard page buttons
		Event evt = new Event();
		evt.text = "Refresh";
		listener.handleEvent(evt);
	}

	public void changeNamespace(Conflict conflict, String newPrefix,
			String newNamespaceURI, String oldPrefix, String oldURI) {
		INamespaceMap<String, String> prefixNSMap = BPELUtils
				.getNamespaceMap(model.fragment);
		// Remove old namespace
		if (prefixNSMap.containsKey(oldPrefix)) {
			prefixNSMap.remove(oldPrefix);
		}
		// Add new namespace
		prefixNSMap.put(newPrefix, newNamespaceURI);

		// Remove conflict
		Namespace ns = (Namespace) conflict.getObject();
		if (this.conflictMap.containsKey(ns.eClass())) {
			this.conflictMap.get(ns.eClass()).remove(conflict);
		}

		Event evt = new Event();
		evt.text = "Refresh";
		listener.handleEvent(evt);

		if (!newPrefix.equals(oldPrefix)) {
			updateNamespacePrefixes((Process) model.fragment, oldURI,
					oldPrefix, newPrefix);
		}
	}

	public void markExistingFiles(ArrayList<? extends BundleDocument> documents) {
		for (BundleDocument doc : documents) {
			boolean contains = checkIfProjectContainsFile(doc);
			// Update the selected flag
			doc.setSelected(!contains);
		}
	}

	public boolean checkIfProjectContainsFile(BundleDocument doc) {
		boolean contains = false;

		if (existingFileMap.containsKey(doc)) {
			contains = existingFileMap.get(doc);
		} else {

			for (BundleDocument bundle : this.existingFiles) {
				if (bundle.getFileExtension().equals(doc.getFileExtension())
						&& bundle.getTargetNamespace().equals(
								doc.getTargetNamespace())) {

					if (doc instanceof WSDL) {
						if (((WSDL) bundle).getName().equals(
								((WSDL) doc).getName())) {
							contains = true;
							existingFileMap.put(doc, contains);
							break;
						}
					} else {
						contains = true;
						existingFileMap.put(doc, contains);
						break;
					}

				} else {
					existingFileMap.put(doc, contains);
				}
			}

		}

		return contains;
	}

	/**
	 * 
	 * @author sonntamo
	 */
	private void addExistingWSDLs(File[] wsdlList) {
		for (int i = 0; i < wsdlList.length; i++) {
			File wsdl = wsdlList[i];
			BufferedReader br = null;
			FileReader fr = null;
			try {
				fr = new FileReader(wsdl);
				br = new BufferedReader(fr);
				String line = null;
				boolean foundName = false;
				boolean foundTNS = false;
				boolean done = false;
				String name = "";
				String tns = "";
				while ((line = br.readLine()) != null
						&& !(foundName && foundTNS) && !done) {
					if (line.contains("name")) {
						name = line.substring(line.indexOf("name") + 6);
						name = name.substring(0, name.indexOf("\""));
						foundName = true;
					}
					if (line.toLowerCase().contains("targetnamespace")) {
						tns = line.substring(line.toLowerCase().indexOf(
								"targetnamespace") + 17);
						tns = tns.substring(0, tns.indexOf("\""));
						foundTNS = true;
					}

					/*
					 * This is to remove the closing tag of the XML version
					 * specification in the WSDL <?xml version="1.0"
					 * encoding="UTF-8"?>
					 */
					if (line.toLowerCase().contains("?>")) {
						line = line.replace("?>", "?");
					}
					/*
					 * This is to remove the closing tag of a comment <!-- -->
					 */
					if (line.toLowerCase().contains("-->")) {
						line = line.replace("-->", "?");
					}
					/*
					 * If we find a closing bracket, we have reached the end of
					 * the definitions-tag. We are done with our search for the
					 * name and targetnamespace.
					 */
					if (line.toLowerCase().contains(">")) {
						done = true;
					}
				}
				existingFiles
						.add(new WSDLImpl(wsdl.getName(), name, tns, null));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (fr != null)
						fr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					if (br != null)
						br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 
	 * @author sonntamo
	 */
	private void addExistingXSDs(File[] xsdList) {
		for (int i = 0; i < xsdList.length; i++) {
			File wsdl = xsdList[i];
			try {
				BufferedReader br = new BufferedReader(new FileReader(wsdl));
				String line = null;
				boolean foundTNS = false;
				String path = wsdl.getAbsolutePath();
				String tns = "";
				while ((line = br.readLine()) != null && !foundTNS) {
					if (line.toLowerCase().contains("targetnamespace")) {
						tns = line.substring(line.toLowerCase().indexOf(
								"targetnamespace") + 17);
						tns = tns.substring(0, tns.indexOf("\""));
						foundTNS = true;
					}
				}
				existingFiles.add(new XSDImpl(path, tns, null));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 * @param process
	 * @param pattern
	 * @return
	 * 
	 * @author sonntamo
	 */
	private File[] getFilesInProject(Process process, final String pattern) {
		IFile bpelFile = BPELUtil.getBPELFile(process);
		IPath bpelPath = bpelFile.getFullPath();
		IPath projectPath = bpelPath.removeFileExtension()
				.removeLastSegments(1);
		String workspacePath = ResourcesPlugin.getWorkspace().getRoot()
				.getLocation().toOSString();
		String absoluteWSPath = workspacePath + projectPath.toOSString();
		File file = new File(absoluteWSPath);
		if (file.isDirectory() && file.canRead()) {
			return file.listFiles(new FileFilter() {
				public boolean accept(File pathname) {
					if (pathname.isFile()) {
						return pathname.getName().toLowerCase()
								.endsWith(pattern);
					}
					return false;
				}

			});
		}
		return new File[] {};
	}

	public void calculateAllProcessFragmentConflicts() {
		calculateVariableConflicts();
		calculatePartnerLinkConflicts();
		calculateCorrelationSetConflicts();
		calculateMessageExchangeConflicts();
		calculateExtensionConflicts();
		calculateNamespaceConflicts();
	}

	private void calculateVariableConflicts() {
		Process process = this.model.process;

		this.conflictMap.get(BPELPackage.Literals.VARIABLE).clear();

		if (process.getVariables() != null
				&& fragmentProcess.getVariables() != null) {
			List<Variable> processVariables = process.getVariables()
					.getChildren();

			List<Variable> fragmentVariables = fragmentProcess.getVariables()
					.getChildren();

			for (Variable processVariable : processVariables) {
				for (Variable var : fragmentVariables) {
					if (var.equals(processVariable)) {
						this.conflictMap.get(var.eClass()).add(
								new Conflict(var, ConflictType.DUPLICATE));
					} else {
						if (var.getName().equals(processVariable.getName())) {
							ConflictType type = ConflictType.DUPLICATE;

							if (!CompareUtils.areVariablesEqual(var,
									processVariable)) {
								type = ConflictType.CONFLICT;
							}

							this.conflictMap.get(var.eClass()).add(
									new Conflict(var, type));
						}
					}
				}
			}
		}
	}

	private void calculatePartnerLinkConflicts() {
		Process process = this.model.process;

		this.conflictMap.get(BPELPackage.Literals.PARTNER_LINK).clear();

		if (process.getPartnerLinks() != null
				&& fragmentProcess.getPartnerLinks() != null) {
			List<PartnerLink> processPartnerLinks = process.getPartnerLinks()
					.getChildren();

			List<PartnerLink> fragmentPartnerLinks = fragmentProcess
					.getPartnerLinks().getChildren();

			for (PartnerLink processPartnerLink : processPartnerLinks) {
				for (PartnerLink ptlk : fragmentPartnerLinks) {
					if (ptlk.equals(processPartnerLink)) {
						this.conflictMap.get(ptlk.eClass()).add(
								new Conflict(ptlk, ConflictType.DUPLICATE));
					} else {
						if (ptlk.getName().equals(processPartnerLink.getName())) {
							ConflictType type = ConflictType.DUPLICATE;

							if (!CompareUtils.arePartnerLinksEqual(ptlk,
									processPartnerLink)) {
								type = ConflictType.CONFLICT;
							}

							this.conflictMap.get(ptlk.eClass()).add(
									new Conflict(ptlk, type));
						}
					}
				}
			}
		}
	}

	private void calculateCorrelationSetConflicts() {
		Process process = this.model.process;

		this.conflictMap.get(BPELPackage.Literals.CORRELATION_SET).clear();

		if (process.getCorrelationSets() != null
				&& fragmentProcess.getCorrelationSets() != null) {
			List<CorrelationSet> processCorrelationSets = process
					.getCorrelationSets().getChildren();

			List<CorrelationSet> fragmentCorrelationSets = fragmentProcess
					.getCorrelationSets().getChildren();

			for (CorrelationSet processCorrelationSet : processCorrelationSets) {
				for (CorrelationSet corrSet : fragmentCorrelationSets) {
					if (corrSet.equals(processCorrelationSet)) {
						this.conflictMap.get(corrSet.eClass()).add(
								new Conflict(corrSet, ConflictType.DUPLICATE));
					} else {
						if (corrSet.getName().equals(
								processCorrelationSet.getName())) {
							ConflictType type = ConflictType.DUPLICATE;

							if (corrSet.getProperties() != null
									&& processCorrelationSet.getProperties() != null) {
								if (!corrSet.getProperties().equals(
										processCorrelationSet.getProperties())) {
									type = ConflictType.CONFLICT;
								}
							}

							this.conflictMap.get(corrSet.eClass()).add(
									new Conflict(corrSet, type));
						}
					}
				}
			}
		}
	}

	private void calculateMessageExchangeConflicts() {
		Process process = this.model.process;

		this.conflictMap.get(BPELPackage.Literals.MESSAGE_EXCHANGE).clear();

		if (process.getMessageExchanges() != null
				&& fragmentProcess.getMessageExchanges() != null) {
			List<MessageExchange> processMessageExchanges = process
					.getMessageExchanges().getChildren();

			List<MessageExchange> fragmentMessageExchanges = fragmentProcess
					.getMessageExchanges().getChildren();

			for (MessageExchange processMessageExchange : processMessageExchanges) {
				for (MessageExchange mex : fragmentMessageExchanges) {
					if (mex.equals(processMessageExchange)) {
						this.conflictMap.get(mex.eClass()).add(
								new Conflict(mex, ConflictType.DUPLICATE));
					} else {
						if (mex.getName().equals(
								processMessageExchange.getName())) {
							this.conflictMap.get(mex.eClass()).add(
									new Conflict(mex, ConflictType.CONFLICT));
						}
					}
				}
			}
		}
	}

	private void calculateExtensionConflicts() {
		Process process = this.model.process;

		this.conflictMap.get(BPELPackage.Literals.EXTENSION).clear();

		if (process.getExtensions() != null
				&& fragmentProcess.getExtensions() != null) {
			List<Extension> processExtensions = process.getExtensions()
					.getChildren();

			List<Extension> fragmentExtensions = fragmentProcess
					.getExtensions().getChildren();

			for (Extension processExtension : processExtensions) {
				for (Extension extension : fragmentExtensions) {
					if (extension.equals(processExtension)) {
						this.conflictMap.get(extension.eClass())
								.add(new Conflict(extension,
										ConflictType.DUPLICATE));
					} else {
						if (extension.getNamespace() != null
								&& processExtension.getNamespace() != null) {
							if (extension.getNamespace().equals(
									processExtension.getNamespace())) {
								// Namespaces are equal -> DUPLICATE
								this.conflictMap.get(extension.eClass()).add(
										new Conflict(extension,
												ConflictType.DUPLICATE));

								if (!extension.getMustUnderstand().equals(
										processExtension.getMustUnderstand())) {
									// Namespaces are equal but the
									// mustUnderstand
									// flag is different -> CONFLICT
									this.conflictMap.get(extension.eClass())
											.add(new Conflict(extension,
													ConflictType.CONFLICT));
								}
							}
						}
					}
				}
			}
		}
	}

	private void calculateNamespaceConflicts() {
		INamespaceMap<String, String> processNS = BPELUtils
				.getNamespaceMap(model.process);
		INamespaceMap<String, String> fragmentNS = BPELUtils
				.getNamespaceMap(model.fragment);

		for (String prefix : processNS.keySet()) {
			String processNSURI = processNS.get(prefix);
			Namespace modelNS = WSDLFactory.eINSTANCE.createNamespace();
			modelNS.setPrefix(prefix);
			modelNS.setURI(processNSURI);
			if (fragmentNS.containsKey(prefix)) {
				String fragmentNSURI = fragmentNS.get(prefix);
				if (!fragmentNSURI.equals(processNSURI)) { // Same Prefix,
															// Different URI
					Namespace ns = WSDLFactory.eINSTANCE.createNamespace();
					ns.setPrefix(prefix);
					ns.setURI(fragmentNSURI);
					Conflict conflict = new Conflict(ns, ConflictType.CONFLICT);
					conflict.setAdditionalInfo(modelNS);
					if (!conflictMap.get(WSDLPackage.Literals.NAMESPACE)
							.contains(conflict)) {
						conflictMap.get(WSDLPackage.Literals.NAMESPACE).add(
								conflict);
					}
				}
			}
			for (String fragmentPrefix : fragmentNS.keySet()) {
				if (fragmentNS.get(fragmentPrefix).equals(processNSURI)
						&& !prefix.equals(fragmentPrefix)) { // Same URI,
																// Different
																// Prefix
					Namespace ns = WSDLFactory.eINSTANCE.createNamespace();
					ns.setPrefix(fragmentPrefix);
					ns.setURI(fragmentNS.get(fragmentPrefix));
					Conflict conflict = new Conflict(ns, ConflictType.DUPLICATE);
					conflict.setAdditionalInfo(modelNS);
					if (!conflictMap.get(WSDLPackage.Literals.NAMESPACE)
							.contains(conflict)) {
						conflictMap.get(WSDLPackage.Literals.NAMESPACE).add(
								conflict);
					}
				}
			}
		}
	}

	public static String[] getNameArrayFromEbjectList(
			EList<? extends EObject> children, EStructuralFeature feature) {
		List<String> names = new ArrayList<String>();

		for (EObject object : children) {
			names.add(object.eGet(feature).toString());
		}

		return names.toArray(new String[0]);
	}

	public void setCurrentListener(Listener listener) {
		this.listener = listener;
	}

	public List<EObject> getRemovedEObjects() {
		return removedEObjects;
	}

	public void removeConflictedElement(Conflict conflict) {
		// Get the list of conflicts for the corresponding class of EObjects
		List<Conflict> conflicts = this.conflictMap.get(conflict.getObject()
				.eClass());

		// Add the EObject to the removedEObjects list. This is used to add only
		// the non existing/conflicting associated elements of the fragment to
		// the process.
		this.removedEObjects.add(conflict.getObject());

		// Remove the conflict from the conflict map
		conflicts.remove(conflict);
	}

	public void reOpenEditor(final BPELEditor editor) {
		Display.getCurrent().asyncExec(new Runnable() {

			@Override
			public void run() {
				// Get the current active page
				IWorkbenchPage page = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage();

				if (page != null) {
					// Get the input of the editor
					IEditorInput editorInput = editor.getEditorInput();

					try {
						Thread.sleep(500);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}

					// Check if a instance for this editor is running
					MonitorManager manager = MonitoringProvider.getInstance()
							.getMonitorManager(editor.getMultipageEditor());
					boolean monitoringActive = false;
					String processPath = null;
					InstanceInformation instanceInfo = null;
					Process originalModel = null;
					if (manager != null) {
						monitoringActive = manager.isActive();
					}

					// Buffer some data if monitoring is active
					if (monitoringActive) {
						processPath = ResourcesPlugin.getWorkspace().getRoot()
								.getLocation().toOSString()
								+ editor.getMultipageEditor().getEditorFile()
										.getFullPath().toOSString();
						instanceInfo = manager.getInstanceInformation();
						originalModel = manager.getOriginalModel();
					}

					// Save and close the editor
					editor.getMultipageEditor().doSave(null);
					page.closeEditor(editor.getMultipageEditor(), false);

					try {
						Thread.sleep(500);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}

					// Open the editor again
					// If monitoring was active and sending requests to the
					// auditing application is enabled, use the InstanceHelper
					// to open
					// the editor and load the monitoring data.
					if (monitoringActive
							&& BPELUIPlugin.INSTANCE.getPreferenceStore()
									.getBoolean("SEND_REQUESTS")
							&& processPath != null && instanceInfo != null) {
						InstanceHelper.openInstance(processPath, instanceInfo,
								originalModel);
					} else {
						try {
							page.openEditor(editorInput,
									"org.eclipse.bpel.ui.bpeleditor", true);
						} catch (PartInitException e) {
							e.printStackTrace();
						}
					}
				} else {
					MessageDialog
							.openInformation(
									Display.getDefault().getActiveShell(),
									"Close & Open failed",
									"Please close the active editor and open the BPEL process model again to synchronize the inserted fragment.");
				}
			}

		});
	}

	private void updateNamespacePrefixes(Process fragment, String namespaceURI,
			String oldPrefix, String newPrefix) {
		TreeIterator<EObject> iter = fragment.eAllContents();

		while (iter.hasNext()) {
			EObject object = iter.next();

			switch (object.eClass().getClassifierID()) {
			case BPELPackage.VARIABLE:
				Variable var = (Variable) object;

				if (var.getXSDElement() != null) {
					String name = var.getXSDElement().getQName();
					if (name.startsWith(oldPrefix)) {
						name = name.replaceFirst(oldPrefix, newPrefix);

						var.getElement().setAttribute(BPELConstants.AT_ELEMENT,
								name);
					}
				}

				if (var.getType() != null) {
					String name = var.getType().getQName();
					if (name.startsWith(oldPrefix)) {
						name = name.replaceFirst(oldPrefix, newPrefix);

						var.getElement().setAttribute(BPELConstants.AT_TYPE,
								name);
					}
				}

				if (var.getMessageType() != null) {
					QName qname = var.getMessageType().getQName();

					if (qname.getNamespaceURI().equals(namespaceURI)) {
						var.getElement().setAttribute(
								BPELConstants.AT_MESSAGE_TYPE,
								newPrefix + ":" + qname.getLocalPart());
					}
				}
				break;

			case BPELPackage.PARTNER_LINK:
				PartnerLink partner = (PartnerLink) object;
				
				if (partner.getPartnerLinkType() instanceof PartnerLinkTypeProxy) {
				PartnerLinkTypeProxy proxy = (PartnerLinkTypeProxy) partner.getPartnerLinkType();
				if (proxy.getQName().getNamespaceURI().equals(namespaceURI)) {
					partner.getElement().setAttribute(
							BPELConstants.AT_PARTNER_LINK_TYPE,
							newPrefix + ":" + proxy.getQName().getLocalPart());
				}
				}
				break;
			}
		}
	}
}
