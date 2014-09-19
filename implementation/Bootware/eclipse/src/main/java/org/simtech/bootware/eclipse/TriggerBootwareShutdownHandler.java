package org.simtech.bootware.eclipse;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.ws.WebServiceException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;

import org.simtech.bootware.core.exceptions.ShutdownException;

/**
 * Handles the triggering of the bootware shutdown operation.
 */
public class TriggerBootwareShutdownHandler extends AbstractHandler {

	public TriggerBootwareShutdownHandler() {}

	public final Integer askForConfirmation() {
		final MessageBox dialog = new MessageBox(Display.getDefault().getActiveShell(), SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
		dialog.setText("Trigger Bootware Shutdown");
		dialog.setMessage("Triggering the bootware shutdown operation will undeploy"
				+ "all active process models, services, the SimTech SWfMS, and the bootware."
				+ "\nAre you sure you want to do this?");
		return dialog.open();
	}

	public final void triggerShutdown() {
		try {
			ShutdownState.set(true);

			final URL localBootwareURL = new URL("http://localhost:6007/axis2/services/Bootware?wsdl");

			// Create local bootware service.
			final LocalBootwareService localBootware = new LocalBootwareService(localBootwareURL);

			// Trigger shutdown operation.
			localBootware.shutdown();

			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					MessageDialog.openInformation(
							Display.getDefault().getActiveShell(),
							"Bootware Shutdown Successful",
							"The bootware shutdown process was successful.");
				}
			});

			ShutdownState.set(false);

		}
		catch (MalformedURLException e) {
			final String message = e.getMessage();
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					MessageDialog.openInformation(
							Display.getDefault().getActiveShell(),
							"Bootware Shutdown Error",
							"There was an error during the bootware shutdown process: " + message);
				}
			});
		}
		catch (WebServiceException e) {
			final String message = e.getMessage();
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					MessageDialog.openInformation(
							Display.getDefault().getActiveShell(),
							"Bootware Shutdown Error",
							"There was an error during the bootware shutdown process: " + message);
				}
			});
		}
		catch (ShutdownException e) {
			final String message = e.getMessage();
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					MessageDialog.openInformation(
							Display.getDefault().getActiveShell(),
							"Bootware Shutdown Error",
							"There was an error during the bootware shutdown process."
							+ "\n\nYou should manually check if there are any resources left running!"
							+ "\n\nMore details: " + message);
				}
			});
		}
	}

	@Override
	public final Object execute(final ExecutionEvent event) throws ExecutionException {

		// Ask user for confirmation.
		final Integer returnCode = askForConfirmation();

		// User confirmed. Shut down the bootware.
		final Integer ok = 32;
		if (returnCode == ok) {
			final Thread t = new Thread(new Runnable() {
				public void run() {
					triggerShutdown();
				}
			});
			t.start();
		}

		return null;
	}

}
