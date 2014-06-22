package org.eclipse.bpel.ui.wizards.simtech.table;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * 
 * @author sonntamo
 */
public class ContentProvider implements IStructuredContentProvider {

	@SuppressWarnings("unchecked")
	public Object[] getElements(Object inputElement) {
//		List<WSDL> wsdls = (List<WSDL>) inputElement;
		List<Object> obj = (List<Object>) inputElement;
		return obj.toArray();
	}

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

}
