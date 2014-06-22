/**
 * 
 */
package org.eclipse.bpel.ui.palette.simtech;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.bpel.model.BPELExtensibleElement;
import org.eclipse.bpel.model.Process;
import org.eclipse.bpel.model.resource.BPELReader;
import org.eclipse.bpel.model.resource.BPELResource;
import org.eclipse.bpel.model.resource.BPELResourceImpl;
import org.eclipse.bpel.model.simtech.FragmentWrapper;
import org.eclipse.bpel.ui.BPELEditor;
import org.eclipse.bpel.ui.Policy;
import org.eclipse.bpel.ui.factories.AbstractUIObjectFactory;
import org.eclipse.bpel.ui.factories.IExtensionUIObjectFactory;
import org.eclipse.bpel.ui.simtech.gateway.FragmentImpl;
import org.eclipse.bpel.ui.util.BPELUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Used for two main purposes:
 * <ul>
 * <li>To create objects of the fragment that is dragged from the palette and
 * dropped onto a BPEL process.</li>
 * <li>To access the image of the fragment as it is displayed in the palette.</li>
 * </ul>
 * Note that the fragments are not realized as extension activities. Standard
 * BPEL code is injected into a process.
 * 
 * @author sonntamo
 */
public class FragmentUIObjectFactory extends AbstractUIObjectFactory implements
		IExtensionUIObjectFactory {

	/**
	 * Stores the BPEL fragment as DOM element.
	 */
	private FragmentImpl fragment;

	private BPELEditor bpelEditor;

	/**
	 * Creates a new fragment object factory.
	 * 
	 * @param fragment
	 *            The BPEL fragment as DOM element
	 */
	public FragmentUIObjectFactory(BPELEditor bpelEditor, FragmentImpl fragment) {
		super();
		this.fragment = fragment;
		this.bpelEditor = bpelEditor;
	}

	/**
	 * Forms a package identifier for the fragment based on the namespace URI
	 * ("http:///org/eclipse/bpel.ecore") and the fragment's name. For a
	 * fragment named "Invoke-Fragment" it would return
	 * "org.eclipse.bpel.Invoke-Fragment".
	 */
	protected String createUniqueIdString() {
		// (1) get the namespace URI of the fragment
		String ns = wrapper.getCompiledFragment().getElement()
				.getNamespaceURI();
		StringBuffer s = new StringBuffer(ns);
		// (2) remove colon-prefixes such as "http:" (if any),
		for (int i = s.indexOf(":"); i >= 0; i = s.indexOf(":"))s.delete(0, i + 1); //$NON-NLS-1$ //$NON-NLS-2$
		// (3) remove leading slashes (if any),
		while (s.length() > 0 && s.charAt(0) == '/')
			s.deleteCharAt(0);
		// (4) remove ".ecore" suffix (if any), and
		if (s.toString().endsWith(".ecore"))s.setLength(s.length() - 5); //$NON-NLS-1$
		// (5) convert slashes to periods.
		for (int i = 0; i < s.length(); i++)
			if (s.charAt(i) == '/')
				s.setCharAt(i, '.');

		if (s.length() > 0 && s.charAt(s.length() - 1) != '.')
			s.append('.');
		String name = wrapper.getFragment().getName();
		s.append(name);

		if (Policy.DEBUG)
			System.out.println("uniqueIdString for " + name + " is: " + s); //$NON-NLS-1$ //$NON-NLS-2$
		return s.toString();
	}

	/**
	 * Bild für Icon im PNG Format
	 * 
	 * @return Image
	 */
	@Override
	public Image getLargeImage() {
		ImageDescriptor myImageDescriptor = getLargeImageDescriptor();
		if (myImageDescriptor != null) {
			return myImageDescriptor.createImage();
		}
		return null;
	}

	/**
	 * Hier wird abhängig von der SimTech Aktivität das entsprechende Bild (im
	 * PNG Format) geladen
	 * 
	 * @return ImageDescriptor
	 */
	@Override
	public ImageDescriptor getLargeImageDescriptor() {
		ImageDescriptor x = null;
		try {
			URL url = new URL(fragment.getIconLargeURL());
			x = ImageDescriptor.createFromURL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return x;
	}

	@Override
	public EClass getModelType() {
		return null;
	}

	/**
	 * Bild für Icon im GIF Format
	 * 
	 * @return Image
	 */
	@Override
	public Image getSmallImage() {
		ImageDescriptor myImageDescriptor = getSmallImageDescriptor();
		if (myImageDescriptor != null) {
			return myImageDescriptor.createImage();
		}
		return null;
	}

	/**
	 * Hier wird abhängig von der SimTech Aktivität das entsprechende Bild (im
	 * GIF Format) geladen
	 * 
	 * @return ImageDescriptor
	 */
	@Override
	public ImageDescriptor getSmallImageDescriptor() {
		ImageDescriptor x = null;
		try {
			URL url = new URL(fragment.getIconSmallURL());
			x = ImageDescriptor.createFromURL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return x;
	}

	/*
	 * Not needed for fragment object factory.
	 */
	public String getTypeLabel() {
		return fragment.getName();
	}

	/*
	 * Not needed for fragment object factory.
	 */
	public EClass[] getClassArray() {
		EClass[] classArray = {};
		return classArray;
	}

	/*
	 * Not needed for fragment object factory.
	 */
	public void setModelType(EClass modelType) {
	}

	/**
	 * Creates an EMF model object of the fragment and wraps it by a special
	 * fragment wrapper.
	 * 
	 * @return EObject
	 */
	@SuppressWarnings(value = { "unchecked" })
	public <T extends EObject> T createInstance() {
		org.eclipse.bpel.model.Process process = bpelEditor.getProcess();

		if (process != null) {

			// This gets us the BPEL resource
			IFile bpelFile = BPELUtil.getBPELFile(process);
			IPath fullProcessPath = bpelFile.getFullPath();
			URI uri = URI.createPlatformResourceURI(fullProcessPath.toString(),
					false);
			BPELResource resource = new BPELResourceImpl(uri);

			// new BPEL reader
			FragmentBPELReader reader = new FragmentBPELReader();
			reader.setResource(resource);

			// first pass compilation
			BPELExtensibleElement extElement = null;

			// @hahnml: Check if we have a fragment containing a process
			if (fragment.containsProcess()) {
				// Compile the whole process with all associated elements
				reader.pass1(fragment.getBpelCode());

				// Get the process
				Process proc = (Process) reader.getResource().getContents()
						.get(0);

				// second pass compilation
				reader.pass2();

				extElement = proc;

				// // Get the associated elements and add them to the fragment
				// AssociatedFragmentElementsImpl associatedElements = new
				// AssociatedFragmentElementsImpl();
				// if (proc.getVariables() != null)
				// associatedElements.setVariables(proc.getVariables()
				// .getChildren());
				// if (proc.getPartnerLinks() != null)
				// associatedElements.setPartnerLinks(proc.getPartnerLinks()
				// .getChildren());
				// if (proc.getMessageExchanges() != null)
				// associatedElements.setMessageExchanges(proc
				// .getMessageExchanges().getChildren());
				// if (proc.getCorrelationSets() != null)
				// associatedElements.setCorrelationSets(proc
				// .getCorrelationSets().getChildren());
				// if (proc.getExtensions() != null)
				// associatedElements.setExtensions(proc.getExtensions()
				// .getChildren());
				//
				// fragment.setAssociatedElement(associatedElements);
			} else {
				Element actEl = (Element) fragment.getBpelCode()
						.getDocumentElement();
				extElement = reader.xml2Activity(actEl);

				// second pass compilation
				reader.pass2();
			}

			// The fragment wrapper contains all information associated with the
			// fragment (BPEL code, WSDL, ...).
			wrapper = new FragmentWrapper(extElement, fragment);
			return (T) wrapper;
		}
		return null;
	}

	FragmentWrapper wrapper = null;

	/**
	 * We need to call the pass2() method of the BPELReader but cannot access it
	 * from here since it is protected. This class extends the BPELReader so
	 * that pass2() can be called from the fragment object factory.
	 * 
	 * //@hahnml: Same issue for the xml2XXX methods
	 * 
	 * @author Mirko Sonntag
	 */
	public class FragmentBPELReader extends BPELReader {

		protected void pass1(Document document) {
			super.pass1(document);
		}

		/**
		 * Second compile step.
		 */
		protected void pass2() {
			super.pass2();
		}
	}
}