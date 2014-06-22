package org.eclipse.bpel.ui.agora.debug.views;

import org.eclipse.bpel.debug.debugmodel.Breakpoint;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

public class BreakpointTableSorter extends ViewerSorter {
	private int propertyIndex;
	// private static final int ASCENDING = 0;
	private static final int DESCENDING = 1;

	private int direction = DESCENDING;

	public BreakpointTableSorter() {
		this.propertyIndex = 1;
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
		Breakpoint breakpoint1 = (Breakpoint) e1;
		Breakpoint breakpoint2 = (Breakpoint) e2;
		int rc = 0;
		switch (propertyIndex) {
		case 0:
			rc = String.valueOf(breakpoint1.isEnabled()).compareToIgnoreCase(String.valueOf(breakpoint2.isEnabled()));
			break;
		case 1:
			rc = breakpoint1.getName().compareToIgnoreCase(breakpoint2.getName());
			break;
		case 2:
			rc = breakpoint1.getTargetXPath().compareToIgnoreCase(breakpoint2.getTargetXPath());
			break;
		case 3:
			rc = breakpoint1.getTargetName().compareToIgnoreCase(breakpoint2.getTargetName());
			break;
		case 4:
			rc = BreakpointViewUtils.getStringRepresentation(breakpoint1.getType()).compareToIgnoreCase(BreakpointViewUtils.getStringRepresentation(breakpoint2.getType()));
			break;
		case 5:
			rc = breakpoint1.getState().getName().compareToIgnoreCase(breakpoint2.getState().getName());
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
