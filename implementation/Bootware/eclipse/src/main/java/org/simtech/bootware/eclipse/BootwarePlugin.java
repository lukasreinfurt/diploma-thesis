package org.simtech.bootware.eclipse;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.eclipse.bpel.ui.IBootwarePlugin;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

@SuppressWarnings({
	"checkstyle:anoninnerlength",
	"checkstyle:classfanoutcomplexity"
})
public class BootwarePlugin implements IBootwarePlugin {

	private MessageConsole myConsole;
	private MessageConsoleStream out;

	public BootwarePlugin() {
		myConsole = findConsole("Bootware");
		out = myConsole.newMessageStream();
		out.println("Bootware Plugin has been started.");
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
				out.println("Starting local bootware.");

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
						out.println(line);
					}
					processReader.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}

				out.println("Local bootware stopped.");
			}

		});
		t.start();

		return "this is ...";
	}

}
