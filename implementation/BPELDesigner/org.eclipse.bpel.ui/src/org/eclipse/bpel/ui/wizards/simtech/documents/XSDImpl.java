package org.eclipse.bpel.ui.wizards.simtech.documents;

import org.eclipse.bpel.model.simtech.XSD;
import org.eclipse.bpel.ui.IBPELUIConstants;
import org.w3c.dom.Element;

/**
 * 
 * @author sonntamo
 */
public class XSDImpl extends BundleDocumentImpl implements XSD {

	public XSDImpl(String filename, String tns, Element element) {
		super(filename, tns, element);
	}
	
	public String getFileExtension() {
		return IBPELUIConstants.EXTENSION_XSD;
	}
}
