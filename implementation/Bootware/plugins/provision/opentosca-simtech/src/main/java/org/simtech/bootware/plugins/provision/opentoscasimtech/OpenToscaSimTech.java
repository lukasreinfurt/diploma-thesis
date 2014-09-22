package org.simtech.bootware.plugins.provision.opentoscasimtech;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.lego4tosca.opentosca.OpenTOSCAInstanceDataAccess;
import org.lego4tosca.opentosca.OpenTOSCAInstanceDataAccess.PlanResponse;

import org.simtech.bootware.core.ApplicationInstance;
import org.simtech.bootware.core.ConfigurationWrapper;
import org.simtech.bootware.core.events.ProvisionPluginEvent;
import org.simtech.bootware.core.events.Severity;
import org.simtech.bootware.core.exceptions.DeprovisionException;
import org.simtech.bootware.core.exceptions.InitializeException;
import org.simtech.bootware.core.exceptions.ProvisionException;
import org.simtech.bootware.core.plugins.AbstractBasePlugin;
import org.simtech.bootware.core.plugins.ProvisionPlugin;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * A provision plugin that can call OpenTOSCA to provision and deprovision the SimTech System.
 */
public class OpenToscaSimTech extends AbstractBasePlugin implements ProvisionPlugin {

	public OpenToscaSimTech() {}

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
	@SuppressWarnings({
			"checkstyle:javancss",
			"checkstyle:executablestatementcount"})
	public final Map<String, String> provision(final ApplicationInstance instance) throws ProvisionException {

		final String provisioningEngineEndpoint = instance.getInstanceInformation().get("appURL");
		final String servicePackageReference = instance.getUserContext().getServicePackageReference();

		eventBus.publish(new ProvisionPluginEvent(Severity.INFO, "Provisioning " + servicePackageReference + " with OpenTOSCA at " + provisioningEngineEndpoint));

		final String endpoint = provisioningEngineEndpoint.replace("http://", "");
		final String csarName = new File(servicePackageReference).getName();
		final Map<String, String> response = new HashMap<String, String>();

		final OpenTOSCAInstanceDataAccess client = new OpenTOSCAInstanceDataAccess(endpoint);

		if (client == null) {
			throw new ProvisionException("Client was null.");
		}

		// upload csar
		// In the future there should be a check here if the upload/deployment of
		// the csar was successful.
		eventBus.publish(new ProvisionPluginEvent(Severity.INFO, "Uploading " + csarName));
		final String uploadResponse = client.uploadCSARDueURL(servicePackageReference);

		// wait a bit so that the csar is deployed
		// There should be a better way to do this.
		try {
			final Integer msInS = 1000;
			final Integer wait = 60000;
			eventBus.publish(new ProvisionPluginEvent(Severity.INFO, "Waiting " + wait / msInS + " seconds for " + csarName + " to be deployed."));
			Thread.sleep(wait);
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		// instantiate csar
		// There should probably be a check here if the instantiation was successful.
		eventBus.publish(new ProvisionPluginEvent(Severity.INFO, "Instantiate " + csarName));
		final PlanResponse provisionResponse = client.provisionService(csarName);
		final String serviceInstanceID = provisionResponse.getServiceInstanceID();
		System.out.println("serviceInstanceID:" + serviceInstanceID);

		if (serviceInstanceID != null) {

			// get properties
			final Document properties = client.getProperties(URI.create(serviceInstanceID), "SimTech Node Template");

			// get some values from properties
			final String odePort = getProperty(properties, "odePort");
			final String odeUrl = getProperty(properties, "odeUrl");
			final String activeMQPort = getProperty(properties, "activeMQPort");
			final String fragmentoPort = getProperty(properties, "fragmentoPort");
			final String fragmentoServiceUrl = getProperty(properties, "fragmentoServiceUrl");

			// get some other properties
			final String installedOnNodeInstanceID = getProperty(properties, "installedOnNodeInstanceID");
			final String id2 = installedOnNodeInstanceID.substring(installedOnNodeInstanceID.lastIndexOf("/") + 1);
			final URI nodeInstancesID = URI.create("http://" + endpoint + ":1337/containerapi/instancedata/nodeInstances/" + id2);
			final Document properties2 = client.getProperties(nodeInstancesID);

			// get IP address from other properties
			final String ipaddress = getProperty(properties2, "publicDNS");

			// build response
			final String odeServerUrl = "http://" + ipaddress + ":" + odePort + "/" + odeUrl;
			final String activeMQUrl = "tcp://" + ipaddress + ":" + activeMQPort;
			final String fragmentoUrl = "http://" + ipaddress + ":" + fragmentoPort + "/" + fragmentoServiceUrl;

			eventBus.publish(new ProvisionPluginEvent(Severity.INFO, "ODE Server URL is " + odeServerUrl));
			eventBus.publish(new ProvisionPluginEvent(Severity.INFO, "ActiveMQ Broker URL is " + activeMQUrl));
			eventBus.publish(new ProvisionPluginEvent(Severity.INFO, "Fragmento URL is " + fragmentoUrl));

			response.put("serviceInstanceID", serviceInstanceID);
			response.put("odeServerUrl", odeServerUrl);
			response.put("activeMQUrl", activeMQUrl);
			response.put("fragmentoUrl", fragmentoUrl);

			// Begin: Setup provisioning manager.
			// This should be done with management plans in the future.

			final String sshUsername = getProperty(properties2, "username");
			final String sshKey = getProperty(properties2, "identityFile");

			final Map<String, String> sshSettings = new HashMap<String, String>();
			sshSettings.put("resourceURL", ipaddress);
			sshSettings.put("sshUsername", sshUsername);
			sshSettings.put("sshKey", sshKey);
			final SetupProvisioningManager setup = new SetupProvisioningManager();
			setup.execute(eventBus, sshSettings, instance);

			// End: Setup provisioning manager

		}
		else {
			throw new ProvisionException("Instantiate response was null.");
		}

		return response;
	}

	/**
	 * Implements the deprovision operation defined in @see org.simtech.bootware.core.plugins.ProvisionPlugin
	 */
	public final void deprovision(final ApplicationInstance instance) throws DeprovisionException {

		final String provisioningEngineEndpoint = instance.getInstanceInformation().get("appURL");
		final String servicePackageReference = instance.getUserContext().getServicePackageReference();
		final String serviceInstanceID = instance.getInstanceInformation().get("serviceInstanceID");

		eventBus.publish(new ProvisionPluginEvent(Severity.INFO, "Deprovisioning " + servicePackageReference + " with OpenTOSCA at " + provisioningEngineEndpoint));

		final String endpoint = provisioningEngineEndpoint.replace("http://", "");
		final String csarName = new File(servicePackageReference).getName();

		final OpenTOSCAInstanceDataAccess client = new OpenTOSCAInstanceDataAccess(endpoint);

		if (client == null) {
			throw new DeprovisionException("Client was null.");
		}

		final PlanResponse deprovisionResponse = client.deprovisionService(csarName, serviceInstanceID);

	}

}
