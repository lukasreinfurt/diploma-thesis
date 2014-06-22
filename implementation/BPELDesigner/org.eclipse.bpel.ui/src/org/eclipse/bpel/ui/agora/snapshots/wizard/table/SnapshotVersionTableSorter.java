package org.eclipse.bpel.ui.agora.snapshots.wizard.table;

import org.eclipse.bpel.ui.agora.ode135.client.TSnapshotVersion;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

/**
 * This class realizes a sorter for the table of {@link TSnapshotVersion} objects.
 * 
 * @author hahnml
 * 
 */
public class SnapshotVersionTableSorter extends ViewerSorter {
	private int propertyIndex;
	// private static final int ASCENDING = 0;
	private static final int DESCENDING = 1;

	private int direction = DESCENDING;

	public SnapshotVersionTableSorter() {
		this.propertyIndex = 0;
		direction = DESCENDING;
	}

	public void setColumn(int column) {
		if (column == this.propertyIndex) {
			// Same column as last sort; toggle the direction
			direction = 1 - direction;
		} else {
			// New column; do an ascending sort
			this.propertyIndex = column;
			direction = DESCENDING;
		}
	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		TSnapshotVersion in1 = (TSnapshotVersion) e1;
		TSnapshotVersion in2 = (TSnapshotVersion) e2;
		int rc = 0;
		switch (propertyIndex) {
		case 0:
			rc = in1.getSnapshotId().compareTo(in2.getSnapshotId());
			break;
		case 1:
			rc = in1.getVersion().compareTo(in2.getVersion());
			break;
		case 2:
			rc = in1.getCreated().toGregorianCalendar().compareTo(in2.getCreated().toGregorianCalendar());
			break;
		default:
			rc = 0;
		}
		// If descending order, flip the direction
		if (direction == DESCENDING) {
			rc = -rc;
		}
		return rc;
	}
}
