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

@SuppressWarnings("checkstyle:illegalcatch")
public class SetupProvisioningManager {

	public SetupProvisioningManager() {}

	public final void execute(final EventBus eb, final Map<String, String> sshSettings, final ApplicationInstance instance) {

		// The path to the service mix installation (i.e. where the bin, data, etc ... folders are)
		final String serviceMixPath = "/opt/apache-servicemix";
		final SshConnection connection = new SshConnection(eb);

		try {
			connection.connect(sshSettings);

			// Copy provision context
			// Marshall to XML
			final UserContext context = instance.getUserContext();
			final JAXBContext jaxbContext = JAXBContext.newInstance(UserContext.class);
			final Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			final JAXBElement root = new JAXBElement(new QName("context"), context.getClass(), context);
			final File file = new File("provisioningContext.xml");
			marshaller.marshal(root, file);
			// Upload
			final String provisioningContextPath = "/tmp/provisioningContext.xml";
			connection.upload(new FileInputStream(file), provisioningContextPath);
			// Set provisioning context location
			connection.execute("sudo sed -i 's,^provisioningcontext.location.*,provisioningcontext.location=" + provisioningContextPath + ",g' " + serviceMixPath + "/etc/provisioningmanager.properties");

			// Set Remote Bootware URL
			final String remoteBootwareIP = instance.getInstanceInformation().get("remoteBootwareIP");
			final String remoteBootwareUrl = "http://" + remoteBootwareIP + ":8080/axis2/services/Bootware?wsdl";
			connection.execute("sudo sed -i 's,^bwr.endpoint.*,bwr.endpoint=" + remoteBootwareUrl + ",g' " + serviceMixPath + "/etc/provisioningmanager.properties");

			// Set service repository URL
			final String repositoryEndpoint = "http://localhost:8077/ServiceRepositoryMockup/services/ServiceRepository";
			connection.execute("sudo sed -i 's,^repository.endpoint.*,repository.endpoint=" + repositoryEndpoint + ",g' " + serviceMixPath + "/etc/provisioningmanager.properties");

			// restart org.simtech.pmbundle
			// save the resulting command to be passed on later
			connection.execute("command=$("
				// ssh into servicemix and return the entry for org.simtech.pmbundle from the list of bundles
				+ "sshpass -p smx ssh -p 8101 smx@localhost 'list | grep org.simtech.pmbundle'"
				// from this entry, extract the bundle id and build the command string 'restart [id]'
				+ "| sudo sed -nr 's/^\\[ *([0-9]+)\\].*/restart \\1/p'"
				+ ");"
				// ssh into service mix again and execute the command string
				+ "sshpass -p smx ssh -p 8101 smx@localhost $command");

			connection.disconnect();
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}
}
