package org.eclipse.bpel.ui.agora.snapshots.wizard.table;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;

public class SnapshotVariableEditingSupport extends EditingSupport {

	private final TableViewer viewer;

	public SnapshotVariableEditingSupport(TableViewer viewer) {
		super(viewer);
		this.viewer = viewer;
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		return new CheckboxCellEditor(null, SWT.CHECK);

	}

	@Override
	protected boolean canEdit(Object element) {
		return true;
	}

	@Override
	protected Object getValue(Object element) {
		VariableInfo info = (VariableInfo) element;
		return info.isSelected();

	}

	@Override
	protected void setValue(Object element, Object value) {
		VariableInfo info = (VariableInfo) element;
		info.setSelected((Boolean) value);
		viewer.refresh();
	}
}
