package org.eclipse.bpel.ui.agora.manager;

import org.apache.ode.bpel.extensions.comm.messages.engineOut.LinkEventMessage;
import org.eclipse.bpel.ui.agora.views.EventMessage;
import org.simtech.workflow.ode.auditing.agora.BPELStates;

/**
 * @author hahnml
 * 
 */
public class LinkManager {

	public static void handleLinkEvent(final LinkEventMessage message,
			MonitorManager manager) {

		// set new state at EMF-model and refresh it
		BPELStates state = StateMachine.computeNewLinkState(message);
		XPathMapper.setLinkState(message.getLinkXPath(), state,
				manager.getProcess());

		// @hahnml: Add the event to the view
		manager.getEventModelProvider().addEventMessage(
				new EventMessage(
						message.getClass()
								.getName()
								.substring(
										message.getClass().getName()
												.lastIndexOf(".") + 1), message
								.getLinkXPath(), message.getLinkName(), message.getTimeStamp(),
						state, message));

	}
}
