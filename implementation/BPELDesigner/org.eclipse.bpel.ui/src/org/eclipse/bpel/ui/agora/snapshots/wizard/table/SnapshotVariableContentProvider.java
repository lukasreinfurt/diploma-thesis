package org.eclipse.bpel.ui.agora.snapshots.wizard.table;

import java.util.Collection;

import org.eclipse.bpel.ui.agora.ode135.client.TVariableInfo;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * An IStructuredContentProvider implementation for {@link TVariableInfo} objects.
 * 
 * @author hahnml
 *
 */
public class SnapshotVariableContentProvider implements IStructuredContentProvider {

	@Override
	public Object[] getElements(Object inputElement) {
		@SuppressWarnings("unchecked")
		Collection<VariableInfo> variables = (Collection<VariableInfo>) inputElement;
		return variables.toArray();
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

}
