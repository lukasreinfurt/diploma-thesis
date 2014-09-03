package org.simtech.bootware.plugins.provision.opentosca;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.lego4tosca.opentosca.OpenTOSCAInstanceDataAccess;

import org.simtech.bootware.core.ConfigurationWrapper;
import org.simtech.bootware.core.exceptions.DeprovisionException;
import org.simtech.bootware.core.exceptions.InitializeException;
import org.simtech.bootware.core.exceptions.ProvisionException;
import org.simtech.bootware.core.plugins.AbstractBasePlugin;
import org.simtech.bootware.core.plugins.ProvisionPlugin;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * A provision plugin that can call OpenTOSCA to provision and deprovision.
 */
public class OpenTosca extends AbstractBasePlugin implements ProvisionPlugin {

	public OpenTosca() {}

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
	 * Gets a property from an XML properties document by its name.
	 *
	 * @param properties The XML properties document.
	 * @param name The name of the property that should be returned.
	 *
	 * @return The property as String if it exists, otherwise null;
	 */
	private String getProperty(final Document properties, final String name) {
		final Node element = properties.getElementsByTagName(name).item(0);
		if (element == null) {
			return null;
		}
		return element.getTextContent();
	}

	/**
	 * Implements the provision operation defined in @see org.simtech.bootware.core.plugins.ProvisionPlugin
	 */
	public final Map<String, String> provision(final String provisioningEngineEndpoint, final String servicePackageReference) throws ProvisionException {
		System.out.println("Provision OpenTOSCA");
		System.out.println("Provision engine endpoint: " + provisioningEngineEndpoint);
		System.out.println("Service package reference: " + servicePackageReference);

		//final String endpoint = provisioningEngineEndpoint.replace("http://", "");
		final String endpoint = "192.168.80.80";
		final String csarPath = servicePackageReference;
		final String csarName = new File(csarPath).getName();
		final Map<String, String> response = new HashMap<String, String>();

		final OpenTOSCAInstanceDataAccess client = new OpenTOSCAInstanceDataAccess(endpoint);

		if (client == null) {
			throw new ProvisionException("Client was null.");
		}

		// upload csar
		final String uploadResponse = client.uploadCSARDueURL(csarPath);
		System.out.println("upload response: " + uploadResponse);

		try {
			final Integer wait = 30000;
			Thread.sleep(wait);
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		// instantiate csar
		final String instantiateResponse = client.instanitateCSAR(csarName);
		System.out.println("instantiation: " + instantiateResponse);

		if (instantiateResponse != null) {

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

			response.put("odeServerUrl", odeServerUrl);
			response.put("activeMQUrl", activeMQUrl);
			response.put("fragmentoUrl", fragmentoUrl);
		}
		else {
			throw new ProvisionException("Instantiate response was null.");
		}

		return response;
	}

	/**
	 * Implements the deprovision operation defined in @see org.simtech.bootware.core.plugins.ProvisionPlugin
	 */
	public final void deprovision(final String provisioningEngineEndpoint, final String servicePackageReference) throws DeprovisionException {
		System.out.println("Deprovision OpenTOSCA");
		System.out.println("Provision engine endpoint: " + provisioningEngineEndpoint);
		System.out.println("Service package reference: " + servicePackageReference);
	}

}
