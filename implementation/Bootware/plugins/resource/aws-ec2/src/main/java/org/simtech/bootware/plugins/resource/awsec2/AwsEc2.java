package org.simtech.bootware.plugins.resource.awsec2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.simtech.bootware.core.ConfigurationWrapper;
import org.simtech.bootware.core.events.ResourcePluginEvent;
import org.simtech.bootware.core.events.Severity;
import org.simtech.bootware.core.exceptions.ConfigurationException;
import org.simtech.bootware.core.exceptions.DeprovisionResourceException;
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
	private String securityGroupName;
	private String keyName;

	private AmazonEC2 ec2Client;

	public AwsEc2() {}

	public final void initialize(final ConfigurationWrapper configuration) {
		try {
			secretKey = configuration.get("secretKey");
			accessKey = configuration.get("accessKey");
			username  = configuration.get("username");
		}
		catch (ConfigurationException e) {
			System.out.println(e.toString());
		}
		securityGroupName = "GeneratedSecurityGroup";
		keyName           = "BootwareKey";
		createClientInstance();
	}

	public final void shutdown() {
		// no op
	}

	public final Map<String, String> provision() throws ProvisionResourceException {
		createSecurityGroup();
		openPorts();
		final String privateKey = createKeyPair();
		final String instanceID = createEC2Instance();
		final Map<String, String> instanceInformation = getInstanceInformation(instanceID);

		instanceInformation.put("username", username);
		instanceInformation.put("securityGroupName", securityGroupName);
		instanceInformation.put("keyName", keyName);
		instanceInformation.put("privateKey", privateKey);
		instanceInformation.put("instanceID", instanceID);

		eventBus.publish(new ResourcePluginEvent(Severity.SUCCESS, "Provisioning complete."));

		return instanceInformation;
	}

	public final void deprovision(final Map<String, String> instanceInformation) throws DeprovisionResourceException {
		terminateEC2Instance(instanceInformation.get("instanceID"));
		deleteKeyPair(instanceInformation.get("keyName"));
		deleteSecurityGroup(instanceInformation.get("securityGroupName"));
	}

	private void createClientInstance() {
		// load credentials
		final AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

		// create client instance (not an actual ec2 instance)
		ec2Client = new AmazonEC2Client(credentials);

		// set region
		ec2Client.setEndpoint("ec2.eu-west-1.amazonaws.com");
	}

	private void createSecurityGroup() throws ProvisionResourceException {
		final CreateSecurityGroupRequest request = new CreateSecurityGroupRequest();

		request
			.withGroupName(securityGroupName)
			.withDescription("Generated by SimTech Bootware");

		try {
			final CreateSecurityGroupResult result = ec2Client.createSecurityGroup(request);
		}
		catch (AmazonServiceException e) {
			throw new ProvisionResourceException(e);
		}

		eventBus.publish(new ResourcePluginEvent(Severity.INFO, "Security group '" + securityGroupName + "' created."));
	}

	private void deleteSecurityGroup(final String name) throws DeprovisionResourceException {
		final DeleteSecurityGroupRequest request = new DeleteSecurityGroupRequest();

		request
			.withGroupName(name);

		try {
			ec2Client.deleteSecurityGroup(request);
		}
		catch (AmazonServiceException e) {
			throw new DeprovisionResourceException(e);
		}

		eventBus.publish(new ResourcePluginEvent(Severity.INFO, "Security group '" + securityGroupName + "' deleted."));
	}

	private void openPorts() throws ProvisionResourceException {
		final IpPermission ipPermission = new IpPermission();

		ipPermission
			.withIpRanges("0.0.0.0/0")
			.withIpProtocol("tcp")
			.withFromPort(22)
			.withToPort(22);

		final AuthorizeSecurityGroupIngressRequest request = new AuthorizeSecurityGroupIngressRequest();

		request
			.withGroupName(securityGroupName)
			.withIpPermissions(ipPermission);

		try {
			ec2Client.authorizeSecurityGroupIngress(request);
		}
		catch (AmazonServiceException e) {
			throw new ProvisionResourceException(e);
		}

		eventBus.publish(new ResourcePluginEvent(Severity.INFO, "Ports opened."));
	}

	private String createKeyPair() throws ProvisionResourceException {
		String privateKey;
		final CreateKeyPairRequest request = new CreateKeyPairRequest();

		request
			.withKeyName(keyName);

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
		return privateKey;
	}

	private void deleteKeyPair(final String name) throws DeprovisionResourceException {
		final DeleteKeyPairRequest request = new DeleteKeyPairRequest();

		request
			.withKeyName(name);

		try {
			ec2Client.deleteKeyPair(request);
		}
		catch (AmazonServiceException e) {
			throw new DeprovisionResourceException(e);
		}

		eventBus.publish(new ResourcePluginEvent(Severity.INFO, "Key pair '" + name + "' deleted."));
	}

	private String createEC2Instance() throws ProvisionResourceException {
		String instanceID;
		final RunInstancesRequest request = new RunInstancesRequest();

		request
			.withImageId("ami-2918e35e")
			.withInstanceType("t1.micro")
			.withMinCount(1)
			.withMaxCount(1)
			.withSecurityGroups(securityGroupName)
			.withKeyName(keyName);

		try {
			final RunInstancesResult result = ec2Client.runInstances(request);
			final Reservation reservation = result.getReservation();
			instanceID = reservation.getInstances().get(0).getInstanceId();
		}
		catch (AmazonServiceException e) {
			throw new ProvisionResourceException(e);
		}

		eventBus.publish(new ResourcePluginEvent(Severity.INFO, "EC2 instance '" + instanceID + "' created."));
		waitForState(instanceID, "running");

		return instanceID;
	}

	private Map<String, String> getInstanceInformation(final String instanceID) throws ProvisionResourceException {
		final Map<String, String> instanceInformation = new HashMap<String, String>();
		final DescribeInstancesRequest ipRequest = new DescribeInstancesRequest().withInstanceIds(instanceID);

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

	private void terminateEC2Instance(final String instanceID) throws DeprovisionResourceException {
		final TerminateInstancesRequest request = new TerminateInstancesRequest();

		request
			.withInstanceIds(instanceID);

		try {
			final TerminateInstancesResult result = ec2Client.terminateInstances(request);
		}
		catch (AmazonServiceException e) {
			throw new DeprovisionResourceException(e);
		}

		waitForState(instanceID, "terminated");
	}

	private void waitForState(final String instanceID, final String state) {
		eventBus.publish(new ResourcePluginEvent(Severity.INFO, "Waiting for '" + instanceID + "' to reach state '" + state + "'."));

	label:
		while (true) {

			DescribeInstancesResult result;
			final DescribeInstancesRequest request = new DescribeInstancesRequest().withInstanceIds(instanceID);

			result = ec2Client.describeInstances(request);

			final List<Reservation> reservations   = result.getReservations();

			for (Reservation reservation : reservations) {
				final List<Instance> instances = reservation.getInstances();
				for (Instance instance : instances) {
					if (instance.getState().getName().equals(state)) {
						eventBus.publish(new ResourcePluginEvent(Severity.INFO, "'" + instanceID + "' has reached state '" + state + "'."));
						break label;
					}
				}
			}

			try {
				Thread.sleep(5000);
			}
			catch (InterruptedException ex) {
				System.out.println(ex);
			}
		}
	}

}
