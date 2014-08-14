package org.simtech.bootware.eclipse;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
//import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
//import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Response;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;


import org.eclipse.bpel.ui.IBootwarePlugin;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import org.simtech.bootware.core.ConfigurationListWrapper;
import org.simtech.bootware.core.ConfigurationWrapper;
import org.simtech.bootware.core.InformationListWrapper;
import org.simtech.bootware.core.UserContext;
import org.simtech.bootware.local.DeployResponse;

import async.client.LocalBootware;

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
	private Map<String, ConfigurationWrapper> defaultConfiguration;

	public BootwarePlugin() {
		myConsole = findConsole("Bootware");
		out = myConsole.newMessageStream();
		log("Bootware Plugin has been started.");
	}

	private void log(final String message) {
		try {
			out.println(message);
			out.flush();
		}
		catch (IOException e) {
			out.println(e.getMessage());
		}

	}

	private MessageConsole findConsole(final String name) {
		final ConsolePlugin plugin = ConsolePlugin.getDefault();
		final IConsoleManager conMan = plugin.getConsoleManager();
		final IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++) {
			if (name.equals(existing[i].getName())) {
				return (MessageConsole) existing[i];
			}
		}
		//no console found, so create a new one
		final MessageConsole newConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[]{newConsole});
		return newConsole;
	}

	private void executeLocalBootware() {
		log("Starting local bootware.");

		final ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", "bootware-local-1.0.0.jar");
		processBuilder.directory(new File("plugins/bootware/bin"));
		processBuilder.redirectErrorStream(true);

		try {
			final Process process = processBuilder.start();
			final OutputStream processOutput = process.getOutputStream();
			final InputStream processInput = process.getInputStream();
			final BufferedReader processReader = new BufferedReader(new InputStreamReader(processInput));
			final BufferedWriter processWriter = new BufferedWriter(new OutputStreamWriter(processOutput));
			String line;
			while ((line = processReader.readLine()) != null) {
				log(line);
			}
			processReader.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		log("Local bootware stopped.");
	}

	private void loadContext() {
		try {
			final JAXBContext jaxbContext = JAXBContext.newInstance(UserContext.class);
			final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			final File file = new File("plugins/bootware/context.xml");
			final JAXBElement<UserContext> root = unmarshaller.unmarshal(new StreamSource(file), UserContext.class);

			context = root.getValue();
		}
		catch (JAXBException e) {
			log("Loading context failed: " + e.getMessage());
			log(e.toString());
		}
	}

	private void loadDefaultConfiguration() {
		try {
			// final ConfigurationListWrapper test0 = new ConfigurationListWrapper();
			// final Map<String, ConfigurationWrapper> test = new HashMap<String, ConfigurationWrapper>();
			// final ConfigurationWrapper test2 = new ConfigurationWrapper();
			// final Map<String, String> test3 = new HashMap<String, String>();

			// test3.put("Test", "123");
			// test2.setConfiguration(test3);
			// test.put("abc", test2);
			// test0.setConfigurationList(test);

			//final QName qName = new QName("", "configurationList");
			//final JAXBElement root = new JAXBElement(qName, test.getClass(), test);

			// final JAXBContext jaxbContext = JAXBContext.newInstance(test0.getClass());
			// final Marshaller marshaller = jaxbContext.createMarshaller();
			// final File file = new File("plugins/bootware/defaultConfiguration.xml");
			// marshaller.marshal(test0, file);

			final JAXBContext jaxbContext = JAXBContext.newInstance(ConfigurationListWrapper.class);
			final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			final File file = new File("plugins/bootware/defaultConfiguration.xml");
			final JAXBElement<ConfigurationListWrapper> root = unmarshaller.unmarshal(new StreamSource(file), ConfigurationListWrapper.class);
			final ConfigurationListWrapper wrapper = root.getValue();
			defaultConfiguration = wrapper.getConfigurationList();
		}
		catch (JAXBException e) {
			log("Loading context failed: " + e.getMessage());
			log(e.toString());
		}
	}

	private void setPreferences() {
		// SimTech settings
		final IPreferenceStore simTechStore = new ScopedPreferenceStore(new InstanceScope(), "org.eclipse.bpel.ui");

		simTechStore.setValue("ACTIVE_MQ_URL", "http://set.by.bootware.org/activemq");
		simTechStore.setValue("SEND_REQUESTS", true);
		simTechStore.setValue("USE_EXT_ITERATION", false);
		simTechStore.setValue("INSTANCE_WAITING_TIME", "200");

		// ODE settings
		final IPreferenceStore odeStore = new ScopedPreferenceStore(new InstanceScope(), "org.eclipse.apache.ode.processManagement");

		odeStore.setValue("pref_ode_url", "http://set.by.bootware.org/ode");
		odeStore.setValue("pref_ode_version", "ODE_Version_134");

	}

	public final void execute() {

		final Thread t = new Thread(new Runnable() {

			public void run() {
				executeLocalBootware();
			}

		});
		t.start();

		loadContext();
		loadDefaultConfiguration();

		Thread t2 = new Thread(new Runnable() {

			public void run() {
				final QName qname = new QName("http://local.bootware.simtech.org/", "LocalBootwareImplService");
				Service service = null;
				final int max = 30;
				int count = 1;

				while (true) {
					try {
						final Integer time = 1000;
						Thread.sleep(time);
						log("Trying to connect to local bootware (" + count + "/" + max + ").");
						service = Service.create(new URL("http://localhost:6007/axis2/services/Bootware?wsdl"), qname);
						break;
					}
					catch (WebServiceException e) {
						if (++count == max) {
							log("Connecting to local bootware failed: " + e.getMessage());
							break;
						}
					}
					catch (MalformedURLException e) {
						log("Local bootware URL seems to be wrong: " + e.getMessage());
						break;
					}
					catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						break;
					}
				}

				if (service != null) {
					try {
						final LocalBootware lb = service.getPort(LocalBootware.class);

						final Response<DeployResponse> response = lb.deployAsync(context);

						while (!response.isDone()) {
							final Integer time = 1000;
							Thread.sleep(time);
						}

						final InformationListWrapper infos = response.get().getReturn();
					}
					catch (WebServiceException e) {
						log("Retrieving service port failed: " + e.getMessage());
						final StringWriter sw = new StringWriter();
						e.printStackTrace(new PrintWriter(sw));
						log(sw.toString());
					}
					catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
					catch (ExecutionException e) {
						log("Executing deploy on local bootware failed: " + e.getMessage());
					}
				}
			}

		});
		t2.start();

		setPreferences();

	}

}
