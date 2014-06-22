package org.eclipse.bpel.ui.agora.debug.views;

import java.util.regex.PatternSyntaxException;

import org.eclipse.bpel.debug.debugmodel.Breakpoint;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

public class BreakpointTableFilter extends ViewerFilter {

	private String searchString;

	public void setSearchText(String s) {
		// Search must be a substring of the existing value
		this.searchString = ".*" + s + ".*";
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (searchString == null || searchString.length() == 0) {
			return true;
		}
		Breakpoint breakpoint = (Breakpoint) element;
		try {
			if (breakpoint.getName().toLowerCase().matches(searchString.toLowerCase())) {
				return true;
			}
			if (breakpoint.getTargetXPath().toLowerCase().matches(searchString.toLowerCase())) {
				return true;
			}
			if (breakpoint.getTargetName().toLowerCase().matches(searchString.toLowerCase())) {
				return true;
			}
			if (BreakpointViewUtils.getStringRepresentation(breakpoint.getType()).toLowerCase().matches(searchString.toLowerCase())) {
				return true;
			}
			if (breakpoint.getState().getName().toLowerCase().matches(searchString.toLowerCase())) {
				return true;
			}
		}catch (PatternSyntaxException e) {
			e.printStackTrace();
		}
		return false;
	}
}
