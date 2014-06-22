package org.eclipse.bpel.ui.agora.debug.commands;

import org.eclipse.bpel.debug.debugmodel.Breakpoint;
import org.eclipse.bpel.debug.debugmodel.Breakpoints;
import org.eclipse.bpel.ui.commands.RemoveFromListCommand;
import org.eclipse.emf.common.util.EList;

public class RemoveBreakpointCommand extends RemoveFromListCommand {

	public RemoveBreakpointCommand(Breakpoints target, Breakpoint newElement) {
		super(target, newElement, "Remove Breakpoint");
	}

	@Override
	protected EList<Breakpoint> getList() {
		return ((Breakpoints) target).getBreakpoint();
	}
}
