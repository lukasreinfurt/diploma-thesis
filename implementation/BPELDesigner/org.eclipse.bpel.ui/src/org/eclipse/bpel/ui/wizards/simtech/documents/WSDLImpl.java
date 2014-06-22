package org.eclipse.bpel.ui.wizards.simtech.documents;

import org.eclipse.bpel.model.simtech.WSDL;
import org.eclipse.bpel.ui.IBPELUIConstants;
import org.w3c.dom.Element;

/**
 * 
 * @author sonntamo
 */
public class WSDLImpl extends BundleDocumentImpl implements WSDL {

	String name;

	public WSDLImpl(String filename, String tns, Element element) {
		super(filename, tns, element);
	}

	public WSDLImpl(String filename, String name, String tns, Element element) {
		super(filename, tns, element);
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getFileExtension() {
		return IBPELUIConstants.EXTENSION_WSDL;
	}
}
