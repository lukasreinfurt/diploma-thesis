package org.eclipse.bpel.ui.agora.debug.commands;

import org.eclipse.bpel.debug.debugmodel.Breakpoint;
import org.eclipse.bpel.debug.debugmodel.BreakpointTypeEnum;
import org.eclipse.bpel.ui.commands.AddToListCommand;
import org.eclipse.emf.common.util.EList;

public class AddBreakpointTypeCommand extends AddToListCommand {

	public AddBreakpointTypeCommand(Breakpoint target, BreakpointTypeEnum newElement) {
		super(target, newElement, "Add BreakpointType");
	}

	@Override
	protected EList<BreakpointTypeEnum> getList() {
		return ((Breakpoint) target).getType();
	}
}
