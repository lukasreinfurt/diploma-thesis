/**
 * 
 */
package org.eclipse.bpel.ui.agora.communication;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;

import org.apache.ode135.deploy.client.DeployUnit;
import org.apache.ode135.deploy.client.DeploymentPortType;
import org.apache.ode135.deploy.client.DeploymentServiceLocator;
import org.apache.ode135.deploy.client._package;
import org.apache.ode135.deploy.client.mime.Base64Binary;
import org.eclipse.bpel.ui.agora.ode135.client.GetSnapshotPartnerLinksFault;
import org.eclipse.bpel.ui.agora.ode135.client.GetSnapshotVariablesFault;
import org.eclipse.bpel.ui.agora.ode135.client.InstanceManagement;
import org.eclipse.bpel.ui.agora.ode135.client.InstanceManagementPortType;
import org.eclipse.bpel.ui.agora.ode135.client.ManagementFault;
import org.eclipse.bpel.ui.agora.ode135.client.TPartnerLinkInfo;
import org.eclipse.bpel.ui.agora.ode135.client.TPartnerLinkRefList;
import org.eclipse.bpel.ui.agora.ode135.client.TSnapshotInfo;
import org.eclipse.bpel.ui.agora.ode135.client.TSnapshotInfoList;
import org.eclipse.bpel.ui.agora.ode135.client.TVariableInfo;
import org.eclipse.bpel.ui.agora.ode135.client.TVariableRefList;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

/**
 * This class realizes an adapter (facade) for the ODE 1.3.5 Client
 * implementation.
 * 
 * @author hahnml
 * 
 */
public class ODEClientAdapter135 extends ODEClientAdapter {

	private static final QName INSTANCEMANAGEMENT_SERVICE_NAME;

	static {
		INSTANCEMANAGEMENT_SERVICE_NAME = new QName(
				"http://www.apache.org/ode/pmapi", "InstanceManagement");
	}

	public static void suspendInstance(Long instanceID) {
		try {
			InstanceManagementPortType instance = new InstanceManagement(
					getODEUrl("/processes/InstanceManagement?wsdl"),
					INSTANCEMANAGEMENT_SERVICE_NAME)
					.getInstanceManagementPort();
			instance.suspend(instanceID);
		} catch (ManagementFault e) {
			MessageDialog.openError(Display.getCurrent().getActiveShell(),
					"Suspend caused an exception", e.getMessage());
		}
	}

	public static void iterateInInstance(Long instanceID, String activity_xPath) {
		try {
			InstanceManagementPortType instance = new InstanceManagement(
					getODEUrl("/processes/InstanceManagement?wsdl"),
					INSTANCEMANAGEMENT_SERVICE_NAME)
					.getInstanceManagementPort();
			instance.iterate(instanceID, activity_xPath);
		} catch (ManagementFault e) {
			MessageDialog.openError(Display.getCurrent().getActiveShell(),
					"Iterate caused an exception", e.getMessage());
		}
	}

	public static void iterateExtInInstance(Long instanceID,
			String activity_xPath, String snapshot_xPath, Long version,
			TVariableRefList variables, TPartnerLinkRefList partnerLinks) {
		try {
			InstanceManagementPortType instance = new InstanceManagement(
					getODEUrl("/processes/InstanceManagement?wsdl"),
					INSTANCEMANAGEMENT_SERVICE_NAME)
					.getInstanceManagementPort();

			instance.iterateExt(instanceID, activity_xPath, snapshot_xPath,
					version, variables, partnerLinks);
		} catch (ManagementFault e) {
			MessageDialog.openError(Display.getCurrent().getActiveShell(),
					"Iterate caused an exception", e.getMessage());
		}
	}

	public static void jumpToActivityInInstance(Long instanceID,
			String activity_xPath) {
		try {
			InstanceManagementPortType instance = new InstanceManagement(
					getODEUrl("/processes/InstanceManagement?wsdl"),
					INSTANCEMANAGEMENT_SERVICE_NAME)
					.getInstanceManagementPort();
			instance.jumpToActivity(instanceID, activity_xPath);
		} catch (ManagementFault e) {
			MessageDialog.openError(Display.getCurrent().getActiveShell(),
					"JumpTo caused an exception", e.getMessage());
		}
	}

