package org.eclipse.bpel.ui.wizards.simtech.documents;

import org.eclipse.bpel.model.simtech.DeploymentDescriptor;
import org.w3c.dom.Element;

public class DeploymentDescriptorImpl implements DeploymentDescriptor {

	public DeploymentDescriptorImpl(Element dd, Type type) {
		this.dd = dd;
		this.type = type;
	}
	
	Type type;
	
	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	Element dd;

	public Element getElement() {
		return dd;
	}

	public void setElement(Element element) {
		this.dd = element;
	}
	
}
