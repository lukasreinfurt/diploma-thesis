package org.eclipse.bpel.ui.agora.manager;

import org.eclipse.bpel.ui.BPELEditDomain;
import org.eclipse.bpel.ui.agora.instances.InstanceHelper;
import org.eclipse.bpel.ui.uiextensionmodel.InstanceState;
import org.eclipse.swt.widgets.Display;
import org.simtech.workflow.ode.auditing.agora.BPELStates;

/**
 * The instance thread sets the instance state within the editor
 * 
 * @author tolevar
 * 
 */
public class InstanceThread implements Runnable {

	private MonitorManager manager = null;
	private BPELStates instanceState = BPELStates.Initial;

	public InstanceThread(BPELStates instanceState, MonitorManager manager) {
		this.instanceState = instanceState;
		this.manager = manager;
	}

	@Override
	public void run() {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {

				// Ignore all instance states during breakpoint
				// updates
				if (!manager.getDebugManager().isUpdatingBreakpoints()) {
					InstanceState instState = ((BPELEditDomain) manager
							.getEditor().getEditDomain()).getInstanceNode();

					instState.setState(InstanceHelper
							.mapToUIExtensionState(instanceState.name()));
				}

				// If the breakpoint updates are finished we got a
				// Instance_Running message and can change back to
				// normal mode
				if (manager.getDebugManager().isUpdatingBreakpoints()
						&& manager.getInstanceInformation().getState() == BPELStates.Suspended
						&& (instanceState == BPELStates.Executing || instanceState == BPELStates.Blocking)) {
					manager.getDebugManager().setIsUpdatingBreakpoints(false);
				}
			}
		});

	}

}
