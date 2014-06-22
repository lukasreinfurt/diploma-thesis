/**
 * 
 */
package org.eclipse.bpel.ui.agora.communication;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.apache.ode.bpel.extensions.comm.messages.engineOut.Loop_Condition_True;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Loop_Iteration_Complete;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Variable_Modification;
import org.eclipse.bpel.ui.BPELEditor;
import org.eclipse.bpel.ui.BPELMultipageEditorPart;
import org.eclipse.bpel.ui.agora.instances.ModelProvider;
import org.eclipse.bpel.ui.agora.manager.MonitorManager;
import org.eclipse.bpel.ui.agora.manager.XPathMapper;
import org.eclipse.bpel.ui.agora.provider.MonitoringProvider;
import org.eclipse.bpel.ui.agora.views.EventMessage;
import org.eclipse.bpel.ui.agora.views.EventModelProvider;
import org.eclipse.swt.widgets.Display;
import org.simtech.workflow.ode.auditing.agora.BPELStates;
import org.simtech.workflow.ode.auditing.communication.messages.InstanceHistoryMessage;
import org.simtech.workflow.ode.auditing.communication.messages.InstanceInformation;
import org.simtech.workflow.ode.auditing.communication.messages.RegisteredProcessInstancesMessage;

/**
 * This class handles the response of requests which were sent.
 * 
 * @author hahnml
 * @author tolevar
 */
public class RequestEventHandler implements MessageListener {

	@Override
	public void onMessage(Message msg) {
		if (!(msg instanceof ObjectMessage)) {
			System.out.println("No ObjectMessage");
			return;
		}

		/**
		 * Objekt aus Message holen
		 */
		ObjectMessage oMsg = (ObjectMessage) msg;
		Serializable obj = null;
		try {
			obj = oMsg.getObject();
		} catch (JMSException ex) {
			Logger.getLogger(RequestEventHandler.class.getName()).log(
					Level.SEVERE, null, ex);
		}

		if (obj == null) {
			System.out.println("obj == null");
			return;
		}

		if (obj instanceof InstanceHistoryMessage) {
			InstanceHistoryMessage historyMessage = (InstanceHistoryMessage) obj;

			Long instanceID = historyMessage.getProcessInstanceID();

			List<Object[]> objects = historyMessage.getObjects();

			MonitorManager manager = MonitoringProvider.getInstance()
					.getMonitorManager(instanceID);

			// Set the information for the auditing view
			EventModelProvider modelProvider = manager.getEventModelProvider();

			// @hahnml: Use an empty list to hold the events
			List<EventMessage> events = new ArrayList<EventMessage>();
			
			// @vonstepk: Collect states of all activities and variables in temporary maps,
			// such that they can be overridden cheaply.
			SortedMap<String, BPELStates> activityStates = 
				new TreeMap<String, BPELStates>();
			Map<String, Variable_Modification> variableValues = 
				new HashMap<String, Variable_Modification>();

			int i = 0;
			for (Object[] object : objects) {
				String eventType = (String) object[i];
				String source = (String) object[i + 1];
				final BPELStates state = (String) object[i + 3] != null ? BPELStates
						.valueOf((String) object[i + 3]) : null;
				final Object messageObject = object[i + 4];

				EventMessage eventMessage = new EventMessage();
				eventMessage.setEventType(eventType);
				eventMessage.setSource(source);
				eventMessage.setTimestamp((Long) object[i + 2]);
				eventMessage.setState(state);
				eventMessage.setMessageObject(messageObject);
				events.add(eventMessage);
				
				// @vonstepk: Update activity state if applicable
				if (state != null) {
					activityStates.put(source, state);
				}

				// @hahnml: Handle loop events to restore counters
				if (eventType.equals("Loop_Iteration_Complete")) {
					manager.getMonitoringHelper().increaseLoopCounter(source);
				}

				// @hahnml: Handle the repeated execution of the child
				// activities of a
				// loop activity (reset childs to initial state).
				if (eventType.equals("Loop_Condition_True")) {
					manager.getMonitoringHelper().resetChildLoopCounter(source,
							manager.getProcess(), manager);
					
					// @vonstepk: Reset activity state of child activities
					for (SortedMap.Entry<String, BPELStates> entry : 
						activityStates.subMap(source, activityStates.lastKey()).entrySet()) {
						
						if (entry.getKey().equals(source)) continue;
						if (!entry.getKey().startsWith(source)) break;
						
						entry.setValue(BPELStates.Ready);
					}
				}
				
				// @vonstepk: Store new variable values at modification events
				if (eventType.equals("Variable_Modification_At_Assign") 
						|| eventType.equals("Variable_Modification")) {
					Variable_Modification vm = (Variable_Modification)messageObject;
					variableValues.put(vm.getVariableXPath(), vm);
				}
			}
		
			// @vonstepk: Apply collected data to instance
			if (manager.getInstanceInformation().getInstanceID()
					.equals(instanceID)) {
				
				// States
				for (SortedMap.Entry<String, BPELStates> entry :
					activityStates.entrySet()) {
					XPathMapper.setState(entry.getKey(), entry.getValue(), manager.getProcess());
				}
				
				// Variable Values
				for (Map.Entry<String, Variable_Modification> entry : variableValues.entrySet()) {
					Variable_Modification vm = entry.getValue();
					XPathMapper.setVariable(entry.getKey(), vm.getValue(), 
							vm.getScopeID(), manager.getProcess());
				}
				
				// Might need to restore partner link values too
			}				
			
			// @hahnml: Update the editor at the end to show the loop counters
			final BPELMultipageEditorPart editor = manager.getEditor();
			Thread t = new Thread(new Runnable() {

				@Override
				public void run() {
					Display.getDefault().syncExec(new Runnable() {
						@Override
						public void run() {
							editor.refreshEditor();
						}
					});
				}
			});
			t.start();
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			// @hahnml: Add the whole list to the EventModelProvider
			// (auto-update)
			modelProvider.addEventMessageList(events);

		} else if (obj instanceof RegisteredProcessInstancesMessage) {
			RegisteredProcessInstancesMessage message = (RegisteredProcessInstancesMessage) obj;

			List<InstanceInformation> instances = message.getProcessInstances();

			// first we have to clear the list of instances
			ModelProvider.getInstance().getInstances().clear();

			for (InstanceInformation instance : instances) {
				// Add all instances to the ModelProvider to make them available
				// in the InstanceSelectionDialog.
				// By using a HashMap all existing instance information objects
				// will be overwritten.
				ModelProvider.getInstance().getInstances()
						.put(instance.getInstanceID(), instance);
			}
		}

	}

}
