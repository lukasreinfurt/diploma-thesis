package org.simtech.bootware.plugins.application.opentosca;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

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

public class OpenTosca extends AbstractBasePlugin implements ApplicationPlugin {

	public OpenTosca() {}

	public final void initialize(final Map<String, ConfigurationWrapper> configurationList) {
		// no op
	}

	public final void shutdown() {
		// no op
	}

	private long getSize(final InputStream is) {
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		final int bufferSize = 4096;

		byte[] buffer = new byte[bufferSize];
		int n;

		try {
			while ((n = is.read(buffer)) > 0) {
				bos.write(buffer, 0, n);
				buffer = bos.toByteArray();
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		return buffer.length;
	}

	public final void provision(final Connection connection) throws ProvisionApplicationException {

		if (connection != null) {
			try {
				// regular installation
				connection.execute("sudo apt-get update &> /tmp/install.log");
				connection.execute("curl install.opentosca.de/installEC2|sh &> /tmp/install.log");

				System.out.println("Waiting...");
				final Integer wait = 120000;
				Thread.sleep(wait);

				// bug fix start

				// create properties file with the actual ip address
				final Properties properties = new Properties();
				properties.setProperty("containerapi.instancedata.epr", "http://" + connection.getURL() + ":1337/containerapi/instancedata");
				properties.setProperty("containerapi.portability.epr", "http://" + connection.getURL() + ":1337/containerapi/portability");

				// store properties in inputstream
				final ByteArrayOutputStream output = new ByteArrayOutputStream();
				properties.store(output, null);
				final ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
				final ByteArrayInputStream input2 = new ByteArrayInputStream(output.toByteArray());

				// upload properties
				connection.upload(input, getSize(input2), "/tmp/opentosca.properties");
				connection.execute("sudo cp /tmp/opentosca.properties /etc/tomcat7/opentosca.properties");

				// upload replacement jar
				final String jarName = "org.opentosca.siengine.service.impl_1.0.0.201407231442.jar";
				final InputStream is1 = OpenTosca.class.getResourceAsStream("/bugfix/" + jarName);
				final InputStream is2 = OpenTosca.class.getResourceAsStream("/bugfix/" + jarName);
				connection.upload(is1, getSize(is2), "/tmp/" + jarName);
				connection.execute("sudo rm ~/OpenTOSCA/lib/org.opentosca.siengine.service.impl_1.0.0.201311221533.jar");
				connection.execute("sudo cp /tmp/" + jarName + " ~/OpenTOSCA/lib/" + jarName);

				// restart opentosca
				connection.execute("sudo service tomcat7 stop");
				connection.execute("pkill -f \"java\"");
				connection.execute("sudo service tomcat7 start");

				System.out.println("Waiting...");
				Thread.sleep(wait);

				connection.execute("sudo wget -qO- http://install.opentosca.de/start | sh");

				System.out.println("Waiting...");
				Thread.sleep(wait);

				// bug fix end
			}
			catch (IOException e) {
				throw new ProvisionApplicationException(e);
			}
			catch (IllegalArgumentException e) {
				throw new ProvisionApplicationException(e);
			}
			catch (ExecuteCommandException e) {
				throw new ProvisionApplicationException(e);
			}
			catch (UploadFileException e) {
				throw new ProvisionApplicationException(e);
			}
			catch (InterruptedException e) {
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

		if (connection != null) {
			try {
				final URL url = new URL("http://" + connection.getURL());
				return url;
			}
			catch (MalformedURLException e) {
				throw new StartApplicationException(e);
			}
		}
		else {
			throw new StartApplicationException("Connection null.");
		}
	}

	public final void stop(final Connection connection) {
		// no op
	}

}
