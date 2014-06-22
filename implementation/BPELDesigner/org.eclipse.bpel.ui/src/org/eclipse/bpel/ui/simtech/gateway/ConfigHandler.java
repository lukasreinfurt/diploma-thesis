package org.eclipse.bpel.ui.simtech.gateway;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

/**
 * @author hahnml
 *
 */
public class ConfigHandler {

	// PRIVATE
	private final File fFile;
	private Properties property;
	
	public static final String PROPERTY_NAME = "name";
	public static final String PROPERTY_DESCRIPTION = "description";
	public static final String PROPERTY_AUTHOR = "author";
	public static final String PROPERTY_ICON = "icon";

	/**
	 * Constructor.
	 * 
	 * @param aFileName
	 *            full name of an existing, readable file.
	 */
	public ConfigHandler(File file) {
		fFile = file;
		property = new Properties();
	}

	public String getProperty(String name) {
		String value = "";

		value = this.property.getProperty(name, "");

		return value;
	}

	public void loadPropertiesFromXMLFile() {
		if (this.fFile.exists() && this.fFile.getName().equals("config.xml")) {
			try {
				this.property.loadFromXML(new BufferedInputStream(
						new FileInputStream(fFile)));
			} catch (InvalidPropertiesFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
