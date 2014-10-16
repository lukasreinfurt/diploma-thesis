package org.simtech.bootware.plugins.provision.opentoscasimtech;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import org.simtech.bootware.core.ApplicationInstance;
import org.simtech.bootware.core.EventBus;
import org.simtech.bootware.core.UserContext;

/**
 * This is a helper class for setting up the provisioning manager after it has
 * been installed with the SimTech CSAR.
 * It sets up the connections to the remote bootware and the service repository
 * and service registry. It also copies the provisioning context.
 * In the Future, all this should be done with a TOSCA management plan.
 * Then, this file, as well as SshConnection.java should be removed from this
 * plugin.
 */
@SuppressWarnings("checkstyle:illegalcatch")
public class SetupProvisioningManager {

	public SetupProvisioningManager() {}

	public final void execute(final EventBus eb,
	                          final Map<String, String> sshSettings,
	                          final ApplicationInstance instance,
	                          final String serviceRegistryURL,
	                          final String serviceRepositoryURL) {

		// The path to the service mix installation (i.e. where the bin, data, etc ... folders are)
		final String serviceMixPath = "/opt/servicemix";
		final SshConnection connection = new SshConnection(eb);

		try {
			connection.connect(sshSettings);

			// Update apt-get. Retry 5 times if it fails.
			connection.execute("trial=0; until sudo apt-get update -y || [ $trial -e q 5 ]; do sleep $((2**++trial )); done >> /tmp/install.log 2>&1");

			// Install unzip
			connection.execute("sudo apt-get -y install unzip >> /tmp/install.log 2>&1");

			// Install zip
			connection.execute("sudo apt-get -y install zip >> /tmp/install.log 2>&1");

			// Change service registry URL
			// Since the service registry URL is set in xbeam.xml inside ServiceRegistryMockup-http-su.zip
			// in ServiceRegistryMockup-SA.zip, which is already deployed, we have to
			// unpack those zip files, change the value, and rezip them.
			// Service Mix will automatically update the deployed bundle as soon as
			// the archive has changed.

			// Unzip ServiceRegistryMockup-SA.zip to temporary location
			connection.execute("sudo mkdir -p /tmp/unzip/1");
			connection.execute("sudo unzip -o " + serviceMixPath + "/deploy/ServiceRegistryMockup-SA.zip -d /tmp/unzip/1");
			// unzip inner archive ServiceRegistryMockup-http-su.zip to temporary location
			connection.execute("sudo mkdir -p /tmp/unzip/2");
			connection.execute("sudo unzip -o /tmp/unzip/1/ServiceRegistryMockup-http-su.zip -d /tmp/unzip/2");
			// Change service registry URL in xbean.xml
			connection.execute("sudo sed -i 's,locationURI=\".*\",locationURI=\"" + serviceRegistryURL + "\",g' /tmp/unzip/2/xbean.xml");
			// zip inner archive
			connection.execute("cd /tmp/unzip/2; sudo zip -r /tmp/unzip/1/ServiceRegistryMockup-http-su.zip .");
			// zip outer archive
			connection.execute("cd /tmp/unzip/1; sudo zip -r " + serviceMixPath + "/deploy/ServiceRegistryMockup-SA.zip .");

			// Copy provision context
			// Marshall to XML
			final UserContext context = instance.getUserContext();
			final JAXBContext jaxbContext = JAXBContext.newInstance(UserContext.class);
			final Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			// Add root element
			final JAXBElement root = new JAXBElement(new QName("context"), context.getClass(), context);
			final File file = new File("provisioningContext.xml");
			marshaller.marshal(root, file);
			// Upload
			final String provisioningContextPath = "/tmp/provisioningContext.xml";
			connection.upload(new FileInputStream(file), provisioningContextPath);

			// Set provisioning context location in ondemandprovisioning.properties
			connection.execute("sudo sed -i 's,^provisioningcontext.location.*,provisioningcontext.location=" + provisioningContextPath + ",g' " + serviceMixPath + "/etc/ondemandprovisioning.properties");

			// Set remote bootware URL in ondemandprovisioning.properties
			final String remoteBootwareIP = instance.getInstanceInformation().get("remoteBootwareIP");
			final String remoteBootwareUrl = "http://" + remoteBootwareIP + ":8080/axis2/services/Bootware?wsdl";
			connection.execute("sudo sed -i 's,^bwr.endpoint.*,bwr.endpoint=" + remoteBootwareUrl + ",g' " + serviceMixPath + "/etc/ondemandprovisioning.properties");

			// Set service repository URL in ondemandprovisioning.properties
			connection.execute("sudo sed -i 's,^repository.endpoint.*,repository.endpoint=" + serviceRepositoryURL + ",g' " + serviceMixPath + "/etc/ondemandprovisioning.properties");

			// Set dynamic binding ESB IP in ondemandprovisioning.properties
			connection.execute("sudo sed -i 's,^dynamicbinding.esbip.*,dynamicbinding.esbip=" + sshSettings.get("resourceURL") + ",g' " + serviceMixPath + "/etc/ondemandprovisioning.properties");

			// Restart ServiceMix service so that the changed properties take effect.
			connection.execute("sudo /etc/init.d/karaf-service restart");

			// Wait a bit so that ServiceMix has enough time to start everything.
			final Integer wait = 30000;
			Thread.sleep(wait);

			connection.disconnect();
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}
}
