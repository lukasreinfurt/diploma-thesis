package org.simtech.bootware.plugins.resource.awsec2;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.configuration.MapConfiguration;

import org.simtech.bootware.core.ConfigurationWrapper;
import org.simtech.bootware.core.events.ResourcePluginEvent;
import org.simtech.bootware.core.events.Severity;
import org.simtech.bootware.core.exceptions.DeprovisionResourceException;
import org.simtech.bootware.core.exceptions.InitializeException;
import org.simtech.bootware.core.exceptions.ProvisionResourceException;
import org.simtech.bootware.core.plugins.AbstractBasePlugin;
import org.simtech.bootware.core.plugins.ResourcePlugin;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.CreateKeyPairRequest;
import com.amazonaws.services.ec2.model.CreateKeyPairResult;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupResult;
import com.amazonaws.services.ec2.model.DeleteKeyPairRequest;
import com.amazonaws.services.ec2.model.DeleteSecurityGroupRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.KeyPair;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesResult;

/**
 * A resource plugin that can create and remove an EC2 instance.
 * <p>
 * It uses the AWS SDK for Java.
 */
@SuppressWarnings({
	"checkstyle:multiplestringliterals",
	"checkstyle:magicnumber",
	"checkstyle:classfanoutcomplexity",
	"checkstyle:classdataabstractioncoupling"
                 })
public class AwsEc2 extends AbstractBasePlugin implements ResourcePlugin {

	private String secretKey;
	private String accessKey;
	private String username;
	private String region;
	private List<Object> ports;
	private String imageId;
	private String instanceType;

	private String securityGroupName;
	private String keyName;

	private AmazonEC2 ec2Client;

	public AwsEc2() {}

	/**
	 * Implements the initialize operation defined in @see org.simtech.bootware.core.plugins.Plugin
	 */
	public final void initialize(final Map<String, ConfigurationWrapper> configurationList) throws InitializeException {

		// Get the aws-ec2 configuration from the configuration list.
		final ConfigurationWrapper configurationWrapper = configurationList.get("aws-ec2");

		if (configurationWrapper == null) {
			throw new InitializeException("Could not find configuration for 'aws-ec2'.");
		}

		// Try to read the configuration values the plugin expects.
		final Map<String, String> configurationMap = configurationWrapper.getConfiguration();
		final MapConfiguration configuration = new MapConfiguration(configurationMap);
		configuration.setThrowExceptionOnMissing(true);

		try {
			secretKey    = configuration.getString("secretKey");
			accessKey    = configuration.getString("accessKey");
			username     = configuration.getString("username");
			region       = configuration.getString("region");
			ports        = configuration.getList("ports");
			imageId      = configuration.getString("imageId");
			instanceType = configuration.getString("instanceType");
		}
		catch (NoSuchElementException e) {
			throw new InitializeException(e);
		}

		// Create unique security group and key names with a timestamp
		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		final String timestamp = dateFormat.format(new Date());
		securityGroupName = "BootwareSecurityGroup_" + timestamp;
		keyName           = "BootwareKey_" + timestamp;

		// Create a client instance
		createClientInstance();
	}

	/**
	 * Implements the shutdown operation defined in @see org.simtech.bootware.core.plugins.Plugin
	 */
	public final void shutdown() {
		// no op
	}

	/**
	 * Implements the provision operation defined in @see org.simtech.bootware.core.plugins.ResourcePlugin
	 */
	public final Map<String, String> provision() throws ProvisionResourceException {
		// Create an EC2 instance.
		createSecurityGroup();
		openPorts();
		final String privateKey = createKeyPair();
		final String instanceID = createEC2Instance();

		// Return some information about the instance.
		final Map<String, String> instanceInformation = getInstanceInformation(instanceID);

		instanceInformation.put("username", username);
		instanceInformation.put("securityGroupName", securityGroupName);
		instanceInformation.put("keyName", keyName);
		instanceInformation.put("privateKey", privateKey);
		instanceInformation.put("instanceID", instanceID);

		return instanceInformation;
	}

	/**
	 * Implements the deprovision operation defined in @see org.simtech.bootware.core.plugins.ResourcePlugin
	 */
	public final void deprovision(final Map<String, String> instanceInformation) throws DeprovisionResourceException {
		// Remove the EC2 instance and the key and security group associated with it.
		terminateEC2Instance(instanceInformation.get("instanceID"));
		deleteKeyPair(instanceInformation.get("keyName"));
		deleteSecurityGroup(instanceInformation.get("securityGroupName"));
	}

	/**
	 * Creates an AmazonEC2Client instance. which is used to make requests to AWS.
	 */
	private void createClientInstance() {
		// load credentials
		final AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

		// create client instance (not an actual ec2 instance)
		ec2Client = new AmazonEC2Client(credentials);

		// set region
		ec2Client.setEndpoint(region);
	}

