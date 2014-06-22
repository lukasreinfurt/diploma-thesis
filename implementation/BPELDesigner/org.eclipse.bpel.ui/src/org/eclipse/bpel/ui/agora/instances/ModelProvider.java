package org.eclipse.bpel.ui.agora.instances;

import java.util.HashMap;

import org.simtech.workflow.ode.auditing.communication.messages.InstanceInformation;

/**
 * Holds all known instances during runtime.
 * 
 * @author hahnml
 *
 */
public class ModelProvider {
	private static ModelProvider content;
	private HashMap<Long, InstanceInformation> instances;

	private ModelProvider() {
		instances = new HashMap<Long, InstanceInformation>();
	}

	public static synchronized ModelProvider getInstance() {
		if (content != null) {
			return content;
		}
		content = new ModelProvider();
		return content;
	}

	public HashMap<Long, InstanceInformation> getInstances() {
		return instances;
	}

}
