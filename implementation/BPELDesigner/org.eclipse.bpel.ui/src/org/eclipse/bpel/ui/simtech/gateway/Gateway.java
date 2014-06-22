package org.eclipse.bpel.ui.simtech.gateway;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.util.XMLUtils;
import org.eclipse.bpel.model.simtech.BundleDocument;
import org.eclipse.bpel.model.simtech.DeploymentDescriptor;
import org.eclipse.bpel.model.simtech.WSDL;
import org.eclipse.bpel.model.simtech.XSD;
import org.eclipse.bpel.ui.wizards.simtech.documents.DeploymentDescriptorImpl;
import org.eclipse.bpel.ui.wizards.simtech.documents.WSDLImpl;
import org.eclipse.bpel.ui.wizards.simtech.documents.XSDImpl;
import org.eclipse.wst.sse.core.internal.model.ModelManagerImpl;
import org.eclipse.wst.sse.core.internal.provisional.IModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.xml.core.internal.document.DOMModelImpl;
import org.eclipse.wst.xml.core.internal.document.DocumentImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * 
 * @author sonntamo
 */
@SuppressWarnings("restriction")
public class Gateway {

	private static final String ns = "http://repository.simtech.ustutt.de/xsd";

	@SuppressWarnings("rawtypes")
	static public ArrayList<FragmentImpl> getRegisteredFragments(
			String endpoint, String operation) throws AxisFault {
		ArrayList<FragmentImpl> fragmentList = new ArrayList<FragmentImpl>();

		Options opts = new Options();
		opts.setTo(new EndpointReference(endpoint));
		opts.setAction(operation);
		ServiceClient workflowClient = null;
		try {
			workflowClient = new ServiceClient();
			workflowClient.setOptions(opts);
			OMElement res = workflowClient.sendReceive(null);
			Iterator it = res.getChildElements();

			int id = 0;
			while (it.hasNext()) {
				id++;
				OMElement ret = (OMElement) it.next();
				Iterator childIt = ret.getChildElements();
				FragmentImpl fragment = new FragmentImpl();
				ArrayList<WSDL> wsdlList = null;
				ArrayList<XSD> xsdList = null;
				Document content = null;
				DeploymentDescriptor dd = null;
				while (childIt.hasNext()) {
					OMNode node = (OMNode) childIt.next();
					if (node instanceof OMElement) {
						OMElement elem = (OMElement) node;
						if (elem.getQName().equals(new QName(ns, "name"))) {
							fragment.setName(elem.getText());
						} else if (elem.getQName().equals(
								new QName(ns, "description"))) {
							fragment.setDescription(elem.getText());
						} else if (elem.getQName().equals(
								new QName(ns, "author"))) {
							fragment.setAuthor(elem.getText());
						} else if (elem.getQName().equals(
								new QName(ns, "iconSmall"))) {
							fragment.setIconSmallURL(elem.getText());
						} else if (elem.getQName().equals(
								new QName(ns, "iconLarge"))) {
							fragment.setIconLargeURL(elem.getText());
						} else if (elem.getQName().equals(
								new QName(ns, "fragment"))) {
							content = toElement(elem, "fragment", id);
						} else if (elem.getQName().equals(
								new QName(ns, "deploymentDescriptor"))) {
							String xsiNil = elem
									.getAttributeValue(new QName(
											"http://www.w3.org/2001/XMLSchema-instance",
											"nil"));
							if (xsiNil != null && xsiNil.equals("true")) {
								continue;
							}
							Element el;
							try {
								el = XMLUtils.toDOM(elem);
								dd = new DeploymentDescriptorImpl(el,
										DeploymentDescriptor.Type.ApacheODE);
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else if (elem.getQName().equals(
								new QName(ns, "wsdlList"))) {
							wsdlList = toList(elem, WSDL.class, WSDLImpl.class);
						} else if (elem.getQName().equals(
								new QName(ns, "xsdList"))) {
							xsdList = toList(elem, XSD.class, XSDImpl.class);
						}
					}
				}
				fragment.setDD(dd);
				fragment.setBpelCode(content);
				fragment.setWsdls(wsdlList);
				fragment.setXsds(xsdList);
				fragmentList.add(fragment);
			}
		} finally {
			if (workflowClient != null) {
				workflowClient.cleanup();
				workflowClient.cleanupTransport();
			}
		}
		return fragmentList;
	}

	@SuppressWarnings("deprecation")
	private static Document toElement(OMElement elem, String modelName, int id) {
		try {
			IModelManager manager = ModelManagerImpl.getInstance();
			IStructuredModel mod = manager.getModelForEdit(modelName + id,
					new ByteArrayInputStream(elem.getFirstElement().toString()
							.getBytes()), null);
			DOMModelImpl mimp = (DOMModelImpl) mod;
			DocumentImpl iFragment = (DocumentImpl) mimp.getDocument();
			return iFragment;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static <T extends BundleDocument> ArrayList<T> toList(
			OMElement elem, Class<T> iFace, Class clazz) {
		ArrayList<T> docList = new ArrayList<T>();
		try {
			Iterator it = elem.getChildElements();
			while (it.hasNext()) {
				OMElement omEl = (OMElement) it.next();
				Element el = null;
				String name = "";
				String filename = "";
				String tns = "";
				Iterator childIt = omEl.getChildElements();
				while (childIt.hasNext()) {
					OMElement omChild = (OMElement) childIt.next();
					if (omChild.getQName().equals(new QName(ns, "name"))) {
						name = omChild.getText();
					} else if (omChild.getQName().equals(
							new QName(ns, "filename"))) {
						filename = omChild.getText();
					} else if (omChild.getQName().equals(new QName(ns, "tns"))) {
						tns = omChild.getText();
					} else {
						// it's the WSDL/XSD content
						el = XMLUtils.toDOM(omChild);
					}
				}

				T obj;
				if (clazz.equals(WSDLImpl.class)) {
					Constructor<T> ctor = clazz.getConstructor(new Class[] {
							String.class, String.class, String.class,
							Element.class });
					Object[] initargs = new Object[] { filename, name, tns, el };
					obj = ctor.newInstance(initargs);
				} else {
					Constructor<T> ctor = clazz.getConstructor(new Class[] {
							String.class, String.class, Element.class });
					Object[] initargs = new Object[] { filename, tns, el };
					obj = ctor.newInstance(initargs);
				}
				docList.add(obj);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return docList;
	}

	// @hahnml: Gets the fragments from a file system path
	@SuppressWarnings("deprecation")
	public static ArrayList<FragmentImpl> getRegisteredFragments(
			String repositoryPath) throws IOException {
		ArrayList<FragmentImpl> fragmentList = new ArrayList<FragmentImpl>();

		// Get all fragment bundle folders from the repository
		File repository = new File(repositoryPath);
		if (!repository.exists()) {
			throw new FileNotFoundException(
					"The export directory for fragments was not found at: "
							+ repositoryPath);
		}
		File[] bundles = repository.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				if (file.isDirectory() && isValidFragment(file)) {
					return true;
				} else {
					return false;
				}
			}
		});

		// Create a fragment for every bundle directory in the repository
		for (File fragmentBundle : bundles) {

			FragmentImpl fragment = new FragmentImpl();
			boolean containsProcess = false;

			// Create new lists
			ArrayList<WSDL> wsdlList = new ArrayList<WSDL>();
			ArrayList<XSD> xsdList = new ArrayList<XSD>();

			// Read the content of the fragment bundle
			File[] files = fragmentBundle.listFiles();

			for (File file : files) {
				int dot = file.getPath().lastIndexOf(".");
				String extension = file.getPath().substring(dot + 1);

				// Read the deployment descriptor
				if (file.getName().equals("ApacheODE-DD.xml")) {

					try {
						DocumentBuilderFactory dbf = DocumentBuilderFactory
								.newInstance();
						dbf.setNamespaceAware(true);
						DocumentBuilder docb = dbf.newDocumentBuilder();
						Document doc = docb.parse(file);

						DeploymentDescriptor dd = new DeploymentDescriptorImpl(
								doc.getDocumentElement(),
								DeploymentDescriptor.Type.ApacheODE);

						fragment.setDD(dd);
					} catch (ParserConfigurationException e) {
						// TODO Fehlermeldung
						e.printStackTrace();
					} catch (SAXException e) {
						// TODO Fehlermeldung
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Fehlermeldung
						e.printStackTrace();
					}
				} else if (extension.equals("bpel")) {
					// Read the fragment content
					IModelManager manager = ModelManagerImpl.getInstance();

					IStructuredModel mod = manager.getModelForRead(file
							.getAbsolutePath(), new BufferedInputStream(
							new FileInputStream(file)), null);
					DOMModelImpl mimp = (DOMModelImpl) mod;
					DocumentImpl doc = (DocumentImpl) mimp.getDocument();

					Element process = doc.getDocumentElement();

					// Check if we got an activity with process envelope or
					// not
					if (process.getLocalName().equals("process")) {
						containsProcess = true;
					}

					fragment.setBpelCode(doc);

					// Release the StructuredModel from the ModelManager
					mod.releaseFromRead();

				} else if (extension.equals("wsdl")) {

					try {
						// Read the wsdl file content
						DocumentBuilderFactory dbf = DocumentBuilderFactory
								.newInstance();
						dbf.setNamespaceAware(true);
						DocumentBuilder db = dbf.newDocumentBuilder();
						Element content = db.parse(file).getDocumentElement();

						// Get the name of the WSDL
						String name = "";
						if (content.hasAttribute("name")) {
							name = content.getAttribute("name");
						}

						// Read the target namespace
						String tns = getTargetNamespace(content);

						// Create the WSDL object and add it to the list
						wsdlList.add(new WSDLImpl(file.getName(), name, tns,
								content));
					} catch (ParserConfigurationException e) {
						// TODO Fehlermeldung
						e.printStackTrace();
					} catch (SAXException e) {
						// TODO Fehlermeldung
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Fehlermeldung
						e.printStackTrace();
					}

				} else if (extension.equals("xsd")) {

					try {
						// Read the xsd file content
						DocumentBuilderFactory dbf = DocumentBuilderFactory
								.newInstance();
						dbf.setNamespaceAware(true);
						DocumentBuilder db = dbf.newDocumentBuilder();
						Element content = db.parse(file).getDocumentElement();

						// Read the target namespace
						String tns = getTargetNamespace(content);

						// Create the XSD object and add it to the list
						xsdList.add(new XSDImpl(file.getName(), tns, content));
					} catch (ParserConfigurationException e) {
						// TODO Fehlermeldung
						e.printStackTrace();
					} catch (SAXException e) {
						// TODO Fehlermeldung
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Fehlermeldung
						e.printStackTrace();
					}

				} else if (file.getName().equals("config.xml")) {
					// Read the config file
					ConfigHandler reader = new ConfigHandler(file);
					reader.loadPropertiesFromXMLFile();

					try {

						fragment.setName(reader
								.getProperty(ConfigHandler.PROPERTY_NAME));

						fragment.setDescription(reader
								.getProperty(ConfigHandler.PROPERTY_DESCRIPTION));

						fragment.setAuthor(reader
								.getProperty(ConfigHandler.PROPERTY_AUTHOR));

						//@hahnml: Get the absolute path to make the relative icon path absolute again
						String parent = file.getParent();
						File icon = new File(parent, 
								reader.getProperty(ConfigHandler.PROPERTY_ICON));
						
						fragment.setIconSmallURL(icon.toURI().toURL()
								.toString());

						fragment.setIconLargeURL(icon.toURI().toURL()
								.toString());
					} catch (MalformedURLException e) {
						// TODO Fehlermeldung
						e.printStackTrace();
					}
				}

			}

			fragment.setWsdls(wsdlList);
			fragment.setXsds(xsdList);

			fragment.setContainsProcess(containsProcess);

			fragmentList.add(fragment);
		}

		return fragmentList;
	}

	protected static boolean isValidFragment(File file) {
		boolean isValid = false;
		
		if (file.isDirectory()) {
			for (File tmp : file.listFiles()) {
				if (tmp.getName().endsWith(".bpel")) {
					isValid = true;
					break;
				}
			}
		}
		
		return isValid;
	}

	private static String getTargetNamespace(Element content) {
		String targetNamespace = "";

		if (content.hasAttribute("targetNamespace")) {
			targetNamespace = content.getAttribute("targetNamespace");
		}

		return targetNamespace;
	}

}
