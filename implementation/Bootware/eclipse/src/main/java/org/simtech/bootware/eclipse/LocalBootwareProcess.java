package org.simtech.bootware.eclipse;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * Manages the local bootware process.
 */
public final class LocalBootwareProcess {

	private static MessageConsoleStream out;
	private static File localBootwareExecutable;
	private static Process process;

	// Get the bootware console.
	static {
		final MessageConsole console = Util.findConsole("Bootware");
		out = console.newMessageStream();
		localBootwareExecutable = new File("plugins/bootware/bin/bootware-local-1.0.0.jar");
	}

	private LocalBootwareProcess() {}

	/**
	 * Starts the local bootware.
	 */
	public static void start() {
		out.println("Starting local bootware.");

		// check if file exists
		if (!localBootwareExecutable.exists()) {
			out.println("Could not find local bootware executable at " + localBootwareExecutable.toString());
			return;
		}

		// check if bootware already running
		if (process != null) {
			out.println("Local bootware process is already running");
			return;
		}

		// Create local bootware process.
		final ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", localBootwareExecutable.getName());
		processBuilder.directory(new File(localBootwareExecutable.getParent()));
		processBuilder.redirectErrorStream(true);

		BufferedReader processReader = null;
		BufferedWriter processWriter = null;
		try {
			// Start local bootware.
			process = processBuilder.start();

			final OutputStream processOutput = process.getOutputStream();
			final InputStream processInput = process.getInputStream();
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

		// Stop the shutdown trigger.
		ShutdownTrigger.stop();
		process = null;
		out.println("Local bootware stopped.");
	}

	/**
	 * Forcefully stops the local bootware process.
	 */
	public static void stop() {
		process.destroy();
		process = null;
	}

}
