package org.simtech.bootware.plugins.provision.opentosca;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.lego4tosca.opentosca.OpenTOSCAInstanceDataAccess;

import org.simtech.bootware.core.ConfigurationWrapper;
import org.simtech.bootware.core.plugins.AbstractBasePlugin;
import org.simtech.bootware.core.plugins.ProvisionPlugin;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class OpenTosca extends AbstractBasePlugin implements ProvisionPlugin {

	public OpenTosca() {}

	public final void initialize(final Map<String, ConfigurationWrapper> configurationList) {
		// no op
	}

	public final void shutdown() {
		// no op
	}

	private String getProperty(final Document properties, final String name) {
		final Node element = properties.getElementsByTagName(name).item(0);
		if (element == null) {
			return null;
		}
		return element.getTextContent();
	}

	public final Map<String, String> provision(final String provisioningEngineEndpoint, final String servicePackageReference) {
		System.out.println("Provision OpenTOSCA");
		System.out.println("Provision engine endpoint: " + provisioningEngineEndpoint);
		System.out.println("Service package reference: " + servicePackageReference);

		final String endpoint = provisioningEngineEndpoint.replace("http://", "");
		final String csarPath = servicePackageReference;
		final String csarName = new File(csarPath).getName();

		final OpenTOSCAInstanceDataAccess client = new OpenTOSCAInstanceDataAccess(endpoint);

		if (client == null) {
			System.out.println("fail here");
		}

		// upload csar
		//final String uploadResponse = client.uploadCSARDueURL(csarPath);
		//System.out.println("upload response: " + uploadResponse);

		// instantiate csar
		//final String instantiateResponse = client.instanitateCSAR(csarName);
		//System.out.println("instantiation: " + instantiateResponse);

		final String instantiateResponse = "some/1";

		// get properties
		final String id = instantiateResponse.substring(instantiateResponse.lastIndexOf("/") + 1);
		final URI propertiesURI = URI.create("http://" + endpoint + ":1337/containerapi/instancedata/nodeInstances/" + id);

		System.out.println(propertiesURI.toString());

		final Document properties = client.getProperties(propertiesURI);

		final String odePort = getProperty(properties, "odePort");
		final String odeUrl = getProperty(properties, "odeUrl");
		final String activeMQPort = getProperty(properties, "activeMQPort");
		final String fragmentoPort = getProperty(properties, "fragmentoPort");
		final String fragmentoServiceUrl = getProperty(properties, "fragmentoServiceUrl");

		final String installedOnNodeInstanceID = getProperty(properties, "installedOnNodeInstanceID");
		final String id2 = installedOnNodeInstanceID.substring(installedOnNodeInstanceID.lastIndexOf("/") + 1);
		final URI propertiesURI2 = URI.create("http://" + endpoint + ":1337/containerapi/instancedata/nodeInstances/" + id2);

		final Document properties2 = client.getProperties(propertiesURI2);

		final String ipaddress = getProperty(properties, "ipaddress");

		// build response
		final String odeServerUrl = "http://" + ipaddress + ":" + odePort + "/" + odeUrl;
		final String activeMQUrl = "http://" + ipaddress + ":" + activeMQPort;
		final String fragmentoUrl = "http://" + ipaddress + ":" + fragmentoPort + "/" + fragmentoServiceUrl;

		final Map<String, String> response = new HashMap<String, String>();

		response.put("odeServerUrl", odeServerUrl);
		response.put("activeMQUrl", activeMQUrl);
		response.put("fragmentoUrl", fragmentoUrl);

		return response;
	}

	public final void deprovision(final String provisioningEngineEndpoint, final String servicePackageReference) {
		System.out.println("Deprovision OpenTOSCA");
		System.out.println("Provision engine endpoint: " + provisioningEngineEndpoint);
		System.out.println("Service package reference: " + servicePackageReference);
	}

}
