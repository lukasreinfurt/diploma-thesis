package org.eclipse.bpel.ui.agora.snapshots.wizard.table;

import java.util.Collection;

import org.eclipse.bpel.ui.agora.ode135.client.TSnapshotVersion;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * An IStructuredContentProvider implementation for {@link TSnapshotVersion} objects.
 * 
 * @author hahnml
 *
 */
public class SnapshotVersionContentProvider implements IStructuredContentProvider {

	@Override
	public Object[] getElements(Object inputElement) {
		@SuppressWarnings("unchecked")
		Collection<TSnapshotVersion> snapshots = (Collection<TSnapshotVersion>) inputElement;
		return snapshots.toArray();
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

}
