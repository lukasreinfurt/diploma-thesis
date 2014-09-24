package org.simtech.bootware.eclipse;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;

public final class Util {

	private Util() {}

	/**
	 * Finds a console by the given name.
	 * <p>
	 * A console by the given name is created if it doesn't exist already.
	 *
	 * @param name The name of the requested console.
	 *
	 * @return The requested console.
	 */
	public static MessageConsole findConsole(final String name) {

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
	 * Loads an object from the given type from an XML file.
	 *
	 * @param type The class of the object that should be loaded.
	 * @param path The path to the XML file that should be loaded.
	 *
	 * @return The object of the give ntype loaded from the given XML file.
	 */
	public static <T> T loadXML(final Class<T> type, final String path) throws JAXBException {
		final JAXBContext jaxbContext = JAXBContext.newInstance(type);
		final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		final File file = new File(path);
		final JAXBElement root = unmarshaller.unmarshal(new StreamSource(file), type);

		return type.cast(root.getValue());
	}

}
