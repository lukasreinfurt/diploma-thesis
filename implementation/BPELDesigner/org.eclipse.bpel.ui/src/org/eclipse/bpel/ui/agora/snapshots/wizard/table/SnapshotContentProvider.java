package org.eclipse.bpel.ui.agora.snapshots.wizard.table;

import java.util.Collection;

import org.eclipse.bpel.ui.agora.ode135.client.TSnapshotInfo;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * An IStructuredContentProvider implementation for {@link TSnapshotInfo} objects.
 * 
 * @author hahnml
 *
 */
public class SnapshotContentProvider implements IStructuredContentProvider {

	@Override
	public Object[] getElements(Object inputElement) {
		@SuppressWarnings("unchecked")
		Collection<TSnapshotInfo> snapshots = (Collection<TSnapshotInfo>) inputElement;
		return snapshots.toArray();
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

}
