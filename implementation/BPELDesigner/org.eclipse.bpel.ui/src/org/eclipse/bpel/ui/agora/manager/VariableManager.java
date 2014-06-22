/**
 * 
 */
package org.eclipse.bpel.ui.agora.manager;

import java.util.ArrayList;
import java.util.List;

import org.apache.ode.bpel.extensions.comm.messages.engineIn.Write_Variable;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Variable_Modification;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Variable_Read;
import org.eclipse.bpel.ui.BPELMultipageEditorPart;
import org.eclipse.bpel.ui.agora.communication.JMSCommunication;
import org.eclipse.bpel.ui.agora.provider.MonitoringProvider;
import org.eclipse.bpel.ui.agora.views.EventMessage;
import org.eclipse.bpel.ui.agora.views.IViewListener;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

/**
 * @author aeichel
 * @author tolevar
 * 
 */
public class VariableManager {
	
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

	public static void handleVariableModification(
			Variable_Modification message, MonitorManager manager) {
		XPathMapper.setVariable(message.getVariableXPath(), message.getValue(),
				message.getScopeID(), manager.getProcess());

		// Add the event to the view
		manager.getEventModelProvider().addEventMessage(
				new EventMessage(
						message.getClass()
								.getName()
								.substring(
										message.getClass().getName()
												.lastIndexOf(".") + 1), message
								.getVariableXPath(), message.getVariableName(), message.getTimeStamp(),
						message));
		
		// @vonstepk inform listeners of the change
		VariableManager.informListeners();
	}

	// unused
	public static void handleVariableRead(Variable_Read message) {
		// TODO what to do with that?
	}

	public static void writeVariable(String variableName, String xpath,
			String changes, Long scopeID) {
		BPELMultipageEditorPart editor = null;
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();

		IEditorPart part = page.getActiveEditor();
		if (part instanceof BPELMultipageEditorPart) {
			editor = (BPELMultipageEditorPart) part;
		} else {
			System.out.println("Kein Editor gefunden");
		}

		if (scopeID != null) {
			Write_Variable message = new Write_Variable();
			message.setProcessID(MonitoringProvider.getInstance()
					.getMonitorManager(editor).getInstanceInformation()
					.getInstanceID());
			message.setScopeID(scopeID);
			message.setVariableName(variableName);
			message.setChanges(changes);
			JMSCommunication.getInstance().send(message);
		}
	}
}
