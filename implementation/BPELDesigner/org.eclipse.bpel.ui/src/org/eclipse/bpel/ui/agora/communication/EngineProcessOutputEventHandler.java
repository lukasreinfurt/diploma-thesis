package org.eclipse.bpel.ui.agora.communication;

import org.apache.ode.bpel.extensions.comm.messages.engineOut.Process_Deployed;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Process_Undeployed;
import org.eclipse.bpel.ui.agora.manager.MonitorManager;
import org.eclipse.bpel.ui.agora.manager.ProcessManager;

/**
 * Handles all process event messages of the Engine Output
 * 
 * @author aeichel, hahnml, tolevar
 * 
 */
public class EngineProcessOutputEventHandler {

	private ProcessManager processManager = null;

	public EngineProcessOutputEventHandler(ProcessManager manager) {
		this.processManager = manager;
	}

	public void onMessage(Object obj) {
		MonitorManager manager = this.processManager.getLastStartedInstance();

		if (manager != null) {
			if (obj instanceof Process_Deployed) {
				manager.getInstanceManager().handleProcessDeployed(
						(Process_Deployed) obj, manager);
				
				this.processManager.setDeployed(obj);

			} else if (obj instanceof Process_Undeployed) {
				manager.getInstanceManager().handleProcessUndeployed(
						(Process_Undeployed) obj, manager);
				
				this.processManager.setDeployed(obj);
			}
		}
	}

}
