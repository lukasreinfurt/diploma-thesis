package org.eclipse.bpel.ui.agora.debug.views.edit;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.bpel.debug.debugmodel.Breakpoint;
import org.eclipse.bpel.debug.debugmodel.BreakpointTypeEnum;
import org.eclipse.bpel.ui.agora.debug.commands.AddBreakpointTypeCommand;
import org.eclipse.bpel.ui.agora.debug.commands.RemoveBreakpointTypeCommand;
import org.eclipse.bpel.ui.agora.debug.views.BreakpointManagementView;
import org.eclipse.bpel.ui.agora.debug.views.BreakpointViewUtils;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;

public class BreakpointTypeEditingSupport extends EditingSupport {

	private final TableViewer viewer;
	
	private BreakpointManagementView parent;

	public BreakpointTypeEditingSupport(TableViewer viewer, BreakpointManagementView breakpointManagementView) {
		super(viewer);
		this.viewer = viewer;
		this.parent = breakpointManagementView;
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		return new BreakpointTypeCellEditor(viewer.getTable(), element, parent);
	}

	@Override
	protected boolean canEdit(Object element) {
		return true;
	}

	@Override
	protected Object getValue(Object element) {
		return BreakpointViewUtils
				.getStringRepresentation(((Breakpoint) element).getType());
	}

	@Override
	protected void setValue(Object element, Object value) {
		Breakpoint breakpoint = (Breakpoint) element;

		saveBreakpointTypeChanges(breakpoint, value);

		viewer.refresh();
	}

