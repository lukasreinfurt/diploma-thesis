package org.eclipse.bpel.ui.agora.communication;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.xml.namespace.QName;

import org.apache.ode.bpel.extensions.comm.messages.engineOut.InstanceEventMessage;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.ProcessEventMessage;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Process_Deployed;
import org.eclipse.bpel.ui.agora.manager.MonitorManager;
import org.eclipse.bpel.ui.agora.manager.ProcessManager;
import org.eclipse.bpel.ui.agora.provider.MonitoringProvider;

/**
 * Receives all event messages of the Engine Output and redirects them to the
 * correct EngineOutputEventHandler.
 * 
 * @author hahnml, aeichel
 * 
 */
public class EngineOutputMessageDispatcher implements MessageListener {

	public EngineOutputMessageDispatcher() {
		JMSCommunication.getInstance().setEngineOutputReceiver(this);
	}

	@Override
	public void onMessage(Message message) {
		if (!(message instanceof ObjectMessage)) {
			System.out.println("No ObjectMessage");
			return;
		}

		/**
		 * Get object of the message
		 */
		ObjectMessage oMsg = (ObjectMessage) message;
		Serializable obj = null;
		try {
			obj = oMsg.getObject();
		} catch (JMSException ex) {
			Logger.getLogger(EngineInstanceOutputEventHandler.class.getName())
					.log(Level.SEVERE, null, ex);
		}

		if (obj == null) {
			System.out.println("obj == null");
			return;
		}

		Long instanceID = null;

		if (obj instanceof InstanceEventMessage) {
			instanceID = ((InstanceEventMessage) obj).getProcessID();

			// Get the correct MonitorManager and send the message to its
			// EngineOutputEventHandler
			MonitorManager manager = MonitoringProvider.getInstance()
					.getMonitorManager(instanceID);
			if (manager != null) {
				manager.getEventHandler().onMessage(obj);
			} else {
				manager = MonitoringProvider
						.getInstance()
						.getProcessManager(
								((InstanceEventMessage) obj).getProcessName(),
								((InstanceEventMessage) obj).getVersion())
						.getLastStartedInstance();
				
				Long man_ID = manager.getInstanceInformation().getInstanceID();
				if (manager != null && (man_ID == null || man_ID.equals(instanceID))) {
					manager.getEventHandler().onMessage(obj);
				}
			}

		} else if (obj instanceof ProcessEventMessage) {
			// TODO: CHECK IF THIS WORKS FOR EVERY SCENARIO
			if (obj instanceof Process_Deployed) {
				// Get the active ProcessManager and send the message to its
				// EngineProcessOutputEventHandler
				ProcessManager manager = MonitoringProvider.getInstance()
						.getActiveProcessManager();
				if (manager != null) {
					manager.getEventHandler().onMessage(obj);
				}
			} else {
				ProcessEventMessage msg = (ProcessEventMessage) obj;
				QName processName = msg.getProcessName();
				Long processVersion = msg.getVersion();

				// Get the ProcessManager with the correct processName and
				// version and send the message to its
				// EngineProcessOutputEventHandler
				ProcessManager manager = MonitoringProvider.getInstance()
						.getProcessManager(processName, processVersion);
				if (manager != null) {
					manager.getEventHandler().onMessage(obj);
				}
			}

		}
	}

}
