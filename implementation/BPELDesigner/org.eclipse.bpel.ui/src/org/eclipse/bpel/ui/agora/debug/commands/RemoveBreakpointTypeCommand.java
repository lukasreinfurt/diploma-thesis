package org.eclipse.bpel.ui.agora.debug.commands;

import org.eclipse.bpel.debug.debugmodel.Breakpoint;
import org.eclipse.bpel.debug.debugmodel.BreakpointTypeEnum;
import org.eclipse.bpel.ui.commands.RemoveFromListCommand;
import org.eclipse.emf.common.util.EList;

public class RemoveBreakpointTypeCommand extends RemoveFromListCommand {

	public RemoveBreakpointTypeCommand(Breakpoint target, BreakpointTypeEnum newElement) {
		super(target, newElement, "Remove BreakpointType");
	}

	@Override
	protected EList<BreakpointTypeEnum> getList() {
		return ((Breakpoint) target).getType();
	}
}