	/**
	 * Create a security group for the EC2 instance.
	 *
	 * @throws ProvisionResourceException If there was an error when creating the security group.
	 */
	private void createSecurityGroup() throws ProvisionResourceException {

		// Create request.
		final CreateSecurityGroupRequest request = new CreateSecurityGroupRequest();

		request
			.withGroupName(securityGroupName)
			.withDescription("Generated by SimTech Bootware");

		// Send request to AWS.
		try {
			final CreateSecurityGroupResult result = ec2Client.createSecurityGroup(request);
		}
		catch (AmazonServiceException e) {
			throw new ProvisionResourceException(e);
		}

		eventBus.publish(new ResourcePluginEvent(Severity.INFO, "Security group '" + securityGroupName + "' created."));
	}

	/**
	 * Delete the security group of the EC2 instance.
	 *
	 * @param name The name of the security group that should be deleted.
	 *
	 * @throws DeprovisionResourceException If there was an error when deleting the security group.
	 */
	private void deleteSecurityGroup(final String name) throws DeprovisionResourceException {

		// Create request.
		final DeleteSecurityGroupRequest request = new DeleteSecurityGroupRequest();

		request
			.withGroupName(name);

		// Send request.
		// Retry a couple times because it sometimes takes some time for AWS to
		// deregister dependencies between already removed EC2 instances and
		// security groups. Otherwise we might get a dependency violation error.
		final Integer max = 10;
		for (Integer i = 1; i <= max; i++) {
			try {
				final Integer time = 5000;
				Thread.sleep(time);
				ec2Client.deleteSecurityGroup(request);
				break;
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			catch (AmazonServiceException e) {
				if (i == max) {
					throw new DeprovisionResourceException(e);
				}
			}
		}

		eventBus.publish(new ResourcePluginEvent(Severity.INFO, "Security group '" + securityGroupName + "' deleted."));
	}

	/**
	 * Open ports for the EC2 instance.
	 *
	 * @throws ProvisionResourceException If there was an error while opening the ports.
	 */
	private void openPorts() throws ProvisionResourceException {

		// Create list of ports that should be opened from configuration value.
		final List<IpPermission> ipPermissions = new ArrayList<IpPermission>();

		for (Object port : ports) {
			final IpPermission ipPermission = new IpPermission();
			ipPermission
				.withIpRanges("0.0.0.0/0")
				.withIpProtocol("tcp")
				.withFromPort(Integer.parseInt((String) port))
				.withToPort(Integer.parseInt((String) port));
			ipPermissions.add(ipPermission);
		}

		// Create request.
		final AuthorizeSecurityGroupIngressRequest request = new AuthorizeSecurityGroupIngressRequest();

		request
			.withGroupName(securityGroupName)
			.withIpPermissions(ipPermissions);

		// Send request.
		try {
			ec2Client.authorizeSecurityGroupIngress(request);
		}
		catch (AmazonServiceException e) {
			throw new ProvisionResourceException(e);
		}

		eventBus.publish(new ResourcePluginEvent(Severity.INFO, "Ports opened."));
	}

	/**
	 * Create a key pair for the EC2 instance.
	 *
	 * @return The key pair as string.
	 *
	 * @throws ProvisionResourceException If there was an error while creating the key pair.
	 */
	private String createKeyPair() throws ProvisionResourceException {

		String privateKey;

		// Create request.
		final CreateKeyPairRequest request = new CreateKeyPairRequest();

		request
			.withKeyName(keyName);

		// Send request and get the private key from the response.
		try {
			final CreateKeyPairResult result = ec2Client.createKeyPair(request);
			KeyPair keyPair = new KeyPair();
			keyPair         = result.getKeyPair();
			privateKey      = keyPair.getKeyMaterial();
		}
		catch (AmazonServiceException e) {
			throw new ProvisionResourceException(e);
		}

		eventBus.publish(new ResourcePluginEvent(Severity.INFO, "Key pair '" + keyName + "' created."));

		// For debugging: Write the private key to a local file.
		try {
			final PrintWriter out = new PrintWriter("aws-ec2-key.pem");
			out.println(privateKey);
			out.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return privateKey;
	}

	/**
	 * Deletes the key pair of the EC2 instance.
	 *
	 * @param name The name of the key pair that should be deleted.
	 *
	 * @throws DeprovisionResourceException If there was an error while deleting the key pair.
	 */
	private void deleteKeyPair(final String name) throws DeprovisionResourceException {

		// Create request.
		final DeleteKeyPairRequest request = new DeleteKeyPairRequest();

		request
			.withKeyName(name);

		// Send request.
		try {
			ec2Client.deleteKeyPair(request);
		}
		catch (AmazonServiceException e) {
			throw new DeprovisionResourceException(e);
		}

		eventBus.publish(new ResourcePluginEvent(Severity.INFO, "Key pair '" + name + "' deleted."));
	}

	/**
	 * Create an EC2 instance.
	 *
	 * @return The instance id as string.
	 *
	 * @throws ProvisionResourceException If there was an error while creating the EC2 instance.
	 */
	private String createEC2Instance() throws ProvisionResourceException {

		String instanceID;

		// Create request.
		final RunInstancesRequest request = new RunInstancesRequest();

		request
			.withImageId(imageId)
			.withInstanceType(instanceType)
			.withMinCount(1)
			.withMaxCount(1)
			.withSecurityGroups(securityGroupName)
			.withKeyName(keyName);

		// Send request and get the instance id from the response.
		try {
			final RunInstancesResult result = ec2Client.runInstances(request);
			final Reservation reservation = result.getReservation();
			instanceID = reservation.getInstances().get(0).getInstanceId();
		}
		catch (AmazonServiceException e) {
			throw new ProvisionResourceException(e);
		}

		eventBus.publish(new ResourcePluginEvent(Severity.INFO, "EC2 instance '" + instanceID + "' created."));

		// Wait until the instance has reached the state "running".
		waitForState(instanceID, "running");

		return instanceID;
	}

	/**
	 * Retrieve some information about the EC2 instance.
	 *
	 * @param instanceID The id of the EC2 instance.
	 *
	 * @return A map of strings that contains information about the EC2 instance.
	 *
	 * @throws ProvisionResourceException If there was an error when creating the security group.
	 */
	private Map<String, String> getInstanceInformation(final String instanceID) throws ProvisionResourceException {

		final Map<String, String> instanceInformation = new HashMap<String, String>();

		// Create request.
		final DescribeInstancesRequest ipRequest = new DescribeInstancesRequest().withInstanceIds(instanceID);

		// Send request and get some information.
		try {
			final DescribeInstancesResult ipResult = ec2Client.describeInstances(ipRequest);

			final String publicIP  = ipResult.getReservations().get(0).getInstances().get(0).getPublicIpAddress();
			final String publicDNS = ipResult.getReservations().get(0).getInstances().get(0).getPublicDnsName();

			instanceInformation.put("publicIP", publicIP);
			instanceInformation.put("publicDNS", publicDNS);
		}
		catch (AmazonServiceException e) {
			throw new ProvisionResourceException(e);
		}

		return instanceInformation;
	}

	/**
	 * Removes an EC2 instance.
	 *
	 * @param instanceID The id of the EC2 instance that should be removed
	 *
	 * @throws DeprovisionResourceException If there was an error while removing the EC2 instance.
	 */
	private void terminateEC2Instance(final String instanceID) throws DeprovisionResourceException {

		// Create request.
		final TerminateInstancesRequest request = new TerminateInstancesRequest();

		request
			.withInstanceIds(instanceID);

		// Send request.
		try {
			final TerminateInstancesResult result = ec2Client.terminateInstances(request);
		}
		catch (AmazonServiceException e) {
			throw new DeprovisionResourceException(e);
		}

		// Wait until the EC2 instance has reached the state "terminated".
		waitForState(instanceID, "terminated");
	}

	/**
	 * Wait until the given instance has reached the given state.
	 *
	 * @param instanceID The id of the instance for which should be waited.
	 * @param state The state the instance has to reach to stop the waiting.
	 */
	private void waitForState(final String instanceID, final String state) {
		eventBus.publish(new ResourcePluginEvent(Severity.INFO, "Waiting for '" + instanceID + "' to reach state '" + state + "'."));

	label:
		while (true) {

			DescribeInstancesResult result;

			// Create request.
			final DescribeInstancesRequest request = new DescribeInstancesRequest().withInstanceIds(instanceID);

			// Send request.
			try {
				result = ec2Client.describeInstances(request);
			}
			catch (AmazonServiceException e) {
				// Retry. The instance might not be up yet.
				continue label;
			}

			final List<Reservation> reservations = result.getReservations();

			// Get the state of the instance.
			for (Reservation reservation : reservations) {
				final List<Instance> instances = reservation.getInstances();
				for (Instance instance : instances) {
					// If the state equals the requested state, break the loop.
					if (instance.getState().getName().equals(state)) {
						eventBus.publish(new ResourcePluginEvent(Severity.INFO, "'" + instanceID + "' has reached state '" + state + "'."));
						break label;
					}
				}
			}

			// Retry after some time.
			try {
				Thread.sleep(5000);
			}
			catch (InterruptedException ex) {
				System.out.println(ex);
			}
		}
	}

}
