package org.simtech.bootware.eclipse;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;

import javax.xml.ws.WebServiceException;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Instance_Terminated;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Process_Instantiated;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import org.simtech.bootware.core.exceptions.ShutdownException;

/**
 * Handles the bootware shutdown functionality.
 */
public final class ShutdownTrigger {

	private static Boolean active;
	private static Boolean triggered;
	private static Integer returnCode;
	private static MessageConsoleStream out;

	// Set up variables and get bootware console.
	static {
		active = false;
		triggered = false;
		returnCode = 0;
		final MessageConsole console = Util.findConsole("Bootware");
		out = console.newMessageStream();
	}

	private ShutdownTrigger() {}

	/**
	 * Returns the state of the bootware trigger.
	 *
	 * @return True if the shutdown trigger is active, false otherwise.
	 */
	public static Boolean isActive() {
		return active;
	}

	/**
	 * Returns the state of the shutdown process.
	 *
	 * @return True if the shutdown process is active, false otherwise.
	 */
	public static Boolean isTriggered() {
		return triggered;
	}

	/**
	 * Starts the shutdown trigger.
	 * <p>
	 * The shutdown trigger will listen at the given activeMQUrl for specific
	 * events and trigger the shutdown operation if those events occur.
	 *
	 * @param activeMQUrl The URL to the ActiveMQ broker.
	 * @param latch A CountDownLatch that will be counted down as soon as the shutdown trigger is ready.
	 */
	public static void start(final String activeMQUrl, final CountDownLatch latch) {
		try {
			out.println("Shutdown trigger is now listening at " + activeMQUrl);

			// Set up connection to ActiveMQ broker.
			final ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(activeMQUrl);
			final Connection connection = connectionFactory.createConnection();
			final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			final Topic topic = session.createTopic("org.apache.ode.events");
			connection.start();

			// Register ODEListener as listener with the ActiveMQ broker.
			final MessageConsumer consumer = session.createConsumer(topic);
			final MessageListener listener = new ShutdownTrigger.ODEListener();
			consumer.setMessageListener(listener);

			// The shutdown trigger is now active.
			active = true;

			// Signal caller that shutdown trigger is now running.
			latch.countDown();

			// Wait until the shutdown trigger should be stopped.
			while (active) {
				try {
					final Integer wait = 10;
					Thread.sleep(wait);
				}
				catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}

			connection.close();
			out.println("Shutdown trigger stopped.");
		}
		catch (JMSException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Stop the shutdown trigger when it was started with start().
	 */
	public static void stop() {
		active = false;
	}

	/**
	 * Show a dialog window to the user to ask, if the shutdown process should be started.
	 * <p>
	 * The answer is saved in ShutdownTrigger.returnCode.
	 */
	public static void askForConfirmation() {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				final MessageBox dialog = new MessageBox(Display.getDefault().getActiveShell(), SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
				dialog.setText("Trigger Bootware Shutdown");
				dialog.setMessage("Triggering the bootware shutdown operation will undeploy"
						+ "all active process models, services, the SimTech SWfMS, and the bootware."
						+ "\nAre you sure you want to do this?");
				returnCode = dialog.open();
			}
		});
	}

	/**
	 * Show a message dialog to the user.
	 *
	 * @param titel The titel of the message dialog.
	 * @param message The message shown on the message dialog.
	 */
	public static void showMessage(final String titel, final String message) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				MessageDialog.openInformation(
						Display.getDefault().getActiveShell(),
						titel,
						message);
			}
		});
	}

	/**
	 * Triggers the shutdown process.
	 * <p>
	 * The shutdown process is triggered by calling the shutdown() operation of the local bootware.
	 */
	public static void trigger() {

		// Do nothing if the shutdown process is already active.
		if (triggered) {
			showMessage("Already Shutting Down", "The bootware is already shutting down. Please wait until the process has finished.");
			return;
		}

		try {
			triggered = true;

			askForConfirmation();

			// If the user clicked OK, start the shutdown process.
			final Integer ok = 32;
			if (returnCode == ok) {
				out.println("Bootware shutdown has been triggered.");

				// Create local bootware service.
				final URL localBootwareURL = new URL("http://localhost:6007/axis2/services/Bootware?wsdl");
				final LocalBootwareService localBootware = new LocalBootwareService(localBootwareURL);

				// Trigger shutdown operation.
				localBootware.shutdown();

				showMessage("Bootware Shutdown Successful", "The bootware shutdown process was successful.");
			}
			else {
				out.println("User canceled bootware shutdown.");
			}

		}
		catch (MalformedURLException e) {
			showMessage("Bootware Shutdown Error", "There was an error during the bootware shutdown process: " + e.getMessage());
		}
		catch (WebServiceException e) {
			showMessage("Bootware Shutdown Error", "There was an error during the bootware shutdown process: " + e.getMessage());
		}
		catch (ShutdownException e) {
			showMessage("Bootware Shutdown Error",
					"There was an error during the bootware shutdown process."
					+ "\n\nYou should manually check if there are any resources left running!"
					+ "\n\nMore details: " + e.getMessage());
		}
		finally {
			triggered = false;
		}
	}

	/**
	 * A MessageListener that handles messages from a ActiveMQ Broker
	 */
	private static class ODEListener implements MessageListener {

		// The currently active process instances counted by the listener.
		private Integer activeProcesses = 0;

		public ODEListener() {};

		public void onMessage(final Message message) {

			// Do some checks.
			if (!(message instanceof ObjectMessage)) {
				return;
			}

			final ObjectMessage oMsg = (ObjectMessage) message;
			Serializable obj = null;

			try {
				obj = oMsg.getObject();
			}
			catch (JMSException e) {
				e.printStackTrace();
				return;
			}

			if (obj == null) {
				return;
			}

			// Count the active process instances. When 0 is reached, trigger shutdown process.
			if (obj instanceof Process_Instantiated) {
				activeProcesses = activeProcesses + 1;
				out.println("Active processes instances: " + activeProcesses);
			}
			if (obj instanceof Instance_Terminated) {
				activeProcesses = activeProcesses - 1;
				out.println("Active processes instances: " + activeProcesses);
				if (activeProcesses == 0) {
					out.println("No active processes instances left. Triggering bootware shutdown...");
					trigger();
				}
			}

		}
	};

}
