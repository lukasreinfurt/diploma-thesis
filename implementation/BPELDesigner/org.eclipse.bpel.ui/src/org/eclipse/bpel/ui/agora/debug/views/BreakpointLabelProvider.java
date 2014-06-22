package org.eclipse.bpel.ui.agora.debug.views;

import org.eclipse.bpel.debug.debugmodel.Breakpoint;
import org.eclipse.bpel.ui.BPELUIPlugin;
import org.eclipse.bpel.ui.IBPELUIConstants;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

public class BreakpointLabelProvider extends LabelProvider implements
		ITableLabelProvider {

	@Override
	public String getColumnText(Object element, int columnIndex) {
		Breakpoint breakpoint = (Breakpoint) element;

		switch (columnIndex) {
		case 0:
			return "";
		case 1:
			return breakpoint.getName();
		case 2:
			return breakpoint.getTargetXPath();
		case 3:
			return breakpoint.getTargetName();
		case 4:
			return BreakpointViewUtils.getStringRepresentation(breakpoint
					.getType());
		case 5:
			return breakpoint.getState().getName();
		default:
			throw new RuntimeException("Too much columns");
		}
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		Breakpoint breakpoint = (Breakpoint) element;

		switch (columnIndex) {
		case 0:
			if (breakpoint.isEnabled()) {
				return BPELUIPlugin.INSTANCE
						.getImage(IBPELUIConstants.ICON_CHECKED);
			} else {
				return BPELUIPlugin.INSTANCE
						.getImage(IBPELUIConstants.ICON_UNCHECKED);
			}
		case 1:
			return null;
		case 2:
			return null;
		case 3:
			return null;
		case 4:
			return null;
		case 5:
			switch (breakpoint.getState()) {
			case UNREGISTERED:
				return BPELUIPlugin.INSTANCE
						.getImage(IBPELUIConstants.ICON_UNREGISTERED);
			case REGISTERED:
				return BPELUIPlugin.INSTANCE
						.getImage(IBPELUIConstants.ICON_REGISTERED);
			case BLOCKING:
				return BPELUIPlugin.INSTANCE
						.getImage(IBPELUIConstants.ICON_BLOCKING_16);
			default:
				return null;
			}
		default:
			throw new RuntimeException("Too much columns");
		}
	}
}
