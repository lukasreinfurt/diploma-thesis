package org.eclipse.bpel.ui.agora.manager;

import org.apache.ode.bpel.extensions.comm.messages.engineOut.CorrelationSet_Modification;
import org.eclipse.bpel.model.CorrelationSet;
import org.eclipse.bpel.ui.agora.views.EventMessage;

public class CorrelationSetManager {

	public static void handleCorrelationSetModification(
			CorrelationSet_Modification message, MonitorManager manager) {
		XPathMapper
				.setCorrelationSet(message.getCSet_xpath(),
						message.getValues(), message.getScopeID(),
						manager.getProcess());

		// Try to get the name of the correlation set
		String csetName = "";
		try {
			csetName = ((CorrelationSet) XPathMapper.handleXPath(
					message.getCSet_xpath(), manager.getProcess())).getName();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Add the event to the view
		manager.getEventModelProvider().addEventMessage(
				new EventMessage(
						message.getClass()
								.getName()
								.substring(
										message.getClass().getName()
												.lastIndexOf(".") + 1), message
								.getCSet_xpath(), csetName, message
								.getTimeStamp(), message));
	}
}
