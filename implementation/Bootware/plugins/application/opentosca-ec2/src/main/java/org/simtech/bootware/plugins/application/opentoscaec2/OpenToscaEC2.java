package org.simtech.bootware.plugins.application.opentoscaec2;

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
import org.simtech.bootware.core.events.ApplicationPluginEvent;
import org.simtech.bootware.core.events.Severity;
import org.simtech.bootware.core.exceptions.DeprovisionApplicationException;
import org.simtech.bootware.core.exceptions.ExecuteCommandException;
import org.simtech.bootware.core.exceptions.InitializeException;
import org.simtech.bootware.core.exceptions.ProvisionApplicationException;
import org.simtech.bootware.core.exceptions.StartApplicationException;
import org.simtech.bootware.core.exceptions.StopApplicationException;
import org.simtech.bootware.core.exceptions.UploadFileException;
import org.simtech.bootware.core.plugins.AbstractBasePlugin;
import org.simtech.bootware.core.plugins.ApplicationPlugin;

/**
 * An application plugin that provisions OpenTOSCA on AWS EC2
 */
public class OpenToscaEC2 extends AbstractBasePlugin implements ApplicationPlugin {

	public OpenToscaEC2() {}

	/**
	 * Implements the initialize operation defined in @see org.simtech.bootware.core.plugins.Plugin
	 */
	public final void initialize(final Map<String, ConfigurationWrapper> configurationList) throws InitializeException {
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

		if (connection != null) {
			try {
				// Bugfix: The apt-get update in the install script seems to fail for
				// some reason. Executing apt-get update before the install script
				// is a fix for that.
				// Update apt-get. Retry 5 times if it fails.
				eventBus.publish(new ApplicationPluginEvent(Severity.INFO, "Updating apt-get. This can take a while."));
				connection.execute("trial=0; until sudo apt-get update -y || [ $trial -e q 5 ]; do sleep $((2**++trial )); done >> /tmp/install.log 2>&1");

				// run regular installation
				eventBus.publish(new ApplicationPluginEvent(Severity.INFO, "Executing OpenTOSCA install script. This can take a while."));
				connection.execute("wget -qO- http://install.opentosca.org/installEC2 | sh >> /tmp/install.log 2>&1");

				// create properties file with the actual ip address
				final Properties properties = new Properties();
				properties.setProperty("containerapi.instancedata.epr", "http://" + connection.getURL() + ":1337/containerapi/instancedata");
				properties.setProperty("containerapi.portability.epr", "http://" + connection.getURL() + ":1337/containerapi/portability");

				// store properties in inputstream
				final ByteArrayOutputStream output = new ByteArrayOutputStream();
				properties.store(output, null);
				final ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());

				// upload properties
				connection.upload(input, "/tmp/opentosca.properties");
				connection.execute("sudo cp /tmp/opentosca.properties /etc/tomcat7/opentosca.properties");

				output.close();
				input.close();

				// Deploy monitor server
				// TODO: Host monitor server .war somewhere "real" and change the URL here.
				connection.execute("wget -P /var/lib/tomcat7/webapps/ https://dl.dropboxusercontent.com/u/2010067/monitor-server-0.1.war");

				// Bugfix for termination plans

				// delete old vinothek.war and upload replacement
				connection.execute("sudo rm -rf /var/lib/tomcat7/webapps/vinothek.war");
				// wait for undeployment
				final Integer wait2 = 10000;
				Thread.sleep(wait2);
				final String fileName = "vinothek.war";
				final InputStream is = OpenToscaEC2.class.getResourceAsStream("/bugfix/" + fileName);
				connection.upload(is, "/tmp/" + fileName);
				connection.execute("sudo cp /tmp/" + fileName + " /var/lib/tomcat7/webapps/" + fileName);

				is.close();

				// Bugfix end

				// wait a bit
				// When the install script above is finished it still takes about two
				// minutes for WSO to start up. If a CSAR is uploaded before that's done
				// it will not be deployed correctly and instantiation will fail.
				// Waiting for everything to be started should be done in a cleaner way
				// in the future.
				final Integer msInS = 1000;
				final Integer wait = 180000;
				eventBus.publish(new ApplicationPluginEvent(Severity.INFO, "Waiting " + wait / msInS + " seconds for OpenTOSCA to be started."));
				Thread.sleep(wait);
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
			throw new ProvisionApplicationException("Connection was null.");
		}
	}

	/**
	 * Implements the deprovision operation defined in @see org.simtech.bootware.core.plugins.ApplicationPlugin
	 */
	public final void deprovision(final Connection connection) throws DeprovisionApplicationException {
		// no op
	}

	/**
	 * Implements the start operation defined in @see org.simtech.bootware.core.plugins.ApplicationPlugin
	 */
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
			throw new StartApplicationException("Connection was null.");
		}
	}

	/**
	 * Implements the stop operation defined in @see org.simtech.bootware.core.plugins.ApplicationPlugin
	 */
	public final void stop(final Connection connection) throws StopApplicationException {
		// no op
	}

}
