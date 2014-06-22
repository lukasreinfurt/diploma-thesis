package org.eclipse.bpel.ui.simtech.gateway;

import java.awt.Image;
import java.util.ArrayList;

import org.eclipse.bpel.model.simtech.DeploymentDescriptor;
import org.eclipse.bpel.model.simtech.Fragment;
import org.eclipse.bpel.model.simtech.WSDL;
import org.eclipse.bpel.model.simtech.XSD;
import org.w3c.dom.Document;

/**
 * 
 * @author sonntamo
 */
public class FragmentImpl implements Fragment {

	String name;
	
	String author;
	
	String description;
	
	String iconSmallURL;
	
	String iconLargeURL;
	
	DeploymentDescriptor dd;
	
	boolean containsProcess = false;
		
	public String getIconSmallURL() {
		return iconSmallURL;
	}

	public void setIconSmallURL(String iconSmallURL) {
		this.iconSmallURL = iconSmallURL;
	}

	public String getIconLargeURL() {
		return iconLargeURL;
	}

	public void setIconLargeURL(String iconLargeURL) {
		this.iconLargeURL = iconLargeURL;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	Document bpelCode;
	
	ArrayList<WSDL> wsdls;
	
	ArrayList<XSD> xsds; 
	
	public ArrayList<WSDL> getWsdls() {
		return wsdls;
	}

	public void setWsdls(ArrayList<WSDL> wsdls) {
		this.wsdls = wsdls;
	}

	public ArrayList<XSD> getXsds() {
		return xsds;
	}

	public void setXsds(ArrayList<XSD> xsds) {
		this.xsds = xsds;
	}

	Image smallImage;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public Document getBpelCode() {
		return bpelCode;
	}

	public void setBpelCode(Document bpelCode) {
		this.bpelCode = bpelCode;
	}

	public Image getSmallImage() {
		return smallImage;
	}

	public void setSmallImage(Image smallImage) {
		this.smallImage = smallImage;
	}

	public Image getLargeImage() {
		return largeImage;
	}

	public void setLargeImage(Image largeImage) {
		this.largeImage = largeImage;
	}

	Image largeImage;

	public DeploymentDescriptor getDD() {
		return dd;
	}

	public void setDD(DeploymentDescriptor dd) {
		this.dd = dd;
	}
	
	@Override
	public boolean containsProcess() {
		return this.containsProcess;
	}

	@Override
	public void setContainsProcess(boolean containsProcess) {
		this.containsProcess = containsProcess;
	}
}
