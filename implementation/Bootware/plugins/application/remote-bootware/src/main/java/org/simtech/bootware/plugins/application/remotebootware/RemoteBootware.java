package org.simtech.bootware.plugins.application.remotebootware;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import org.simtech.bootware.core.ConfigurationWrapper;
import org.simtech.bootware.core.Connection;
// import org.simtech.bootware.core.events.ApplicationPluginEvent;
// import org.simtech.bootware.core.events.Severity;
import org.simtech.bootware.core.exceptions.ExecuteCommandException;
import org.simtech.bootware.core.exceptions.ProvisionApplicationException;
import org.simtech.bootware.core.exceptions.StartApplicationException;
import org.simtech.bootware.core.exceptions.UploadFileException;
import org.simtech.bootware.core.plugins.AbstractBasePlugin;
import org.simtech.bootware.core.plugins.ApplicationPlugin;

/**
 * An application plugin that provisions the remote bootware
 */
public class RemoteBootware extends AbstractBasePlugin implements ApplicationPlugin {

	public RemoteBootware() {}

	/**
	 * Implements the initialize operation defined in @see org.simtech.bootware.core.plugins.Plugin
	 */
	public final void initialize(final Map<String, ConfigurationWrapper> configurationList) {
		// no op
	}

	/**
	 * Implements the shutdown operation defined in @see org.simtech.bootware.core.plugins.Plugin
	 */
	public final void shutdown() {
		// no op
	}

	/**
	 * Implements the provision operation defined in @see org.simtech.bootware.core.plugins.ApplicationPlugin
	 */
	public final void provision(final Connection connection) throws ProvisionApplicationException {
		final String remotePathPrefix = "/tmp/";

		if (connection != null) {
			// Upload the content of the remote folder to /tmp/remote on the resource.
			try {
				final Collection<File> files =  FileUtils.listFilesAndDirs(new File("remote/"), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
				for (File file : files) {
					final String remotePath = new File(remotePathPrefix, file.toString()).toString().replace("\\", "/");
					// If file, upload file
					if (file.isFile()) {
						final FileInputStream is = new FileInputStream(file);
						connection.upload(is, file.length(), remotePath);
					}
					// If directory, create directory
					else if (file.isDirectory()) {
						connection.execute("mkdir -p " + remotePath);
					}
				}
			}
			catch (IllegalArgumentException e) {
				throw new ProvisionApplicationException(e);
			}
			catch (FileNotFoundException e) {
				throw new ProvisionApplicationException(e);
			}
			catch (ExecuteCommandException e) {
				throw new ProvisionApplicationException(e);
			}
			catch (UploadFileException e) {
				throw new ProvisionApplicationException(e);
			}
		}
		else {
			throw new ProvisionApplicationException("Connection is null.");
		}
	}

	/**
	 * Implements the deprovision operation defined in @see org.simtech.bootware.core.plugins.ApplicationPlugin
	 */
	public final void deprovision(final Connection connection) {
		// no op
	}

	/**
	 * Implements the start operation defined in @see org.simtech.bootware.core.plugins.ApplicationPlugin
	 */
	public final URL start(final Connection connection) throws StartApplicationException {

		if (connection != null) {
			try {
				// Execute the remote bootware jar in the background and pipe output to /dev/null
				connection.execute("cd /tmp/remote; nohup java -jar bootware-remote-1.0.0.jar &> /dev/null &");

				final URL url = new URL("http://" + connection.getURL() + ":8080/axis2/services/Bootware");
				return url;
			}
			catch (ExecuteCommandException e) {
				throw new StartApplicationException(e);
			}
			catch (MalformedURLException e) {
				throw new StartApplicationException(e);
			}
		}
		else {
			throw new StartApplicationException("Connection null.");
		}
	}

	/**
	 * Implements the stop operation defined in @see org.simtech.bootware.core.plugins.ApplicationPlugin
	 */
	public final void stop(final Connection connection) {
		// no op
	}

}
