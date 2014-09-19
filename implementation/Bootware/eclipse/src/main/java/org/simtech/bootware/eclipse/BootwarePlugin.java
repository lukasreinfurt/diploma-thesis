package org.simtech.bootware.eclipse;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.CountDownLatch;

import javax.xml.bind.JAXBException;
import javax.xml.ws.WebServiceException;

import org.apache.commons.configuration.MapConfiguration;

import org.eclipse.bpel.ui.IBootwarePlugin;
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
	private Thread localBootwareThread;
	private LocalBootwareService localBootware;
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
	 * Stops the bootware. Forcefully if necessary
	 */
	private void stopBootware() {
		try {
			if (localBootware != null) {
				out.println("Trying to shutdown bootware normally.");
				localBootware.shutdown();
			}
			else if (localBootwareThread != null && localBootwareThread.isAlive()) {
				out.println("Forcefully stopping local bootware process.");
				LocalBootwareProcess.stop();
				localBootwareThread.join();
			}
		}
		catch (ShutdownException ex) {
			out.println("Shutting down bootware failed: " + ex.getMessage());
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * Checks if the bootware is currently shutting down.
	 */
	public final Boolean isShuttingDown() {
		return ShutdownTrigger.isTriggered();
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
				LocalBootwareProcess.start();
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
			stopBootware();
			return;
		}
		catch (WebServiceException e) {
			out.println("Connecting to local bootware failed: " + e.getMessage());
			stopBootware();
			return;
		}
		catch (SetConfigurationException e) {
			out.println("Could not set default configuration: " + e.getMessage());
			stopBootware();
			return;
		}
		catch (DeployException e) {
			out.println("Deploy request failed: " + e.getMessage());
			stopBootware();
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
					ShutdownTrigger.start(activeMQUrl, shutdownTriggerLatch);
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
