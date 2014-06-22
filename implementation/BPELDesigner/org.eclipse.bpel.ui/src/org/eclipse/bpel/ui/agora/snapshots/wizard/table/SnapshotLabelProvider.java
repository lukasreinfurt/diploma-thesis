package org.eclipse.bpel.ui.agora.snapshots.wizard.table;

import org.eclipse.bpel.ui.agora.ode135.client.TSnapshotInfo;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * This class is used to display {@link TSnapshotInfo} objects and their
 * data in a table.
 * 
 * @author hahnml
 * 
 */
public class SnapshotLabelProvider extends LabelProvider implements
		ITableLabelProvider {

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		TSnapshotInfo snapshot = (TSnapshotInfo) element;
		
		switch (columnIndex) {
		case 0:
			return snapshot.getActivityXPath();
		case 1:
			return String.valueOf(snapshot.getSnapshotVersion().size());
		default:
			throw new RuntimeException("Should not happen");
		}
	}

}
