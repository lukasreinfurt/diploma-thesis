package org.eclipse.bpel.ui.agora.debug.views.handlers;

import org.eclipse.bpel.ui.agora.provider.MonitoringProvider;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class UpdateBreakpointsOnEngineHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		MonitoringProvider.getInstance().getActiveProcessManager().getDebugManager().updateBreakpointsOnEngine();

		return null;
	}

}
