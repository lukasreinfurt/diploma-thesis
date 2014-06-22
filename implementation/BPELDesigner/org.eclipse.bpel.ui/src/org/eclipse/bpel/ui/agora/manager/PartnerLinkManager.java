package org.eclipse.bpel.ui.agora.manager;

import java.util.ArrayList;
import java.util.List;

import org.apache.ode.bpel.extensions.comm.messages.engineIn.Write_PartnerLink;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.PartnerLink_Modification;
import org.eclipse.bpel.ui.BPELMultipageEditorPart;
import org.eclipse.bpel.ui.agora.communication.JMSCommunication;
import org.eclipse.bpel.ui.agora.provider.MonitoringProvider;
import org.eclipse.bpel.ui.agora.views.EventMessage;
import org.eclipse.bpel.ui.agora.views.IViewListener;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

/**
 * 
 * @author sonntamo
 */
public class PartnerLinkManager {
	
	// @vonstepk added methods for subscribing to modification events
	private static List<IViewListener> listeners = new ArrayList<IViewListener>();
	
	public static void registerAsListener(IViewListener obj) {
		if (obj != null) {
			listeners.add(obj);
		}
	}
	
	public static void informListeners() {
		for (IViewListener listener : listeners) {
				listener.update();
		}
	}

	public static void handlePartnerLinkModification(PartnerLink_Modification message, MonitorManager manager){
		XPathMapper.setPartnerLink(message.getPlXPath(), message.getPlValue(), message.getScopeID(), manager.getProcess());
		
		// Add the event to the view
		manager.getEventModelProvider().addEventMessage(
				new EventMessage(
						message.getClass()
								.getName()
								.substring(
										message.getClass().getName()
												.lastIndexOf(".") + 1), message
								.getPlXPath(), message.getPlName(), message.getTimeStamp(),
						message));
		
		// @vonstepk inform listeners of the change
		PartnerLinkManager.informListeners();
	}
	
	public static void writePartnerLink(String partnerLinkName, String xpath, String newEPR, Long scopeID){
		BPELMultipageEditorPart editor = null;
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		
		IEditorPart part = page.getActiveEditor();
		if (part instanceof BPELMultipageEditorPart) {
			editor = (BPELMultipageEditorPart) part;
		} else {
			System.out.println("Kein Editor gefunden");
		}
		
		if (scopeID != null){
			Write_PartnerLink message = new Write_PartnerLink();
			message.setProcessID(MonitoringProvider.getInstance().getMonitorManager(editor).getInstanceInformation().getInstanceID());
			message.setScopeID(scopeID);
			message.setPlName(partnerLinkName);
			message.setNewEPR(newEPR);
			JMSCommunication.getInstance().send(message);
		}
	}
}
