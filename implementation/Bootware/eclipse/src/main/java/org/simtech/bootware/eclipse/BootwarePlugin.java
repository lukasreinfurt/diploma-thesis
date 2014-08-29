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
// import java.net.MalformedURLException;
// import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
// import javax.xml.ws.WebServiceException;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Process_Deployed;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Process_Undeployed;

import org.eclipse.bpel.ui.IBootwarePlugin;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import org.simtech.bootware.core.ConfigurationListWrapper;
// import org.simtech.bootware.core.InformationListWrapper;
import org.simtech.bootware.core.UserContext;
// import org.simtech.bootware.core.exceptions.DeployException;
// import org.simtech.bootware.core.exceptions.SetConfigurationException;

//import fragmentorcp.FragmentoPlugIn;
//import fragmentorcppresenter.presenter.Presenter;

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

	private MessageConsole myConsole;
	private MessageConsoleStream out;
	private UserContext context;
	private ConfigurationListWrapper defaultConfiguration;
	private Boolean stopShutdownTrigger = false;
	private Boolean bootwareRunning = false;

	/**
	 * Creates the bootware plugin.
	 */
	public BootwarePlugin() {
		myConsole = findConsole("Bootware");
		out = myConsole.newMessageStream();
		out.println("Bootware Plugin has been started.");
	}

	/**
	 * Finds a console by the given name.
	 * <p>
	 * A console by the given name is created if it doesn't exist already.
	 *
	 * @param name The name of the requested console.
	 *
	 * @return The requested console.
	 */
	private MessageConsole findConsole(final String name) {

		final ConsolePlugin plugin = ConsolePlugin.getDefault();
		final IConsoleManager conMan = plugin.getConsoleManager();
		final IConsole[] existing = conMan.getConsoles();

		// Get the console if it already exists.
		for (int i = 0; i < existing.length; i++) {
			if (name.equals(existing[i].getName())) {
				return (MessageConsole) existing[i];
			}
		}

		// Create the requested console if it doesn't exist already.
		final MessageConsole newConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[]{newConsole});
		return newConsole;
	}

	/**
	 * Starts the local bootware.
	 */
	private void executeLocalBootware() {
		out.println("Starting local bootware.");
		bootwareRunning = true;

		// check if file exists

		// check if bootware already running

		// Create local bootware process.
		final ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", "bootware-local-1.0.0.jar");
		processBuilder.directory(new File("plugins/bootware/bin"));
		processBuilder.redirectErrorStream(true);

		try {
			// Start local bootware.
			final Process process = processBuilder.start();


			final OutputStream processOutput = process.getOutputStream();
			final InputStream processInput = process.getInputStream();
			final BufferedReader processReader = new BufferedReader(new InputStreamReader(processInput));
			final BufferedWriter processWriter = new BufferedWriter(new OutputStreamWriter(processOutput));
			String line;

			// Write local bootware output to console. This will block until the
			// local bootware is terminated.
			while ((line = processReader.readLine()) != null) {
				out.println(line);
			}

			processReader.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		bootwareRunning = false;
		out.println("Local bootware stopped.");
	}

	/**
	 * Loads the user context that will be send to the local bootware to deploy
	 * the local bootware and the SimTech SWfMS.
	 */
	private void loadContext() {
		try {
			final JAXBContext jaxbContext = JAXBContext.newInstance(UserContext.class);
			final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			final File file = new File("plugins/bootware/context.xml");
			final JAXBElement<UserContext> root = unmarshaller.unmarshal(new StreamSource(file), UserContext.class);

			context = root.getValue();
		}
		catch (JAXBException e) {
			out.println("Loading context failed: " + e.getMessage());
			out.println(e.toString());
		}
	}

	/**
	 * Loads the default configuration that will be send to the local and local
	 * bootware.
	 */
	private void loadDefaultConfiguration() {
		try {
			final JAXBContext jaxbContext = JAXBContext.newInstance(ConfigurationListWrapper.class);
			final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			final File file = new File("plugins/bootware/defaultConfiguration.xml");
			final JAXBElement<ConfigurationListWrapper> root = unmarshaller.unmarshal(new StreamSource(file), ConfigurationListWrapper.class);

			defaultConfiguration = root.getValue();
		}
		catch (JAXBException e) {
			out.println("Loading context failed: " + e.getMessage());
			out.println(e.toString());
		}
	}

	/**
	 * Sets the SimTech preferences to the values provided in the given map.
	 *
	 * @param preferences A map of strings with the preference values.
	 */
	private void setSimTechPreferences(final Map<String, String> preferences) {

		out.println("Setting SimTech preferences.");
		final MapConfiguration configuration = new MapConfiguration(preferences);
		configuration.setThrowExceptionOnMissing(true);

		try {
			// SimTech settings
			final IPreferenceStore simTechStore = new ScopedPreferenceStore(new InstanceScope(), "org.eclipse.bpel.ui");

			simTechStore.setValue("ACTIVE_MQ_URL", configuration.getString("activeMQUrl"));
			//simTechStore.setValue("SEND_REQUESTS", true);
			//simTechStore.setValue("USE_EXT_ITERATION", false);
			//simTechStore.setValue("INSTANCE_WAITING_TIME", "200");

			// ODE settings
			final IPreferenceStore odeStore = new ScopedPreferenceStore(new InstanceScope(), "org.eclipse.apache.ode.processManagement");

			odeStore.setValue("pref_ode_url", configuration.getString("odeServerUrl"));
			//odeStore.setValue("pref_ode_version", "ODE_Version_134");

			// Fragmento settings
			out.println(configuration.getString("fragmentoUrl"));
			//final Presenter presenter = FragmentoPlugIn.getDefault().getPresenter();
			//presenter.getOperator().getFragmento().setServiceURI("Blub");

		}
		catch (NoSuchElementException e) {
			out.println("There was an error while setting the SimTech preferences: " + e.getMessage());
		}
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

					if (obj instanceof Process_Deployed) {
						activeProcesses = activeProcesses + 1;
						out.println("Active processes: " + activeProcesses);
					}
					if (obj instanceof Process_Undeployed) {
						activeProcesses = activeProcesses - 1;
						out.println("Active processes: " + activeProcesses);
						if (activeProcesses == 0) {
							out.println("No active processes left. Triggering bootware shutdown...");

							Display.getDefault().asyncExec(new Runnable() {

								public void run() {
									final TriggerBootwareShutdownHandler shutdownHandler = new TriggerBootwareShutdownHandler();

									// Ask for user confirmation.
									final Integer returnCode = shutdownHandler.askForConfirmation();

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

							});

						}
					}

				}
			};

			consumer.setMessageListener(listener);

			while (!stopShutdownTrigger) {
				try {
					final Integer wait = 10;
					Thread.sleep(wait);
				}
				catch (InterruptedException e) {
					e.printStackTrace();
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
	public final void execute() {

		if (bootwareRunning) {
			return;
		}

		// Start local bootware process in new thread so that we don't block further
		// execution.
		final Thread localBootwareThread = new Thread(new Runnable() {

			public void run() {
				executeLocalBootware();
			}

		});
		localBootwareThread.start();

		// Load user context and default configuration.
		loadContext();
		loadDefaultConfiguration();

		final Map<String, String> informationList;

		// Deploy the middleware.
		// try {
		// 	final URL localBootwareURL = new URL("http://localhost:6007/axis2/services/Bootware?wsdl");

		// 	// Create local bootware service.
		// 	out.println("Connecting to local bootware.");
		// 	final LocalBootwareService localBootware = new LocalBootwareService(localBootwareURL);
		// 	out.println("Local bootware started at " + localBootwareURL + ".");

		// 	// Send default configuration to local bootware.
		// 	localBootware.setConfiguration(defaultConfiguration);

		// 	// Send deploy request for remote bootware and SimTech SWfMS to local bootware.
		// 	final InformationListWrapper informationListWrapper = localBootware.deploy(context);

		// 	// Unwrap response
		// 	informationList = informationListWrapper.getInformationList();
		// }
		// catch (MalformedURLException e) {
		// 	out.println("Local bootware URL is malformed: " + e.getMessage());
		// }
		// catch (WebServiceException e) {
		// 	out.println("Connecting to local bootware failed: " + e.getMessage());
		// }
		// catch (SetConfigurationException e) {
		// 	out.println("Could not set default configuration: " + e.getMessage());
		// }
		// catch (DeployException e) {
		// 	out.println("Deploy request failed: " + e.getMessage());
		// }

		informationList = new HashMap<String, String>();
		informationList.put("odeServerUrl", "http://localhost:8080/ode");
		informationList.put("activeMQUrl", "tcp://localhost:61616");
		informationList.put("fragmentoUrl", "fragmento");
		for (Map.Entry<String, String> entry : informationList.entrySet()) {
			out.println(entry.getKey() + ": " + entry.getValue());
		}

		if (informationList == null) {
			out.println("fail");
		}

		// Set the SimTech preferences from the response.
		setSimTechPreferences(informationList);

		// Initialize the shutdown trigger in new thread so that we don't block
		// further execution.
		final Thread shutdownTriggerThread = new Thread(new Runnable() {

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

	}

}
