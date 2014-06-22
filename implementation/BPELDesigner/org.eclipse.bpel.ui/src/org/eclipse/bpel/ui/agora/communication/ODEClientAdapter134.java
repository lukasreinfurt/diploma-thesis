/**
 * 
 */
package org.eclipse.bpel.ui.agora.communication;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.xml.namespace.QName;

import org.eclipse.bpel.ui.agora.ode134.client.Base64Binary;
import org.eclipse.bpel.ui.agora.ode134.client.DeployUnit;
import org.eclipse.bpel.ui.agora.ode134.client.DeploymentService;
import org.eclipse.bpel.ui.agora.ode134.client.DeploymentServicePortType;
import org.eclipse.bpel.ui.agora.ode134.client.InstanceManagement;
import org.eclipse.bpel.ui.agora.ode134.client.InstanceManagementPortType;
import org.eclipse.bpel.ui.agora.ode134.client.ManagementFault;
import org.eclipse.bpel.ui.agora.ode134.client.Package;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

/**
 * This class realizes an adapter (facade) for the ODE 1.3.4 Client
 * implementation.
 * 
 * @author hahnml
 * 
 */
public class ODEClientAdapter134 extends ODEClientAdapter {

	private static final QName INSTANCEMANAGEMENT_SERVICE_NAME;
	private static final QName PROCESSMANAGEMENT_SERVICE_NAME;

	static {
		INSTANCEMANAGEMENT_SERVICE_NAME = new QName(
				"http://www.apache.org/ode/pmapi", "InstanceManagement");
		PROCESSMANAGEMENT_SERVICE_NAME = new QName(
				"http://www.apache.org/ode/pmapi", "ProcessManagement");
	}

	public static void suspendInstance(Long instanceID) {
		try {
			InstanceManagementPortType instance = new InstanceManagement(
					getODEUrl("/processes/InstanceManagement?wsdl"),
					INSTANCEMANAGEMENT_SERVICE_NAME)
					.getInstanceManagementSOAP11PortHttp();
			instance.suspend(instanceID);
		} catch (ManagementFault e) {
			MessageDialog.openError(Display.getCurrent().getActiveShell(),
					"Suspend caused an exception", e.getMessage());
		}
	}

	public static void resumeInstance(Long instanceID) {
		try {
			InstanceManagementPortType instance = new InstanceManagement(
					getODEUrl("/processes/InstanceManagement?wsdl"),
					INSTANCEMANAGEMENT_SERVICE_NAME)
					.getInstanceManagementSOAP11PortHttp();
			instance.resume(instanceID);
		} catch (ManagementFault e) {
			MessageDialog.openError(Display.getCurrent().getActiveShell(),
					"Resume caused an exception", e.getMessage());
		}
	}

	public static void terminateInstance(Long instanceID) {
		try {
			InstanceManagementPortType instance = new InstanceManagement(
					getODEUrl("/processes/InstanceManagement?wsdl"),
					INSTANCEMANAGEMENT_SERVICE_NAME)
					.getInstanceManagementSOAP11PortHttp();
			instance.terminate(instanceID);
		} catch (ManagementFault e) {
			MessageDialog.openError(Display.getCurrent().getActiveShell(),
					"Terminate caused an exception", e.getMessage());
		}
	}

	public static String deployProcess(IPath path) throws IOException {
		String processFileName = "test";
		processFileName = path.removeFileExtension().lastSegment();

		File processFolder = path.toFile().getParentFile();
		ByteArrayOutputStream dataOut = ManagementAPIHandler
				.zipFolder(processFolder);

		DeploymentServicePortType deploymentService = new DeploymentService(
				getODEUrl("/processes/ProcessManagement?wsdl"),
				PROCESSMANAGEMENT_SERVICE_NAME)
				.getDeploymentServiceSOAP11PortHttp();

		Package zipPackage = new Package();
		Base64Binary zip = new Base64Binary();
		zip.setValue(dataOut.toByteArray());

		dataOut.close();

		zipPackage.setZip(zip);

		DeployUnit response = deploymentService.deploy(processFileName,
				zipPackage);
		return response.getName();
	}

	public static void undeployProcess(String packageName) {
		DeploymentServicePortType deploymentService = new DeploymentService(
				getODEUrl("/processes/ProcessManagement?wsdl"),
				PROCESSMANAGEMENT_SERVICE_NAME)
				.getDeploymentServiceSOAP11PortHttp();

		deploymentService.undeploy(new QName(packageName));
	}
}
