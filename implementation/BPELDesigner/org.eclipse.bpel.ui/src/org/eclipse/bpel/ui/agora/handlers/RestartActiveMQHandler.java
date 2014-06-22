package org.eclipse.bpel.ui.agora.handlers;

import org.eclipse.bpel.ui.agora.communication.JMSCommunication;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

/**
 * This class handles the restart of the ActiveMQ connection.
 * 
 * @author hahnml
 * 
 */
public class RestartActiveMQHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		JMSCommunication.getInstance().restart();
		
		MessageDialog.openInformation(Display.getDefault().getActiveShell(), "Connection successfully established", "Connection to ActiveMQ successfully established.");

		return null;
	}

}
