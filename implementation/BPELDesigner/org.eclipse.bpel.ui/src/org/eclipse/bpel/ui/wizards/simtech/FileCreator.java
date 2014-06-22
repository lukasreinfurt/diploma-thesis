package org.eclipse.bpel.ui.wizards.simtech;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.util.XMLUtils;
import org.eclipse.bpel.model.Activity;
import org.eclipse.bpel.model.BPELFactory;
import org.eclipse.bpel.model.Import;
import org.eclipse.bpel.model.Process;
import org.eclipse.bpel.model.adapters.INamespaceMap;
import org.eclipse.bpel.model.simtech.BundleDocument;
import org.eclipse.bpel.model.simtech.DeploymentDescriptor;
import org.eclipse.bpel.model.simtech.WSDL;
import org.eclipse.bpel.model.simtech.XSD;
import org.eclipse.bpel.model.util.BPELUtils;
import org.eclipse.bpel.ui.util.BPELUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.wst.wsdl.util.WSDLConstants;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * @author sonntamo
 */
public class FileCreator {

	/**
	 * Converts a DOM element to a well-formatted string.
	 * 
	 * @param element
	 *            The DOM element to convert
	 * @return the DOM element as formatted string
	 */
	static private String domElement2String(Element element) {
		try {
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer trans;
			trans = tf.newTransformer();

			StringWriter sw = new StringWriter();
			trans.transform(new DOMSource(element), new StreamResult(sw));
			return sw.toString();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		// in case of a failure an empty string is returned
		return "";
	}

	/**
	 * Imports the given documents into a process. This comprises both to create
	 * the documents in the project directory in the workspace and to create
	 * <code>&lt;import&gt;</code>s in the BPEL process. Note that all documents
	 * must have the same file extension!
	 * 
	 * @param <T>
	 *            type of the documents; must be a BundleDocument
	 * @param clazz
	 *            denotes the type of the document
	 * @param docs
	 *            list of the documents to import
	 * @param process
	 *            the process that imports the documents
	 * @param fileExtension
	 *            the file extension of the documents. All imported documents
	 *            must have the same file extension, e.g. "wsdl", "xsd"
	 * @param compiledFragment
	 *            the compiled process fragment. Is needed to determine the
	 *            namespace prefix of the document to import
	 * 
	 */
	static private <T extends BundleDocument> void createFilesForImport(
			Class<T> clazz, ArrayList<T> docs, Process process,
			Activity compiledFragment) {
		IFile bpelFile = BPELUtil.getBPELFile(process);
		IPath bpelPath = bpelFile.getFullPath();
		IPath projectPath = bpelPath.removeFileExtension()
				.removeLastSegments(1);

		String absoluteWorkspacePath = ResourcesPlugin.getWorkspace().getRoot()
				.getLocation().toOSString();
		for (BundleDocument doc : docs) {
			if (!doc.isSelected())
				continue;
			createFileInWorkspace(doc, projectPath, absoluteWorkspacePath);
			createBpelImport(doc, process, compiledFragment);
		}
	}

	/**
	 * Imports the given WSDLs into the given process. This is one step of the
	 * insertion of a fragment into a process. The WSDLs are both created in the
	 * project directory and imported into the process.
	 * 
	 * @param wsdls
	 *            the WSDLs to import
	 * @param process
	 *            the process to import the WSDLs into
	 * @param compiledFragment
	 *            the fragment to insert
	 */
	static public void createWsdlsForImport(ArrayList<WSDL> wsdls,
			Process process, Activity compiledFragment) {
		createFilesForImport(WSDL.class, wsdls, process, compiledFragment);
	}

	static private void createFileInWorkspace(BundleDocument doc,
			IPath projectPath, String absoluteWorkspacePath) {
		String fileExtension = doc.getFileExtension();
		String fileName = doc.getFilename();
		if (fileName.endsWith("." + fileExtension))
			fileName = fileName.substring(0,
					fileName.length() - fileExtension.length() - 1);
		IPath docPath = projectPath.append(fileName).addFileExtension(
				fileExtension);
		File docFile = new File(absoluteWorkspacePath + docPath.toOSString());
		if (!docFile.exists()) {
			OutputStreamWriter out = null;
			try {
				if (docFile.createNewFile()) {

					out = new OutputStreamWriter(new FileOutputStream(
							docFile.getAbsolutePath()), "UTF-8");
					String domAsString = domElement2String(doc.getElement());
					out.write(domAsString);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (out != null) {
					try {
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * Imports the given document into the given BPEL process.
	 * 
	 * @param doc
	 *            the document to import
	 * @param process
	 *            the process to import the document into
	 * @param compiledFragment
	 *            the compiled fragment is used to get the namespace prefix for
	 *            the document to import
	 */
	static private void createBpelImport(BundleDocument doc, Process process,
			Activity compiledFragment) {
		boolean exist = false;
		for (Import i : process.getImports()) {
			if (i.getLocation().equals(doc.getFilename())) {
				exist = true;
			}
		}
		if (!exist) {
			Import docImport = BPELFactory.eINSTANCE.createImport();
			String importType;
			if (doc instanceof WSDL) {
				importType = WSDLConstants.WSDL_NAMESPACE_URI;
			} else {
				importType = WSDLConstants.XSD_NAMESPACE_URI;
			}
			docImport.setImportType(importType);
			docImport.setLocation(doc.getFilename());
			docImport.setNamespace(doc.getTargetNamespace());
			process.getImports().add(docImport);

			/*
			 * if the namespace is already associated with a prefix, we take
			 * this prefix and add the namespace declaration on the process
			 * element
			 */
			String prefix = BPELUtils.getNamespacePrefix(compiledFragment,
					doc.getTargetNamespace());
			if (prefix != null) {
				INamespaceMap<String, String> nsMap = BPELUtils
						.getNamespaceMap(process);
				nsMap.put(prefix, doc.getTargetNamespace());
			} else {
				// @hahnml: Add a new prefix for the added namespace
				// Use the first element of the namespace as prefix, e.g.
				// "http://example.bpel.org" with prefix "example"
				prefix = doc.getTargetNamespace().substring(
						doc.getTargetNamespace().indexOf("//") + 2,
						doc.getTargetNamespace().indexOf("."));
				INamespaceMap<String, String> nsMap = BPELUtils
						.getNamespaceMap(process);
				nsMap.put(prefix, doc.getTargetNamespace());
			}
		}
	}

	/**
	 * Imports the given XSDs into the given process. This is one step of the
	 * insertion of a fragment into a process. The XSDs are both created in the
	 * project directory and imported into the process.
	 * 
	 * @param xsds
	 *            the XSDs to import
	 * @param process
	 *            the process to import the XSDs into
	 * @param compiledFragment
	 *            the fragment to insert
	 */
	static public void createXsdsForImport(ArrayList<XSD> xsds,
			Process process, Activity compiledFragment) {
		createFilesForImport(XSD.class, xsds, process, compiledFragment);
	}

	static public void updateDD(DeploymentDescriptor dd, Process process) {
		IFile bpelFile = BPELUtil.getBPELFile(process);
		IPath bpelPath = bpelFile.getFullPath().removeFileExtension()
				.removeLastSegments(1);
		IPath relativePath = bpelPath.makeRelativeTo(bpelPath
				.removeLastSegments(1));

		switch (dd.getType()) {
		case ApacheODE:
			String fileExtension = "xml";
			String fileName = "deploy";
			IPath ddPath = relativePath.append(fileName).addFileExtension(
					fileExtension);

			// @hahnml: Changed to IFile to synchronize the workspace after the
			// file content was changed to avoid a ResourceException
			IFile ddFile = bpelFile.getProject().getFile(ddPath);
			if (ddFile.exists()) {
				try {
					// @hahnml: Check if resource is in sync
					if (!ddFile.isSynchronized(IResource.DEPTH_ZERO)) {
						ddFile.refreshLocal(IResource.DEPTH_ZERO, null);
					}

					InputStream fis = ddFile.getContents();
					XMLInputFactory xif = XMLInputFactory.newInstance();
					XMLStreamReader reader = xif.createXMLStreamReader(fis);
					StAXOMBuilder builder = new StAXOMBuilder(reader);
					OMElement omOdeDD = builder.getDocumentElement();

					// we assume that there is only one process defined in the
					// DD
					OMElement omProcess = omOdeDD.getFirstElement();
					Element elem = dd.getElement();
					NodeList list = elem.getElementsByTagNameNS(
							"http://www.apache.org/ode/schemas/dd/2007/03",
							"deploy");
					Node deployNode = null;

					// @hahnml: If we load the DD from the file system,
					// dd.getElement() returns directly the deploy element and
					// so we don't have to search the child nodes
					if (list.getLength() == 0) {
						if (elem.getLocalName().equals("deploy")
								&& elem.getNamespaceURI()
										.equals("http://www.apache.org/ode/schemas/dd/2007/03"))
							deployNode = elem;
					} else {
						deployNode = list.item(0);
					}

					NodeList childList = deployNode.getChildNodes();
					for (int i = 0; i < childList.getLength(); i++) {
						Node node = childList.item(i);
						if (node.getNodeType() == Node.ELEMENT_NODE) {
							try {
								Element elNode = (Element) node;
								OMElement omEl;
								omEl = XMLUtils.toOM(elNode);
								// @hahnml: Check if a provide/invoke element
								// for the same partnerLink exists already
								if (!containsElement(omProcess, omEl)) {
									omProcess.addChild(omEl);
								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}
					}

					builder.releaseParserOnClose(true);
					omOdeDD.build();
					omOdeDD.detach();
					try {
						fis.close();
						reader.close();
						builder.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

					System.out.println(omOdeDD);

					StringWriter strWriter = new StringWriter();
					XMLOutputFactory xof = XMLOutputFactory.newInstance();
					XMLStreamWriter writer = xof
							.createXMLStreamWriter(strWriter);

					omOdeDD.serialize(writer);

					InputStream source = new ByteArrayInputStream(strWriter
							.toString().getBytes());
					ddFile.setContents(source, IFile.FORCE, null);
				} catch (XMLStreamException e) {
					e.printStackTrace();
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			break;
		}
	}

	/**
	 * Checks if the deployment descriptor contains already a provide/invoke
	 * element for the partnerLink specified by the provided element.
	 * 
	 * @param omProcess The process element of the deployment descriptor to search in.
	 * @param omEl The element which should be added (provide or invoke).
	 * @return If the deployment descriptor contains the provided element already. 
	 * 
	 * @author hahnml
	 */
	private static boolean containsElement(OMElement omProcess, OMElement omEl) {
		boolean exists = false;

		QName plQName = new QName("partnerLink");

		@SuppressWarnings("unchecked")
		Iterator<OMElement> children = omProcess.getChildElements();
		while (exists == false && children.hasNext()) {
			OMElement elm = children.next();
			if (elm.getLocalName().equals("invoke")
					&& omEl.getLocalName().equals("invoke")) {
				if (elm.getAttributeValue(plQName).equals(
						omEl.getAttributeValue(plQName))) {
					exists = true;
				}
			} else {
				if (elm.getLocalName().equals("provide")
						&& omEl.getLocalName().equals("provide")) {
					if (elm.getAttributeValue(plQName).equals(
							omEl.getAttributeValue(plQName))) {
						exists = true;
					}
				}
			}
		}

		return exists;
	}
}
