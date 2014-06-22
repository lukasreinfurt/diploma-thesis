package org.eclipse.bpel.ui.agora.instances;

import java.util.Collection;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.simtech.workflow.ode.auditing.communication.messages.InstanceInformation;

/**
 * An IStructuredContentProvider implementation for InstanceInformation objects.
 * 
 * @author hahnml
 *
 */
public class InstanceContentProvider implements IStructuredContentProvider {

	@Override
	public Object[] getElements(Object inputElement) {
		@SuppressWarnings("unchecked")
		Collection<InstanceInformation> instances = (Collection<InstanceInformation>) inputElement;
		return instances.toArray();
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

}
