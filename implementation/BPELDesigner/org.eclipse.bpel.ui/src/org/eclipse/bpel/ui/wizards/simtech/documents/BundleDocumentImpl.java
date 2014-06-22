package org.eclipse.bpel.ui.wizards.simtech.documents;

import org.eclipse.bpel.model.simtech.BundleDocument;
import org.w3c.dom.Element;

/**
 * 
 * @author sonntamo
 */
public abstract class BundleDocumentImpl implements BundleDocument {
	
	Element element;

	String targetNamespace;

	String filename;

	boolean selected = true;
	
	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	public BundleDocumentImpl(String filename, String tns, Element element) {
		this.filename = filename;
		this.targetNamespace = tns;
		this.element = element;
	}

	public Element getElement() {
		return element;
	}

	public String getFilename() {
		return filename;
	}

	public String getTargetNamespace() {
		return targetNamespace;
	}
	public void setElement(Element element) {
		this.element = element;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	public void setTargetNamespace(String targetNamespace) {
		this.targetNamespace = targetNamespace;
	}
}
