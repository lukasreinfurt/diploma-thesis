package org.eclipse.bpel.ui.agora.manager;

import java.util.Iterator;

import org.apache.ode.bpel.extensions.comm.messages.engineOut.ActivityEventMessage;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Activity_Ready;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Loop_Condition_True;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Loop_Iteration_Complete;
import org.eclipse.bpel.model.Activity;
import org.eclipse.bpel.model.BPELExtensibleElement;
import org.eclipse.bpel.model.Process;
import org.eclipse.bpel.model.Scope;
import org.eclipse.bpel.model.Variable;
import org.eclipse.bpel.ui.agora.views.EventMessage;
import org.simtech.workflow.ode.auditing.agora.BPELStates;

/**
 * @author aeichel
 * @author tolevar
 * 
 */
public class ActivityManager {

	public static void handleActivityEvent(final ActivityEventMessage message,
			MonitorManager manager) {

		// set new state at EMF-model and refresh it
		BPELStates state = StateMachine.computeNewActivityState(message);
		XPathMapper.setState(message.getActivityXPath(), state,
				manager.getProcess());
		
		String elementName = message.getActivityName();
		if (elementName == null || elementName.isEmpty()) {
			// Try to get the name of the activity
			BPELExtensibleElement result = XPathMapper.handleXPath(
					message.getActivityXPath(), manager.getProcess());
			if (result instanceof Process) { 
				elementName = ((Process) result).getName();
			} else if (result instanceof Activity) {
				elementName = ((Activity) result).getName();
			}
		}
		
		// @hahnml: Add the event to the view
		manager.getEventModelProvider().addEventMessage(
				new EventMessage(
						message.getClass()
								.getName()
								.substring(
										message.getClass().getName()
												.lastIndexOf(".") + 1), message
								.getActivityXPath(), elementName, message.getTimeStamp(),
						state, message));

		// @hahnml: Handle the complection of a loop activity iteration
		// (increase the counter)
		if (message instanceof Loop_Iteration_Complete) {
			manager.getMonitoringHelper().increaseLoopCounter(
					message.getActivityXPath());
		}

		// @hahnml: Handle the repeated execution of the child activities of a
		// loop activity (reset childs to initial state).
		if (message instanceof Loop_Condition_True) {
			manager.getMonitoringHelper().resetStateOfChildActivities(
					message.getActivityXPath(), manager.getProcess(), manager);
		}
		
		// @vonstepk: Set ScopeIDs for all child variables whenever a new scope
		// is entered. 
		if  (message instanceof Activity_Ready) {
			Activity_Ready m = (Activity_Ready)message;
			
			if (m.getActivityXPath().matches(".*/process")
				|| (m.getActivityXPath().matches(".*/scope\\[.*\\]"))) {							
				
				BPELExtensibleElement result = XPathMapper.handleXPath(
						message.getActivityXPath(), manager.getProcess());
				
				Iterator<Variable> it = null;
				
				if (result instanceof Process) { 
					it = ((Process) result).getVariables().getChildren().iterator();
				} else if (result instanceof Scope) {
					it = ((Scope) result).getVariables().getChildren().iterator();
				}	
				
				while(it != null && it.hasNext())  {
					Variable var = it.next();
					XPathMapper.setVariable(var.getXPath(), var.getValue(),
						m.getActivityID(), manager.getProcess());
				}
			}
		}
		
		
		
	}

}
