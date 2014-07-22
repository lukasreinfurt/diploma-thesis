package org.simtech.bootware.plugins.application.test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

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

public class Test extends AbstractBasePlugin implements ApplicationPlugin {

	public Test() {}

	public final void initialize(final ConfigurationWrapper configuration) {
		// no op
	}

	public final void shutdown() {
		// no op
	}

	public final void provision(final Connection connection) throws ProvisionApplicationException {
		final String remotePathPrefix = "/tmp/";

		if (connection != null) {
			try {
				connection.execute("java -version");
				connection.execute("ls /tmp");

				final Collection<File> files =  FileUtils.listFilesAndDirs(new File("remote/"), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
				for (File file : files) {
					if (file.isFile()) {
						final File remotePath = new File(remotePathPrefix, file.getParent());
						connection.upload(file.toString(), remotePath.toString());
					}
					else if (file.isDirectory()) {
						final File remotePath = new File(remotePathPrefix, file.toString());
						connection.execute("mkdir -p " + remotePath.toString());
					}
				}

				connection.execute("ls -al /tmp");
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

	public final void deprovision(final Connection connection) {
		// no op
	}

	public final URL start(final Connection connection) throws StartApplicationException {
		try {
			final URL url = new URL("http://localhost:8080/axis2/services/Bootware?wsdl");
			return url;
		}
		catch (MalformedURLException e) {
			throw new StartApplicationException(e);
		}
	}

	public final void stop(final Connection connection) {
		// no op
	}

}
