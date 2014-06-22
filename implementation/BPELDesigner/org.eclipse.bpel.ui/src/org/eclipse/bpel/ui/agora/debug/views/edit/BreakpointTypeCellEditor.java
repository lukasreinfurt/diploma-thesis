package org.eclipse.bpel.ui.agora.debug.views.edit;

import org.eclipse.bpel.debug.debugmodel.Breakpoint;
import org.eclipse.bpel.ui.agora.debug.views.BreakpointManagementView;
import org.eclipse.bpel.ui.agora.debug.views.BreakpointViewUtils;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class BreakpointTypeCellEditor extends DialogCellEditor {
	
	Breakpoint breakpoint;
	
	private BreakpointManagementView parent;

	public BreakpointTypeCellEditor(Composite parent, Object element, BreakpointManagementView breakpointManagementView) {
		super(parent);
		this.breakpoint = (Breakpoint) element;
		this.parent = breakpointManagementView;
	}

	@Override
	protected Object openDialogBox(Control cellEditorWindow) {
		BreakpointTypeDialog dialog = new BreakpointTypeDialog(cellEditorWindow.getShell(), this.breakpoint, this.parent);
		
		dialog.setBlockOnOpen(true);
		
		int exitCode = dialog.open();
		
		if (exitCode == SWT.OK) {
			return BreakpointViewUtils.getStringRepresentation(dialog.getTypeList());
		}
		
		return null;
	}

}
