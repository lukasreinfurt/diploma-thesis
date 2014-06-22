package org.eclipse.bpel.ui.wizards.simtech;

import java.util.ArrayList;

import org.eclipse.bpel.model.BPELExtensibleElement;
import org.eclipse.bpel.model.Process;
import org.eclipse.bpel.model.simtech.DeploymentDescriptor;
import org.eclipse.bpel.model.simtech.WSDL;
import org.eclipse.bpel.model.simtech.XSD;

/**
 * 
 * @author sonntamo
 */
public class WizardModel {
	
	protected ArrayList<WSDL> wsdls;
	
	protected ArrayList<XSD> xsds;
	
	protected BPELExtensibleElement fragment;
	
	protected DeploymentDescriptor dd;
	
	public DeploymentDescriptor getDD() {
		return dd;
	}

	public void setDD(DeploymentDescriptor dd) {
		this.dd = dd;
	}

	protected Process process;
	
	public void setWsdls(ArrayList<WSDL> wsdlList) {
		this.wsdls = wsdlList;
	}
	
	public void setXsds(ArrayList<XSD> xsdList) {
		this.xsds = xsdList;
	}
	
	public void resetFlags() {
		for (WSDL wsdl: wsdls) {
			wsdl.setSelected(true);
		}
		for (XSD xsd: xsds) {
			xsd.setSelected(true);
		}
	}
	
	public void setProcess(Process process) {
		this.process = process;
	}
	
	public void setFragment(BPELExtensibleElement fragment) {
		this.fragment = fragment;
	}
}