	public static void reexecuteInInstance(Long instanceID,
			String activity_xPath, String snapshot_xPath, Long version) {
		try {
			InstanceManagementPortType instance = new InstanceManagement(
					getODEUrl("/processes/InstanceManagement?wsdl"),
					INSTANCEMANAGEMENT_SERVICE_NAME)
					.getInstanceManagementPort();
			instance.reexecute(instanceID, activity_xPath, snapshot_xPath,
					version);
		} catch (ManagementFault e) {
			MessageDialog.openError(Display.getCurrent().getActiveShell(),
					"Reexecute caused an exception", e.getMessage());
		}
	}

	public static void reexecuteExtInInstance(Long instanceID,
			String activity_xPath, String snapshot_xPath, Long version,
			TVariableRefList variables, TPartnerLinkRefList partnerLinks) {
		try {
			InstanceManagementPortType instance = new InstanceManagement(
					getODEUrl("/processes/InstanceManagement?wsdl"),
					INSTANCEMANAGEMENT_SERVICE_NAME)
					.getInstanceManagementPort();

			instance.reexecuteExt(instanceID, activity_xPath, snapshot_xPath,
					version, variables, partnerLinks);
		} catch (ManagementFault e) {
			MessageDialog.openError(Display.getCurrent().getActiveShell(),
					"Reexecute caused an exception", e.getMessage());
		}
	}

	public static void resumeInstance(Long instanceID) {
		try {
			InstanceManagementPortType instance = new InstanceManagement(
					getODEUrl("/processes/InstanceManagement?wsdl"),
					INSTANCEMANAGEMENT_SERVICE_NAME)
					.getInstanceManagementPort();
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
					.getInstanceManagementPort();
			instance.terminate(instanceID);
		} catch (ManagementFault e) {
			MessageDialog.openError(Display.getCurrent().getActiveShell(),
					"Terminate caused an exception", e.getMessage());
		}
	}

	public static void finishInstance(Long instanceID) {
		try {
			InstanceManagementPortType instance = new InstanceManagement(
					getODEUrl("/processes/InstanceManagement?wsdl"),
					INSTANCEMANAGEMENT_SERVICE_NAME)
					.getInstanceManagementPort();
			instance.finish(instanceID);
		} catch (ManagementFault e) {
			MessageDialog.openError(Display.getCurrent().getActiveShell(),
					"Finish caused an exception", e.getMessage());
		}
	}

	public static String deployProcess(IPath path) throws ServiceException,
			IOException {
		String processFileName = "test";
		processFileName = path.removeFileExtension().lastSegment();

		File processFolder = path.toFile().getParentFile();
		ByteArrayOutputStream dataOut = ManagementAPIHandler
				.zipFolder(processFolder);
		// old
		// DeploymentServicePortType deploymentService = new
		// DeploymentService().getDeploymentServiceSOAP11PortHttp();
		//
		// Package zipPackage = new Package();
		// Base64Binary zip = new Base64Binary();
		// zip.setValue(dataOut.toByteArray());
		//
		// dataOut.close();
		//
		// zipPackage.setZip(zip);
		//
		// DeployUnit response = deploymentService.deploy(processFileName,
		// zipPackage);
		// return response.getName();

		DeploymentPortType portType = new DeploymentServiceLocator()
				.getDeploymentPort(getODEUrl("/processes/DeploymentService"));
		_package zipPackage = new _package();
		Base64Binary zip = new Base64Binary();
		zip.set_value(dataOut.toByteArray());
		dataOut.close();
		zipPackage.setZip(zip);
		DeployUnit response = portType.deploy(processFileName, zipPackage);
		return response.getName();
	}

