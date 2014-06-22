package org.eclipse.bpel.ui.agora.debug.views.edit;

import org.eclipse.bpel.debug.debugmodel.Breakpoint;
import org.eclipse.bpel.debug.debugmodel.DebugPackage;
import org.eclipse.bpel.ui.agora.debug.views.BreakpointManagementView;
import org.eclipse.bpel.ui.commands.SetCommand;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;

public class NameEditingSupport extends EditingSupport {

	private final TableViewer viewer;
	private BreakpointManagementView parent;

	public NameEditingSupport(TableViewer viewer, BreakpointManagementView breakpointManagementView) {
		super(viewer);
		this.viewer = viewer;
		this.parent = breakpointManagementView;
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		return new TextCellEditor(viewer.getTable());
	}

	@Override
	protected boolean canEdit(Object element) {
		return true;
	}

	@Override
	protected Object getValue(Object element) {
		return ((Breakpoint) element).getName();
	}

	@Override
	protected void setValue(Object element, Object value) {
		Breakpoint breakpoint = (Breakpoint) element;
		this.parent.getProcessManager().getDebugManager().getCommandFramework().execute(
				new SetCommand(breakpoint, String.valueOf(value),
						DebugPackage.eINSTANCE.getBreakpoint_Name()));
		viewer.refresh();
	}

}
