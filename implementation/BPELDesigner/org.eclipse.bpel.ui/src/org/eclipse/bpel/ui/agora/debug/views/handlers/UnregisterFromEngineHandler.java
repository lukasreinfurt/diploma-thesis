package org.eclipse.bpel.ui.agora.debug.views.handlers;

import org.eclipse.bpel.ui.BPELEditDomain;
import org.eclipse.bpel.ui.BPELMultipageEditorPart;
import org.eclipse.bpel.ui.agora.debug.DebugManager;
import org.eclipse.bpel.ui.agora.instances.InstanceHelper;
import org.eclipse.bpel.ui.agora.provider.MonitoringProvider;
import org.eclipse.bpel.ui.uiextensionmodel.InstanceState;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.simtech.workflow.ode.auditing.agora.BPELStates;

public class UnregisterFromEngineHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		BPELMultipageEditorPart editor = MonitoringProvider.getInstance().getActiveEditor();
		
		if (editor != null) {
			DebugManager debug = MonitoringProvider.getInstance().getActiveProcessManager().getDebugManager();

			debug.removeAllBlockingEvents();
			debug.releaseAllOutstandingBlockingEvents();

			// Show in the instance state figure that the process instance
			// is executed again
			InstanceState instState = ((BPELEditDomain) editor.getEditDomain())
					.getInstanceNode();

			instState.setState(InstanceHelper
					.mapToUIExtensionState(BPELStates.Executing.name()));
		}

		return null;
	}

}