	public static String deployNewVersionOfProcess(IPath path, Long instanceID)
			throws ServiceException, IOException {
		String processFileName = "test";
		processFileName = path.removeFileExtension().lastSegment();

		File processFolder = path.toFile().getParentFile();
		ByteArrayOutputStream dataOut = ManagementAPIHandler
				.zipFolder(processFolder);
		// old
		// DeploymentServicePortType deploymentService = new
		// DeploymentService().getDeploymentServiceSOAP11PortHttp();
		//
		// Package zipPackage = new Package();
		// Base64Binary zip = new Base64Binary();
		// zip.setValue(dataOut.toByteArray());
		//
		// dataOut.close();
		//
		// zipPackage.setZip(zip);
		//
		// DeployUnit response = deploymentService.deployNewVersion(instanceID,
		// processFileName,
		// zipPackage);
		// return response.getName();
		DeploymentPortType portType = new DeploymentServiceLocator()
				.getDeploymentPort(getODEUrl("/processes/DeploymentService"));
		_package zipPackage = new _package();
		Base64Binary zip = new Base64Binary();
		zip.set_value(dataOut.toByteArray());
		dataOut.close();
		zipPackage.setZip(zip);
		DeployUnit response = portType.deployNewVersion(processFileName,
				zipPackage, instanceID);
		return response.getName();
	}

	public static void undeployProcess(String packageName) throws Exception {
		DeploymentPortType portType = new DeploymentServiceLocator()
				.getDeploymentPort(getODEUrl("/processes/DeploymentService"));

		portType.undeploy(new QName(packageName));
	}

	public static TSnapshotInfoList getSnapshots(Long instanceID,
			String act_xPath) {
		try {
			InstanceManagementPortType instance = new InstanceManagement(
					getODEUrl("/processes/InstanceManagement?wsdl"),
					INSTANCEMANAGEMENT_SERVICE_NAME)
					.getInstanceManagementPort();

			return instance.getSnapshots(instanceID, act_xPath);

		} catch (ManagementFault e) {
			MessageDialog
					.openError(
							Display.getCurrent().getActiveShell(),
							"An exception occurs during the collection of snapshot information",
							e.getMessage());
		}

		return null;
	}

	public static TSnapshotInfo getSnapshotVersion(Long instanceID,
			String act_xPath) {
		try {
			InstanceManagementPortType instance = new InstanceManagement(
					getODEUrl("/processes/InstanceManagement?wsdl"),
					INSTANCEMANAGEMENT_SERVICE_NAME)
					.getInstanceManagementPort();

			return instance.getSnapshotVersions(instanceID, act_xPath);

		} catch (ManagementFault e) {
			MessageDialog
					.openError(
							Display.getCurrent().getActiveShell(),
							"An exception occurs during the collection of snapshot information",
							e.getMessage());
		}

		return null;
	}

	public static List<TVariableInfo> getSnapshotVariables(Long instanceID,
			Long snapshotID) {
		try {
			InstanceManagementPortType instance = new InstanceManagement(
					getODEUrl("/processes/InstanceManagement?wsdl"),
					INSTANCEMANAGEMENT_SERVICE_NAME)
					.getInstanceManagementPort();

			return instance.getSnapshotVariables(instanceID, snapshotID)
					.getVariableInfo();

		} catch (GetSnapshotVariablesFault e) {
			MessageDialog
					.openError(
							Display.getCurrent().getActiveShell(),
							"An exception occurs during the collection of snapshot variables",
							e.getMessage());
		}

		return null;
	}

	public static List<TPartnerLinkInfo> getSnapshotPartnerLinks(
			Long instanceID, Long snapshotID) {
		try {
			InstanceManagementPortType instance = new InstanceManagement(
					getODEUrl("/processes/InstanceManagement?wsdl"),
					INSTANCEMANAGEMENT_SERVICE_NAME)
					.getInstanceManagementPort();

			return instance.getSnapshotPartnerLinks(instanceID, snapshotID)
					.getPartnerLinkInfo();

		} catch (GetSnapshotPartnerLinksFault e) {
			MessageDialog
					.openError(
							Display.getCurrent().getActiveShell(),
							"An exception occurs during the collection of snapshot partnerLinks",
							e.getMessage());
		}

		return null;
	}
}
