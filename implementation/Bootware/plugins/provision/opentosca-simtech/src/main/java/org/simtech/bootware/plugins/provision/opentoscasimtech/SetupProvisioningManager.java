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
		System.out.println("##################################################################################");

		final SshConnection connection = new SshConnection(eb);

		try {
			connection.connect(sshSettings);

			// Set Remote Bootware URL
			// sed -r -i 's/http.*Repository/Test/g;' /tmp/blueprint.xml
			// sed -r -i 's/http.*Bootware\?wsdl/Test/g;' /tmp/blueprint.xml
			// sed -i 's,http://.*/Bootware?wsdl,http://www.replace.com/page,g' /tmp/blueprint.xml
			final String remoteBootwareIP = instance.getInstanceInformation().get("remoteBootwareIP");
			final String remoteBootwareUrl = "http://" + remoteBootwareIP + ":8080/axis2/services/Bootware?wsdl";
			connection.execute("sed -i 's,http://.*/Bootware?wsdl," + remoteBootwareUrl + ",g' /tmp/blueprint.xml");

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
			connection.upload(new FileInputStream(file), "/tmp/provisioningContext.xml");

			connection.disconnect();
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}

		System.out.println("##################################################################################");
	}
}
