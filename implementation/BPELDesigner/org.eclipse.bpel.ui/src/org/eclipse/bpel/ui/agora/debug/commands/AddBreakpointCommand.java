package org.eclipse.bpel.ui.agora.debug.commands;

import org.eclipse.bpel.debug.debugmodel.Breakpoint;
import org.eclipse.bpel.debug.debugmodel.Breakpoints;
import org.eclipse.bpel.ui.commands.AddToListCommand;
import org.eclipse.emf.common.util.EList;

public class AddBreakpointCommand extends AddToListCommand {

	public AddBreakpointCommand(Breakpoints target, Breakpoint newElement) {
		super(target, newElement, "Add Breakpoint");
	}

	@Override
	protected EList<Breakpoint> getList() {
		return ((Breakpoints) target).getBreakpoint();
	}
}
