package org.eclipse.bpel.ui.agora.debug.views.edit;

import org.eclipse.bpel.debug.debugmodel.Breakpoint;
import org.eclipse.bpel.debug.debugmodel.DebugPackage;
import org.eclipse.bpel.ui.agora.debug.views.BreakpointManagementView;
import org.eclipse.bpel.ui.commands.SetCommand;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;

public class EnabledEditingSupport extends EditingSupport {

	private final TableViewer viewer;
	private BreakpointManagementView parent;

	public EnabledEditingSupport(TableViewer viewer, BreakpointManagementView breakpointManagementView) {
		super(viewer);
		this.viewer = viewer;
		this.parent = breakpointManagementView;
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		return new CheckboxCellEditor(null, SWT.CHECK | SWT.READ_ONLY);

	}

	@Override
	protected boolean canEdit(Object element) {
		return true;
	}

	@Override
	protected Object getValue(Object element) {
		Breakpoint breakpoint = (Breakpoint) element;
		return breakpoint.isEnabled();

	}

	@Override
	protected void setValue(Object element, Object value) {
		Breakpoint breakpoint = (Breakpoint) element;
		this.parent.getProcessManager().getDebugManager().getCommandFramework().execute(
				new SetCommand(breakpoint, (Boolean) value,
						DebugPackage.eINSTANCE.getBreakpoint_Enabled()));
		viewer.refresh();
	}

}
