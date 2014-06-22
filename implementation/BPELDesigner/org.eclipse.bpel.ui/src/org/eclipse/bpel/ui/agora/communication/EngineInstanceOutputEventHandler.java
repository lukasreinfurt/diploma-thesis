package org.eclipse.bpel.ui.agora.communication;

import org.apache.ode.bpel.extensions.comm.messages.engineOut.ActivityEventMessage;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.CorrelationSet_Modification;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.InstanceEventMessage;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Instance_Completed;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Instance_Faulted;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Instance_Iteration_Prepared;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Instance_JumpTo_Prepared;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Instance_Reexecution_Prepared;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Instance_Running;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Instance_Suspended;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Instance_Terminated;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.LinkEventMessage;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.PartnerLink_Modification;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Process_Instantiated;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Variable_Modification;
import org.eclipse.bpel.ui.agora.manager.ActivityManager;
import org.eclipse.bpel.ui.agora.manager.CorrelationSetManager;
import org.eclipse.bpel.ui.agora.manager.LinkManager;
import org.eclipse.bpel.ui.agora.manager.MonitorManager;
import org.eclipse.bpel.ui.agora.manager.PartnerLinkManager;
import org.eclipse.bpel.ui.agora.manager.VariableManager;
import org.simtech.workflow.ode.auditing.communication.messages.InstanceInformation;

/**
 * Handles all instance event messages of the Engine Output
 * 
 * @author aeichel, hahnml, tolevar
 * 
 */
public class EngineInstanceOutputEventHandler {

	private MonitorManager manager = null;

	public EngineInstanceOutputEventHandler(MonitorManager manager) {
		this.manager = manager;
	}

	public void onMessage(Object obj) {
		InstanceInformation instanceInfo = null;

		if (obj instanceof Instance_Completed
				|| obj instanceof Instance_Running
				|| obj instanceof Instance_Faulted
				|| obj instanceof Instance_Suspended
				|| obj instanceof Instance_Terminated
				|| obj instanceof Instance_Iteration_Prepared
				|| obj instanceof Instance_Reexecution_Prepared
				|| obj instanceof Instance_JumpTo_Prepared) {

			manager.getInstanceManager().handleInstanceStatusChanged(obj,
					manager);

			if (obj instanceof Instance_Completed
					|| obj instanceof Instance_Terminated
					|| obj instanceof Instance_Faulted) {
				// If the instance is completed we remove all registered
				// blocking events
				manager.getDebugManager().removeAllBlockingEvents();
			}

		} else if (obj instanceof ActivityEventMessage
				|| obj instanceof LinkEventMessage) {

			// If the event is blocking we register the message in our
			// DebugManager
			if (((InstanceEventMessage) obj).getBlocking()) {
				manager.getDebugManager().registerBlockingMessage(obj);
			}

			if (obj instanceof ActivityEventMessage) {
				/**
				 * regards BPEL activities
				 */
				ActivityEventMessage message = (ActivityEventMessage) obj;
				instanceInfo = manager.getInstanceInformation();
				if (instanceInfo.getInstanceID() != null) {
					if (instanceInfo.getInstanceID().compareTo(
							message.getProcessID()) == 0) {
						synchronized (this) {
							ActivityManager.handleActivityEvent(message,
									manager);
						}
					}
				}
			} else if (obj instanceof LinkEventMessage) {
				LinkEventMessage message = (LinkEventMessage) obj;
				instanceInfo = manager.getInstanceInformation();
				if (instanceInfo.getInstanceID() != null) {
					if (instanceInfo.getInstanceID().compareTo(
							((InstanceEventMessage) obj).getProcessID()) == 0) {
						synchronized (this) {
							LinkManager.handleLinkEvent(message, manager);
						}
					}
				}
			}

		} else if (obj instanceof Variable_Modification) {
			Variable_Modification message = (Variable_Modification) obj;
			instanceInfo = manager.getInstanceInformation();
			if (instanceInfo.getInstanceID().compareTo(
					((InstanceEventMessage) obj).getProcessID()) == 0) {
				if (obj instanceof Variable_Modification) {
					VariableManager
							.handleVariableModification(message, manager);
				}
			}

			// @author: sonntamo
		} else if (obj instanceof PartnerLink_Modification) {
			PartnerLink_Modification message = (PartnerLink_Modification) obj;
			instanceInfo = manager.getInstanceInformation();
			if (instanceInfo.getInstanceID().compareTo(
					((InstanceEventMessage) obj).getProcessID()) == 0) {
				PartnerLinkManager.handlePartnerLinkModification(message,
						manager);

			}

			// @author: sonntamo
		} else if (obj instanceof CorrelationSet_Modification) {
			CorrelationSet_Modification message = (CorrelationSet_Modification) obj;
			instanceInfo = manager.getInstanceInformation();
			if (instanceInfo.getInstanceID().compareTo(
					((InstanceEventMessage) obj).getProcessID()) == 0) {
				CorrelationSetManager.handleCorrelationSetModification(message,
						manager);
			}

		} else if (obj instanceof Process_Instantiated) {
			// @hahnml: Only handle this event, if the MonitorManager has not
			// yet a corresponding instance.
			if (manager.getInstanceInformation().getInstanceID() == null) {
				manager.getInstanceManager().handleProcessInstantiated(
						(Process_Instantiated) obj, manager);

				// Register the blocking events for debugging
				manager.getDebugManager().registerBlockingEventsOnEngine(
						((Process_Instantiated) obj).getProcessName(),
						((Process_Instantiated) obj).getProcessID());
			}
		}
	}

}
