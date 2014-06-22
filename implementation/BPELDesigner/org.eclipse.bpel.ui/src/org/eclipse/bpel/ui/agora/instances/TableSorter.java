package org.eclipse.bpel.ui.agora.instances;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.simtech.workflow.ode.auditing.communication.messages.InstanceInformation;

/**
 * This class realizes a sorter for the table of InstanceInformation objects.
 * 
 * @author hahnml
 * 
 */
public class TableSorter extends ViewerSorter {
	private int propertyIndex;
	// private static final int ASCENDING = 0;
	private static final int DESCENDING = 1;

	private int direction = DESCENDING;

	public TableSorter() {
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
		InstanceInformation in1 = (InstanceInformation) e1;
		InstanceInformation in2 = (InstanceInformation) e2;
		int rc = 0;
		switch (propertyIndex) {
		case 0:
			rc = in1.getInstanceID().compareTo(in2.getInstanceID());
			break;
		case 1:
			rc = in1.getProcessName().toString().compareTo(in2.getProcessName().toString());
			break;
		case 2:
			rc = in1.getProcessVersion().compareTo(in2.getProcessVersion());
			break;
		case 3:
			rc = in1.getState().compareTo(in2.getState());
			break;
		case 4:
			rc = in1.getTimestamp().compareTo(in2.getTimestamp());
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
