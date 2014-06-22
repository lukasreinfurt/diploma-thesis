/**
 * 
 */
package org.eclipse.bpel.ui.agora.manager;

import org.apache.ode.bpel.extensions.comm.messages.engineOut.InstanceEventMessage;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Instance_Completed;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Instance_Faulted;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Instance_Iteration_Prepared;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Instance_JumpTo_Prepared;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Instance_Reexecution_Prepared;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Instance_Running;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Instance_Suspended;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Instance_Terminated;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Process_Deployed;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Process_Instantiated;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Process_Undeployed;
import org.eclipse.bpel.ui.agora.provider.MonitoringProvider;
import org.eclipse.bpel.ui.agora.views.EventMessage;
import org.simtech.workflow.ode.auditing.agora.BPELStates;
import org.simtech.workflow.ode.auditing.communication.messages.InstanceInformation;

/**
 * @author aeichel
 * @author tolevar
 * 
 */
public class InstanceManager {

	private BPELStates instanceState = BPELStates.Initial;

	public void handleProcessInstantiated(Process_Instantiated message,
			MonitorManager manager) {
		manager.getInstanceInformation().setInstanceID(message.getProcessID());

		// Clear the view
		manager.getEventModelProvider().clear();

		// Add the new instance event to the view
		manager.getEventModelProvider().addEventMessage(

				new EventMessage(
						message.getClass()
								.getName()
								.substring(
										message.getClass().getName()
												.lastIndexOf(".") + 1), message
								.getProcessName().getLocalPart(), manager
								.getProcess().getName(),
						message.getTimeStamp(), message));

	}

	public void handleProcessDeployed(Process_Deployed message,
			MonitorManager manager) {
		manager.getInstanceInformation()
				.setProcessVersion(message.getVersion());
		manager.getInstanceInformation().setProcessName(
				message.getProcessName());
		manager.getInstanceInformation().setTimestamp(message.getTimeStamp());
	}

	/**
	 * Handles the process undeployed message
	 * 
	 * @param message
	 * @author tolevar
	 */
	public void handleProcessUndeployed(Process_Undeployed message,
			MonitorManager manager) {

		if (message.getProcessName().equals(
				manager.getInstanceInformation().getProcessName())) {
			instanceState = BPELStates.Initial;

			manager.getEventModelProvider().clear();

			updateView(manager);

			MonitoringProvider.getInstance()
					.getProcessManager(manager.getEditor())
					.deleteMonitorManager(manager);
		}
	}

	public void handleInstanceStatusChanged(Object message,
			MonitorManager manager) {

		if (message instanceof Instance_Completed) {
			manager.setCompleted();
			instanceState = BPELStates.Completed;
		} else if (message instanceof Instance_Running) {
			// @hahnml: Check if any activities are blocked
			if (manager.getDebugManager().isBlocking()) {
				instanceState = BPELStates.Blocking;
			} else {
				instanceState = BPELStates.Executing;
			}

			// Update the process name and version at the *.bpelex file
			Instance_Running msg = (Instance_Running) message;

			// Save the changes
			MonitoringProvider.saveBPELExFile(msg.getProcessName(),
					msg.getVersion(), manager);

		} else if (message instanceof Instance_Faulted) {
			instanceState = BPELStates.Faulted;
		} else if (message instanceof Instance_Suspended) {
			manager.setSuspended();
			instanceState = BPELStates.Suspended;
		} else if (message instanceof Instance_Terminated) {
			instanceState = BPELStates.Terminated;
			manager.setTerminated();
		}

		if (!(message instanceof Instance_Iteration_Prepared)
				&& !(message instanceof Instance_Reexecution_Prepared)
				&& !(message instanceof Instance_JumpTo_Prepared)) {

			updateView(manager);

			manager.getInstanceInformation().setState(instanceState);
		}

		// Add the event to the view
		manager.getEventModelProvider().addEventMessage(
				new EventMessage(
						message.getClass()
								.getName()
								.substring(
										message.getClass().getName()
												.lastIndexOf(".") + 1),
						((InstanceEventMessage) message).getProcessName()
								.getLocalPart(), manager.getProcess().getName(),
						((InstanceEventMessage) message).getTimeStamp(),
						message));
		
		// @vonstepk Reset all scopeIDs. We don't want to carry those over to 
		// the next run.
		if ( message instanceof Instance_Terminated 
				|| message instanceof Instance_Faulted
				|| message instanceof Instance_Completed ) {
			manager.getMonitoringHelper().resetScopeIDs(manager.getProcess());
		}

	}

	/**
	 * Updates the state of the running instance in the editor
	 * 
	 * @param manager
	 * @author tolevar
	 */
	private void updateView(MonitorManager manager) {
		Thread t = new Thread(new InstanceThread(instanceState, manager));
		t.start();
		try {
			t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void clearInstance(InstanceInformation instanceInfo) {
		instanceInfo.setInstanceID(null);
		instanceInfo.setPackageName(null);
		instanceInfo.setProcessVersion(null);
		instanceInfo.setState(BPELStates.Initial);
		instanceInfo.setTimestamp(null);
	}

	// public void deleteInstance(InstanceInformation instanceInfo) {
	// MonitoringProvider.getInstance().getCorrelationMap()
	// .remove(instanceInfo);
	// }
}
