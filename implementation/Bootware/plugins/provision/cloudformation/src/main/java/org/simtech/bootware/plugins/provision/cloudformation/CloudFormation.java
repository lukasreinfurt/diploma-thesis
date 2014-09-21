package org.simtech.bootware.plugins.provision.cloudformation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.configuration.MapConfiguration;

import org.simtech.bootware.core.ApplicationInstance;
import org.simtech.bootware.core.ConfigurationWrapper;
import org.simtech.bootware.core.exceptions.DeprovisionException;
import org.simtech.bootware.core.exceptions.InitializeException;
import org.simtech.bootware.core.exceptions.ProvisionException;
import org.simtech.bootware.core.plugins.AbstractBasePlugin;
import org.simtech.bootware.core.plugins.ProvisionPlugin;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClient;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.CreateStackResult;
import com.amazonaws.services.cloudformation.model.DeleteStackRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksResult;
import com.amazonaws.services.cloudformation.model.Output;
import com.amazonaws.services.cloudformation.model.Parameter;
import com.amazonaws.services.cloudformation.model.Stack;

public class CloudFormation extends AbstractBasePlugin implements ProvisionPlugin {

	private String secretKey;
	private String accessKey;
	private String region;

	private AmazonCloudFormationClient cloudFormationClient;

	public CloudFormation() {}

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
			secretKey = configuration.getString("secretKey");
			accessKey = configuration.getString("accessKey");
			region    = configuration.getString("region");
		}
		catch (NoSuchElementException e) {
			throw new InitializeException(e);
		}

		System.out.println(secretKey);
		System.out.println(accessKey);

		createClientInstance();
	}

	public final void shutdown() {
		// no op
	}

	public final Map<String, String> provision(final ApplicationInstance instance) throws ProvisionException {
		final String servicePackageReference = instance.getUserContext().getServicePackageReference();
		createStack(servicePackageReference);
		return getStackOutput();
	}

	public final void deprovision(final ApplicationInstance instance) throws DeprovisionException {

		final DeleteStackRequest request = new DeleteStackRequest();

		request.withStackName("Test");

		try {
			cloudFormationClient.deleteStack(request);
		}
		catch (AmazonServiceException e) {
			throw new DeprovisionException(e);
		}

		waitForStackToReachStatus("DELETE_COMPLETE");
	}

	private void createClientInstance() {
		// load credentials
		final AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

		// create client instance
		cloudFormationClient = new AmazonCloudFormationClient(credentials);

		cloudFormationClient.setEndpoint("https://cloudformation.eu-west-1.amazonaws.com");
	}

	private void createStack(final String templateURL) throws ProvisionException {

		final Parameter parameter = new Parameter();

		parameter
			.withParameterKey("KeyName")
			.withParameterValue("test");

		final CreateStackRequest request = new CreateStackRequest();

		request
			.withStackName("Test")
			.withTemplateURL(templateURL)
			.withParameters(parameter);

		try {
			final CreateStackResult result = cloudFormationClient.createStack(request);
		}
		catch (AmazonServiceException e) {
			throw new ProvisionException(e);
		}

		waitForStackToReachStatus("CREATE_COMPLETE");
	}

	private Map<String, String> getStackOutput() throws ProvisionException {

		final Map<String, String> outputMap = new HashMap<String, String>();

		final DescribeStacksRequest request = new DescribeStacksRequest().withStackName("Test");

		try {
			final DescribeStacksResult result = cloudFormationClient.describeStacks(request);

			final List<Stack> stacks = result.getStacks();

			for (Stack stack : stacks) {
				final List<Output> outputs = stack.getOutputs();
				for (Output output : outputs) {
					outputMap.put(output.getOutputKey(), output.getOutputValue());
				}
			}
		}
		catch (AmazonServiceException e) {
			throw new ProvisionException(e);
		}

		return outputMap;
	}

	private void waitForStackToReachStatus(final String status) {

	label:
		while (true) {

			DescribeStacksResult result;

			// Create request.
			final DescribeStacksRequest request = new DescribeStacksRequest().withStackName("Test");

			// Send request.
			try {
				result = cloudFormationClient.describeStacks(request);
			}
			catch (AmazonServiceException e) {
				// Retry. The instance might not be up yet.
				if ("DELETE_COMPLETE".equals(status)) {
					System.out.println("'" + "Test" + "' has reached status '" + status + "'.");
					break label;
				}
				else {
					continue label;
				}
			}

			final List<Stack> stacks = result.getStacks();

			// Get the state of the instance.
			for (Stack stack : stacks) {
				// If the state equals the requested state, break the loop.
				if (stack.getStackStatus().equals(status)) {
					System.out.println("'" + "Test" + "' has reached status '" + status + "'.");
					break label;
				}
			}

			// Retry after some time.
			try {
				final Integer wait = 5000;
				Thread.sleep(wait);
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}

	}



}
