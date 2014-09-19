package org.simtech.bootware.eclipse;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.NoSuchElementException;
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
import javax.xml.bind.JAXBException;
import javax.xml.ws.WebServiceException;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Instance_Terminated;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Process_Instantiated;

import org.eclipse.bpel.ui.IBootwarePlugin;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import org.simtech.bootware.core.ConfigurationListWrapper;
import org.simtech.bootware.core.InformationListWrapper;
import org.simtech.bootware.core.UserContext;
import org.simtech.bootware.core.exceptions.DeployException;
import org.simtech.bootware.core.exceptions.SetConfigurationException;
import org.simtech.bootware.core.exceptions.ShutdownException;

/**
 * Starts the bootstrapping process.
 * <p>
 * This plugin starts the local bootware and triggers the deployment of the
 * local bootware as well as the SimTech SWfMS. It uses the response of the
 * SimTech SWfMS deployment to set the SimTech properties in the Modeler.
 */
@SuppressWarnings({
	"checkstyle:anoninnerlength",
	"checkstyle:classfanoutcomplexity",
	"checkstyle:javancss",
	"checkstyle:cyclomaticcomplexity",
	"checkstyle:classdataabstractioncoupling"
})
public class BootwarePlugin implements IBootwarePlugin {

	private MessageConsoleStream out;
	private UserContext context;
	private ConfigurationListWrapper defaultConfiguration;
	private Boolean stopShutdownTrigger = false;
	private Process localBootwareProcess;
	private Thread localBootwareThread;
	private Thread shutdownTriggerThread;
	private CountDownLatch shutdownTriggerLatch;

	/**
	 * Creates the bootware plugin.
	 */
	public BootwarePlugin() {
		final MessageConsole console = Util.findConsole("Bootware");
		out = console.newMessageStream();
		out.println("Bootware Plugin has been started.");
	}

	/**
	 * Starts the local bootware.
	 */
	private void executeLocalBootware() {
		out.println("Starting local bootware.");

		// check if file exists

		// check if bootware already running

		// Create local bootware process.
		final ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", "bootware-local-1.0.0.jar");
		processBuilder.directory(new File("plugins/bootware/bin"));
		processBuilder.redirectErrorStream(true);

		BufferedReader processReader = null;
		BufferedWriter processWriter = null;
		try {
			// Start local bootware.
			localBootwareProcess = processBuilder.start();

			final OutputStream processOutput = localBootwareProcess.getOutputStream();
			final InputStream processInput = localBootwareProcess.getInputStream();
			processReader = new BufferedReader(new InputStreamReader(processInput));
			processWriter = new BufferedWriter(new OutputStreamWriter(processOutput));
			String line;

			// Write local bootware output to console. This will block until the
			// local bootware is terminated.
			while ((line = processReader.readLine()) != null) {
				out.println(line);
			}
		}
		catch (IOException e) {
			out.println("There was an error while reading the output of the bootware process: " + e.getMessage());
		}
		finally {
			try {
				if (processReader != null) {
					processReader.close();
				}
				if (processWriter != null) {
					processWriter.close();
				}
			}
			catch (IOException e) {
				out.println("There was an error: " + e.getMessage());
			}
		}

		stopShutdownTrigger = true;
		out.println("Local bootware stopped.");
	}

