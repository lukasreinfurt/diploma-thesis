package org.eclipse.bpel.ui.agora.views;

import org.apache.ode.bpel.extensions.comm.messages.engineOut.ActivityEventMessage;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.CorrelationSet_Modification;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.InstanceEventMessage;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Instance_Faulted;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.PartnerLink_Modification;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Variable_Modification;
import org.eclipse.swt.widgets.Display;

/**
 * This class is used to filter the events on which the RandomEventDialog could
 * be opened to display some detailed information.
 * 
 * @author hahnml
 * 
 */
public class EventDetailViewer {

	public static void showDialog(Object messageObject) {
		if (messageObject instanceof ActivityEventMessage
				|| messageObject instanceof Instance_Faulted
				|| messageObject instanceof Variable_Modification
			
				//@author: sonntamo
				|| messageObject instanceof PartnerLink_Modification
				|| messageObject instanceof CorrelationSet_Modification) {
			
			RandomEventDialog dialog = new RandomEventDialog(Display
					.getDefault().getActiveShell(),
					(InstanceEventMessage) messageObject);
			dialog.open();
		}
	}

}
