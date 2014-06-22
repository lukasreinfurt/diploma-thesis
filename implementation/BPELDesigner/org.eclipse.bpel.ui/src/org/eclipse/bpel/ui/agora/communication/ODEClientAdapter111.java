/**
 * 
 */
package org.eclipse.bpel.ui.agora.communication;

import java.io.ByteArrayOutputStream;
import java.io.File;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.util.Base64;
import org.apache.axis2.AxisFault;
import org.apache.ode.axis2.service.ServiceClientUtil;
import org.eclipse.apache.ode.processmanagement.IProcessManagementConstants;
import org.eclipse.apache.ode.processmanagement.ProcessManagementUI;
import org.eclipse.bpel.ui.agora.ode111.client.InstanceManagementPortType;
import org.eclipse.bpel.ui.agora.ode111.client.InstanceManagementServiceLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

/**
 * This class realizes an adapter (facade) for the ODE 1.1.1 Client
 * implementation.
 * 
 * @author hahnml
 * 
 */
public class ODEClientAdapter111 extends ODEClientAdapter {

	private static final String NAMESPACE = "http://www.apache.org/ode/pmapi/types/2006/08/02/";
	private static final String EPR_DEPLOYMENT_SERVICE = "/processes/DeploymentService";

	public static void suspendInstance(Long instanceID) {
		try {
			InstanceManagementPortType instanceService = new InstanceManagementServiceLocator()
					.getInstanceManagementPort(getODEUrl("/processes/InstanceManagement"));

			instanceService.suspend(instanceID);
		} catch (Exception e) {
			MessageDialog.openError(Display.getCurrent().getActiveShell(),
					"Suspend caused an exception", e.getMessage());
		}
	}

	public static void resumeInstance(Long instanceID) {
		try {
			InstanceManagementPortType instanceService = new InstanceManagementServiceLocator()
					.getInstanceManagementPort(getODEUrl("/processes/InstanceManagement"));

			instanceService.resume(instanceID);
		} catch (Exception e) {
			MessageDialog.openError(Display.getCurrent().getActiveShell(),
					"Resume caused an exception", e.getMessage());
		}
	}

	public static void terminateInstance(Long instanceID) {
		try {
			InstanceManagementPortType instanceService = new InstanceManagementServiceLocator()
					.getInstanceManagementPort(getODEUrl("/processes/InstanceManagement"));

			instanceService.terminate(instanceID);
		} catch (Exception e) {
			MessageDialog.openError(Display.getCurrent().getActiveShell(),
					"Terminate caused an exception", e.getMessage());
		}
	}

	public static String deployProcess(IPath path) {
		File processFolder = path.toFile().getParentFile();
		ByteArrayOutputStream dataOut = ManagementAPIHandler
				.zipFolder(processFolder);

		ServiceClientUtil client = new ServiceClientUtil();
		OMElement msg = null;
		OMFactory _factory;
		_factory = OMAbstractFactory.getOMFactory();
		OMNamespace depns = _factory.createOMNamespace(NAMESPACE, "deployapi");

		OMElement zipElmt = _factory.createOMElement("zip", depns);

		String base64Enc = Base64.encode(dataOut.toByteArray());
		OMText zipContent = _factory.createOMText(base64Enc, "application/zip",
				true);

		zipElmt.addChild(zipContent);

		msg = client.buildMessage("deploy", new String[] { "name", "package" },
				new Object[] { "test2", zipElmt });

		OMElement result = null;
		try {
			result = client.send(msg, ProcessManagementUI.getDefault().getPreferenceStore().getString(IProcessManagementConstants.PREF_ODE_URL)+EPR_DEPLOYMENT_SERVICE);
		} catch (AxisFault e) {
			MessageDialog.openError(Display.getCurrent().getActiveShell(),
					"Deployment caused an exception", e.getMessage());
		}
		OMElement part = result.getFirstChildWithName(new QName(null, "name"));
		return part.getText();
	}

	public static void undeployProcess(String packageName) {
		ServiceClientUtil client = new ServiceClientUtil();
		OMElement msg = client.buildMessage("undeploy",
				new String[] { "packageName" }, new String[] { packageName });

		OMElement result = null;
		try {
			result = client.send(msg, ProcessManagementUI.getDefault().getPreferenceStore().getString(IProcessManagementConstants.PREF_ODE_URL)+EPR_DEPLOYMENT_SERVICE);
		} catch (AxisFault e) {
			MessageDialog.openError(Display.getCurrent().getActiveShell(),
					"Undeployment caused an exception", e.getMessage());
		}
	}
}
