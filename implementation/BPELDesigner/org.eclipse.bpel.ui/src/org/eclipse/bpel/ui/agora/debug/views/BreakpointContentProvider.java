package org.eclipse.bpel.ui.agora.debug.views;

import java.util.List;

import org.eclipse.bpel.debug.debugmodel.Breakpoint;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class BreakpointContentProvider implements IStructuredContentProvider {

	@Override
	public Object[] getElements(Object inputElement) {
		@SuppressWarnings("unchecked")
		List<Breakpoint> breakpoints = (List<Breakpoint>) inputElement;
		return breakpoints.toArray();
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

}