	private void initializeShutdownTrigger(final String activeMQUrl) {
		try {
			out.println("Shutdown trigger is now listening at " + activeMQUrl);

			final ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(activeMQUrl);
			final Connection connection = connectionFactory.createConnection();
			final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			final Topic topic = session.createTopic("org.apache.ode.events");
			connection.start();

			final MessageConsumer consumer = session.createConsumer(topic);

			final MessageListener listener = new MessageListener() {

				private Integer activeProcesses = 0;
				private Integer returnCode = 0;

				public void onMessage(final Message message) {

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

					if (obj instanceof Process_Instantiated) {
						activeProcesses = activeProcesses + 1;
						out.println("Active processes instances: " + activeProcesses);
					}
					if (obj instanceof Instance_Terminated) {
						activeProcesses = activeProcesses - 1;
						out.println("Active processes instances: " + activeProcesses);
						if (activeProcesses == 0) {
							out.println("No active processes instances left. Triggering bootware shutdown...");

							final TriggerBootwareShutdownHandler shutdownHandler = new TriggerBootwareShutdownHandler();

							// Ask for user confirmation.
							Display.getDefault().syncExec(new Runnable() {
								public void run() {
									returnCode = shutdownHandler.askForConfirmation();
								}
							});

							// User confirmed. Shut down the bootware.
							final Integer ok = 32;
							if (returnCode == ok) {
								stopShutdownTrigger = true;
								out.println("Bootware shutdown has been triggered.");
								shutdownHandler.triggerShutdown();
							}
							else {
								out.println("User canceled bootware shutdown.");
							}

						}
					}

				}
			};

			consumer.setMessageListener(listener);

			// Signal to outer threat to continue;
			shutdownTriggerLatch.countDown();

			stopShutdownTrigger = false;
			while (!stopShutdownTrigger) {
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
	 * Executes the bootstrapping process.
	 */
	@SuppressWarnings({
		"checkstyle:npathcomplexity",
		"checkstyle:executablestatementcount",
		"checkstyle:methodlength"
	})
	public final void execute() {

		if (localBootwareThread != null && localBootwareThread.isAlive()) {
			out.println("Local bootware is already running. Skipping bootstrapping.");
			return;
		}

		// Start local bootware process in new thread so that we don't block further
		// execution.
		localBootwareThread = new Thread(new Runnable() {

			public void run() {
				executeLocalBootware();
			}

		});
		localBootwareThread.start();

		// Load user context and default configuration.
		try {
			context = Util.loadXML(UserContext.class, "plugins/bootware/context.xml");
			defaultConfiguration = Util.loadXML(ConfigurationListWrapper.class, "plugins/bootware/defaultConfiguration.xml");
		}
		catch (JAXBException e) {
			out.println("There was an error while loading an XML file: " + e.getMessage());
		}

		final Map<String, String> informationList;

		// Deploy the middleware.
		LocalBootwareService localBootware = null;
		try {
			final URL localBootwareURL = new URL("http://localhost:6007/axis2/services/Bootware?wsdl");

			// Create local bootware service.
			out.println("Connecting to local bootware.");
			localBootware = new LocalBootwareService(localBootwareURL);
			out.println("Local bootware started at " + localBootwareURL + ".");

			// Wait for local bootware to be ready.
			out.println("Wait for local bootware to be ready.");
			final Integer max = 10;
			final Integer wait = 5000;
			for (Integer i = 0; i <= max; i++) {
				if (localBootware.isReady()) {
					break;
				}
				out.println("Local bootware is not ready yet.");
				try {
					Thread.sleep(wait);
				}
				catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
			out.println("Local bootware is ready.");

			// Send default configuration to local bootware.
			localBootware.setConfiguration(defaultConfiguration);

			// Send deploy request for remote bootware and SimTech SWfMS to local bootware.
			final InformationListWrapper informationListWrapper = localBootware.deploy(context);

			// Unwrap response
			informationList = informationListWrapper.getInformationList();
		}
		// Shutdown local bootware if something didn't work.
		catch (MalformedURLException e) {
			out.println("Local bootware URL is malformed: " + e.getMessage());
			localBootwareProcess.destroy();
			try {
				localBootwareThread.join();
			}
			catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
			return;
		}
		catch (WebServiceException e) {
			out.println("Connecting to local bootware failed: " + e.getMessage());
			localBootwareProcess.destroy();
			try {
				localBootwareThread.join();
			}
			catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
			return;
		}
		catch (SetConfigurationException e) {
			out.println("Could not set default configuration: " + e.getMessage());
			try {
				if (localBootware != null) {
					localBootware.shutdown();
				}
			}
			catch (ShutdownException ex) {
				out.println("Shutting down bootware failed: " + ex.getMessage());
			}
			return;
		}
		catch (DeployException e) {
			out.println("Deploy request failed: " + e.getMessage());
			try {
				if (localBootware != null) {
					localBootware.shutdown();
				}
			}
			catch (ShutdownException ex) {
				out.println("Shutting down bootware failed: " + ex.getMessage());
			}
			return;
		}

		// Set the SimTech preferences from the response.
		SimTechPreferences.update(informationList);

		// Initialize the shutdown trigger in new thread so that we don't block
		// further execution.
		shutdownTriggerLatch = new CountDownLatch(1);
		shutdownTriggerThread = new Thread(new Runnable() {

			public void run() {

				final MapConfiguration configuration = new MapConfiguration(informationList);
				configuration.setThrowExceptionOnMissing(true);

				try {
					final String activeMQUrl = configuration.getString("activeMQUrl");
					initializeShutdownTrigger(activeMQUrl);
				}
				catch (NoSuchElementException e) {
					out.println("There was an error while initializing the shutdown trigger: " + e.getMessage());
				}
			}

		});
		shutdownTriggerThread.start();

		// Wait for shutdown trigger to be ready.
		try {
			shutdownTriggerLatch.await();
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

	}

}