	private void saveBreakpointTypeChanges(Breakpoint breakpoint, Object value) {
		List<BreakpointTypeEnum> oldTypes = new ArrayList<BreakpointTypeEnum>();
		List<BreakpointTypeEnum> newTypes = new ArrayList<BreakpointTypeEnum>();
		
		List<BreakpointTypeEnum> typesToAdd = new ArrayList<BreakpointTypeEnum>();
		List<BreakpointTypeEnum> typesToRemove = new ArrayList<BreakpointTypeEnum>();
		
		String[] newTypeList = BreakpointViewUtils.getStringRepresentation(breakpoint.getType()).split(", ");
		String[] oldTypeList = String.valueOf(value).split(", ");
		
		for (String s : oldTypeList) {
			if (s.equals(BreakpointTypeEnum.ACTIVITY_READY.getName())) {
				oldTypes.add(BreakpointTypeEnum.ACTIVITY_READY);
				continue;
			}
			if (s.equals(BreakpointTypeEnum.ACTIVITY_EXECUTED.getName())) {
				oldTypes.add(BreakpointTypeEnum.ACTIVITY_EXECUTED);
				continue;
			}
			if (s.equals(BreakpointTypeEnum.ACTIVITY_FAULTED.getName())) {
				oldTypes.add(BreakpointTypeEnum.ACTIVITY_FAULTED);
				continue;
			}
			if (s.equals(BreakpointTypeEnum.EVALUATING_TRANSITION_CONDITION_FAULTED.getName())) {
				oldTypes.add(BreakpointTypeEnum.EVALUATING_TRANSITION_CONDITION_FAULTED);
				continue;
			}
			if (s.equals(BreakpointTypeEnum.SCOPE_COMPENSATING.getName())) {
				oldTypes.add(BreakpointTypeEnum.SCOPE_COMPENSATING);
				continue;
			}
			if (s.equals(BreakpointTypeEnum.SCOPE_HANDLING_TERMINATION.getName())) {
				oldTypes.add(BreakpointTypeEnum.SCOPE_HANDLING_TERMINATION);
				continue;
			}
			if (s.equals(BreakpointTypeEnum.SCOPE_HANDLING_FAULT.getName())) {
				oldTypes.add(BreakpointTypeEnum.SCOPE_HANDLING_FAULT);
				continue;
			}
			if (s.equals(BreakpointTypeEnum.SCOPE_COMPLETE_WITH_FAULT.getName())) {
				oldTypes.add(BreakpointTypeEnum.SCOPE_COMPLETE_WITH_FAULT);
				continue;
			}
			if (s.equals(BreakpointTypeEnum.LOOP_ITERATION_COMPLETE.getName())) {
				oldTypes.add(BreakpointTypeEnum.LOOP_ITERATION_COMPLETE);
				continue;
			}
			if (s.equals(BreakpointTypeEnum.LOOP_CONDITION_TRUE.getName())) {
				oldTypes.add(BreakpointTypeEnum.LOOP_CONDITION_TRUE);
				continue;
			}
			if (s.equals(BreakpointTypeEnum.LOOP_CONDITION_FALSE.getName())) {
				oldTypes.add(BreakpointTypeEnum.LOOP_CONDITION_FALSE);
				continue;
			}
			if (s.equals(BreakpointTypeEnum.LINK_EVALUATED.getName())) {
				oldTypes.add(BreakpointTypeEnum.LINK_EVALUATED);
			}
		}
		
		for (String s : newTypeList) {
			if (s.equals(BreakpointTypeEnum.ACTIVITY_READY.getName())) {
				newTypes.add(BreakpointTypeEnum.ACTIVITY_READY);
				continue;
			}
			if (s.equals(BreakpointTypeEnum.ACTIVITY_EXECUTED.getName())) {
				newTypes.add(BreakpointTypeEnum.ACTIVITY_EXECUTED);
				continue;
			}
			if (s.equals(BreakpointTypeEnum.ACTIVITY_FAULTED.getName())) {
				newTypes.add(BreakpointTypeEnum.ACTIVITY_FAULTED);
				continue;
			}
			if (s.equals(BreakpointTypeEnum.EVALUATING_TRANSITION_CONDITION_FAULTED.getName())) {
				newTypes.add(BreakpointTypeEnum.EVALUATING_TRANSITION_CONDITION_FAULTED);
				continue;
			}
			if (s.equals(BreakpointTypeEnum.SCOPE_COMPENSATING.getName())) {
				newTypes.add(BreakpointTypeEnum.SCOPE_COMPENSATING);
				continue;
			}
			if (s.equals(BreakpointTypeEnum.SCOPE_HANDLING_TERMINATION.getName())) {
				newTypes.add(BreakpointTypeEnum.SCOPE_HANDLING_TERMINATION);
				continue;
			}
			if (s.equals(BreakpointTypeEnum.SCOPE_HANDLING_FAULT.getName())) {
				newTypes.add(BreakpointTypeEnum.SCOPE_HANDLING_FAULT);
				continue;
			}
			if (s.equals(BreakpointTypeEnum.SCOPE_COMPLETE_WITH_FAULT.getName())) {
				newTypes.add(BreakpointTypeEnum.SCOPE_COMPLETE_WITH_FAULT);
				continue;
			}
			if (s.equals(BreakpointTypeEnum.LOOP_ITERATION_COMPLETE.getName())) {
				newTypes.add(BreakpointTypeEnum.LOOP_ITERATION_COMPLETE);
				continue;
			}
			if (s.equals(BreakpointTypeEnum.LOOP_CONDITION_TRUE.getName())) {
				newTypes.add(BreakpointTypeEnum.LOOP_CONDITION_TRUE);
				continue;
			}
			if (s.equals(BreakpointTypeEnum.LOOP_CONDITION_FALSE.getName())) {
				newTypes.add(BreakpointTypeEnum.LOOP_CONDITION_FALSE);
				continue;
			}
			if (s.equals(BreakpointTypeEnum.LINK_EVALUATED.getName())) {
				newTypes.add(BreakpointTypeEnum.LINK_EVALUATED);
				continue;
			}
		}
		
		//Get all removed types (oldTypes - newTypes)
		typesToRemove.addAll(oldTypes);
		typesToRemove.removeAll(newTypes);

		//Get all added types (newTypes - oldTypes)
		typesToAdd.addAll(newTypes);
		typesToAdd.removeAll(oldTypes);
		
		//Remove all removed types from the model
		for (BreakpointTypeEnum type : typesToRemove) {
			this.parent.getProcessManager().getDebugManager().getCommandFramework().execute(
					new RemoveBreakpointTypeCommand(breakpoint, type));
		}
		
		//Add all added types to the model
		for (BreakpointTypeEnum type : typesToAdd) {
			this.parent.getProcessManager().getDebugManager().getCommandFramework().execute(
					new AddBreakpointTypeCommand(breakpoint, type));
		}
	}
}
