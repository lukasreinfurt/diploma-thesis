package org.simtech.bootware.eclipse;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.ws.Response;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;

import org.eclipse.bpel.ui.IBootwarePlugin;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import org.simtech.bootware.core.Context;
import org.simtech.bootware.core.InformationListWrapper;
import org.simtech.bootware.local.DeployResponse;

import async.client.LocalBootware;

@SuppressWarnings({
	"checkstyle:anoninnerlength",
	"checkstyle:classfanoutcomplexity",
	"checkstyle:javancss",
	"checkstyle:cyclomaticcomplexity"
})
public class BootwarePlugin implements IBootwarePlugin {

	private MessageConsole myConsole;
	private MessageConsoleStream out;
	private Context context;

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

	public final String execute() {

		Thread t = new Thread(new Runnable() {

			public void run() {
				log("Starting local bootware.");

				final ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", "bootware-local-1.0.0.jar");
				processBuilder.directory(new File("plugins/Bootware-bin/bin"));
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

		});
		t.start();

		context = new Context();
		context.setResourcePlugin("test-1.0.0.jar");
		context.setCommunicationPlugin("2");
		context.setApplicationPlugin("3");

		try {
			final JAXBContext jaxbContext = JAXBContext.newInstance(Context.class);

			final Marshaller marshaller = jaxbContext.createMarshaller();
			final OutputStream os = new FileOutputStream("plugins/Bootware-bin/sample.xml");
			marshaller.marshal(context, os);
			os.close();

			final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			final File file = new File("plugins/Bootware-bin/context.xml");
			context = (Context) unmarshaller.unmarshal(file);

			log(context.getResourcePlugin());
			log(context.getCommunicationPlugin());
			log(context.getApplicationPlugin());
		}
		catch (FileNotFoundException e) {
			log("File not found: " + e.getMessage());
		}
		catch (IOException e) {
			log(e.getMessage());
		}
		catch (JAXBException e) {
			log("Loading context failed: " + e.getMessage());
			log(e.toString());
		}

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
						log(e.toString());
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
					}
					catch (InterruptedException e) {
						log("Local bootware failed: " + e.getMessage());
					}
					catch (ExecutionException e) {
						log("Executing deploy on local bootware failed: " + e.getMessage());
					}
				}
			}

		});
		t2.start();

		return "this is ...";
	}

}
