package org.simtech.bootware.plugins.resource.test;

import java.util.HashMap;
import java.util.Map;

import org.simtech.bootware.core.ConfigurationWrapper;
import org.simtech.bootware.core.events.ResourcePluginEvent;
import org.simtech.bootware.core.events.Severity;
import org.simtech.bootware.core.exceptions.DeprovisionResourceException;
import org.simtech.bootware.core.exceptions.InitializeException;
import org.simtech.bootware.core.exceptions.ProvisionResourceException;
import org.simtech.bootware.core.plugins.AbstractBasePlugin;
import org.simtech.bootware.core.plugins.ResourcePlugin;

/**
 * An resource plugin that can be used for testing.
 */
public class Test extends AbstractBasePlugin implements ResourcePlugin {

	public Test() {}

	public final void initialize(final Map<String, ConfigurationWrapper> configurationList) throws InitializeException {
		// do initialization stuff
	}

	public final void shutdown() {
		// no shutdown stuff
	}

	public final Map<String, String> provision() throws ProvisionResourceException {
		eventBus.publish(new ResourcePluginEvent(Severity.INFO, "Provision has been called."));
		final Map<String, String> instanceInformation = new HashMap<String, String>();
		instanceInformation.put("resourceURL", "54.194.203.216");
		instanceInformation.put("sshUsername", "ubuntu");
		instanceInformation.put("sshKey", "-----BEGIN RSA PRIVATE KEY-----\n"
				+ "MIIEogIBAAKCAQEAoOEwovOYOQWJyn9SJUpeFskP/+X+IOFU9NtPlL6h+yZnlAovELBdCIWUtkXk\n"
				+ "Twaoc+s07CPnWG/fcQYZHyJiL5pzAkulFEzKRFlw9m78XS4tbUQUxetNy2uw5nq5VMG+S+UqYybZ\n"
				+ "dpyW1vTApj5V7r1BCWkZ68U/sIHAPMe/nuiPlQJrlHY4Dcd+ru+HAjERXxda07W2k8g6ZmqjEsH7\n"
				+ "wMjwVv9DrtwZo2X2hzRcqVvH9XQfIFlfpRrsMWQjlPOJO1jEa4RzTyM31DZ6gRVb74dTCdARehXW\n"
				+ "eVnRzwqzyaF2q+71IpsMKoHnw7l1+ggV0+4VGFEw2oBEd0XlWf2jywIDAQABAoIBABVoggFdKweD\n"
				+ "fieNM9A0ijq7/L4Py/ZyJc0Dlh50E7gJD0V66XJuFaM5q2Vp/YyqlSX/yaYWzQVOtcwjhJpJCAnP\n"
				+ "orhOeqQt58iaolaYxEpeB7t/kMgZnNuR9BNnh1wJABq3XyGcnNmU0tm97hjxOJwxYQMwkjt2nxgM\n"
				+ "3Bj6d7xcfeTDav5Ao6v/nRkaMd3oDZm1ygoAHNnhWz8/tpkLFrki0Sg3v/TGymNdEe/ZM4HjB9x4\n"
				+ "kOEy1VlnLqSb5aIgMNBbmQvIyDHBCMVq9fmYbUiRWFhZeGmlKcZ5JINcvaEnleIc4bRPMH1Fdetq\n"
				+ "B8UAUMke8qumSwdWxTfUGLhXzqkCgYEA30dVCMFjqKYpH5vJqNB5MHLuXeF8iKKUtbieTbnkrSUH\n"
				+ "cChGZXOVnXbNf0wVj6iW1XoGrrm0LHgo2XezsrPmdW4vk+WjKAg0vEeBYuiytO0bZWRQW54y1KDe\n"
				+ "l2SnLtraBZegV0vGUp/vd0Z3RoS0+8NtEQltoI1nn1CEjL1WpccCgYEAuHTbNyefwdind+q/ZHze\n"
				+ "M1tQXBcPB+Yoaybx8N/B3hYW2tP2Ah3XHAiugInnT3saUTk4gLMXuY2fnX1oogSLttHf972DAhwv\n"
				+ "T6bIMLxVLqgM2hN3SurXMcNCsSOyIXQhQ+HiEbmRVTlDAC7TMN3LZwxfcMT5vFf2m2Nc6JUfQd0C\n"
				+ "gYBqzZ9Sadd3E8x2Dzo+Y0KlN0ToaXT9ku+ZoW8v9Oz7GcxeDRXnb4uw3+RasRVbO6SzBwTtTW1v\n"
				+ "HCdGJykxITbrE0L09xK9kndPXND6dB8Pxp+lN3/s41ajOTRgrMf/9LX3mNkdCOUcHt8Yaf+iHtbH\n"
				+ "22pvjNgAxcM8hPJytBsffwKBgDc+aFlAeMxpwt5bZC8IZtQiyRKLpWjMOda9t0BZA4ssOnQXf1pq\n"
				+ "7X3r67d7lddsGxmwdYe2G6jrDZ7xbGD5045RlS5xh8ceAd1tKg/OmAR9ODwoEFZTt1ekD1lWQ4dZ\n"
				+ "bI0UHc0JMsm/eJ2ZaI7+Hmkif4Mau2D5R1aKj8zovI/RAoGAcdNpCcILI3g8aqcanYMbCRk7xUuH\n"
				+ "lJRqqj6em2FZKd0KySUM9IgHe+mlv3Nu0xWdLE/YXFyL0jwMRUG2g7kkNXvgvfXNZmRb+t5THmQj\n"
				+ "P+2vKzqGxsaFPFPkuarGb3YvdyFuyJSbPlev/UFgolAuZZ414AS7QLjINvs+LL8+/SE=\n"
				+ "-----END RSA PRIVATE KEY-----");
		return instanceInformation;
	}

	public final void deprovision(final Map<String, String> instanceInformation) throws DeprovisionResourceException {
		eventBus.publish(new ResourcePluginEvent(Severity.INFO, "Deprovision has been called."));
	}

}
