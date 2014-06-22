package org.eclipse.bpel.ui.agora.snapshots.wizard.table;

import java.util.Collection;

import org.eclipse.bpel.ui.agora.ode135.client.TPartnerLinkInfo;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * An IStructuredContentProvider implementation for {@link TPartnerLinkInfo} objects.
 * 
 * @author hahnml
 *
 */
public class SnapshotPartnerLinkContentProvider implements IStructuredContentProvider {

	@Override
	public Object[] getElements(Object inputElement) {
		@SuppressWarnings("unchecked")
		Collection<PartnerLinkInfo> partnerLinks = (Collection<PartnerLinkInfo>) inputElement;
		return partnerLinks.toArray();
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

}
