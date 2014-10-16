/**
 * 
 */
package org.eclipse.bpel.ui.agora.communication;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.eclipse.apache.ode.processmanagement.IProcessManagementConstants;
import org.eclipse.apache.ode.processmanagement.ProcessManagementUI;
import org.eclipse.bpel.model.Activity;
import org.eclipse.bpel.model.Flow;
import org.eclipse.bpel.model.OnMessage;
import org.eclipse.bpel.model.Pick;
import org.eclipse.bpel.model.Receive;
import org.eclipse.bpel.model.Sequence;
import org.eclipse.bpel.model.Variable;
import org.eclipse.bpel.ui.BPELMultipageEditorPart;
import org.eclipse.bpel.ui.BPELUIPlugin;
import org.eclipse.bpel.ui.agora.ode135.client.TPartnerLinkInfo;
import org.eclipse.bpel.ui.agora.ode135.client.TPartnerLinkRefList;
import org.eclipse.bpel.ui.agora.ode135.client.TSnapshotInfo;
import org.eclipse.bpel.ui.agora.ode135.client.TSnapshotInfoList;
import org.eclipse.bpel.ui.agora.ode135.client.TVariableInfo;
import org.eclipse.bpel.ui.agora.ode135.client.TVariableRefList;
import org.eclipse.bpel.ui.simtech.properties.FileOperations;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

/**
 * Realizes the ManagementAPI Client for all supported ODE versions.
 * 
 * @author aeichel
 * @author hahnml
 * @author tolevar
 * 
 */
public class ManagementAPIHandler {

	private static final int BUFFER = 4096;

	public static final String ODE_VERSION_111 = "ODE_Version_111";
	public static final String ODE_VERSION_134 = "ODE_Version_134";
	public static final String ODE_VERSION_135 = "ODE_Version_135";

	public static void suspendInstance(Long instanceID) {
		if (isODEVersion(ODE_VERSION_111)) {
			ODEClientAdapter111.suspendInstance(instanceID);
		} else if (isODEVersion(ODE_VERSION_135)) {
			ODEClientAdapter135.suspendInstance(instanceID);
		} else {
			ODEClientAdapter134.suspendInstance(instanceID);
		}
	}

	public static void resumeInstance(Long instanceID) {
		if (isODEVersion(ODE_VERSION_111)) {
			ODEClientAdapter111.resumeInstance(instanceID);
		} else if (isODEVersion(ODE_VERSION_135)) {
			ODEClientAdapter135.resumeInstance(instanceID);
		} else {
			ODEClientAdapter134.resumeInstance(instanceID);
		}
	}

	public static void iterateInInstance(Long instanceID, String act_xPath) {
		if (isODEVersion(ODE_VERSION_135)) {
			ODEClientAdapter135.iterateInInstance(instanceID, act_xPath);
		} else {
			MessageDialog
					.openWarning(
							Display.getCurrent().getActiveShell(),
							"Unsupported Operation for the currently used ODE version",
							"The ability to iterate parts of a process instance is only supported by ODE version 1.3.5 or above.");
		}
	}

	public static void iterateExtInInstance(Long instanceID, String act_xPath,
			String snapshot_xPath, Long version, TVariableRefList variables,
			TPartnerLinkRefList partnerLinks) {
		if (isODEVersion(ODE_VERSION_135)) {
			ODEClientAdapter135.iterateExtInInstance(instanceID, act_xPath,
					snapshot_xPath, version, variables, partnerLinks);
		} else {
			MessageDialog
					.openWarning(
							Display.getCurrent().getActiveShell(),
							"Unsupported Operation for the currently used ODE version",
							"The ability to iterate parts of a process instance is only supported by ODE version 1.3.5 or above.");
		}
	}

	public static void jumpToInInstance(Long instanceID, String act_xPath) {
		if (isODEVersion(ODE_VERSION_135)) {
			ODEClientAdapter135.jumpToActivityInInstance(instanceID, act_xPath);
		} else {
			MessageDialog
					.openWarning(
							Display.getCurrent().getActiveShell(),
							"Unsupported Operation for the currently used ODE version",
							"The ability to jump to an activity in a process instance is only supported by ODE version 1.3.5 or above.");
		}
	}

	public static void reexecuteInInstance(Long instanceID, String act_xPath,
			String snapshot_xPath, Long version) {
		if (isODEVersion(ODE_VERSION_135)) {
			ODEClientAdapter135.reexecuteInInstance(instanceID, act_xPath,
					snapshot_xPath, version);
		} else {
			MessageDialog
					.openWarning(
							Display.getCurrent().getActiveShell(),
							"Unsupported Operation for the currently used ODE version",
							"The ability to reexecute parts of a process instance is only supported by ODE version 1.3.5 or above.");
		}
	}

	public static void reexecuteExtInInstance(Long instanceID,
			String act_xPath, String snapshot_xPath, Long version,
			TVariableRefList variables, TPartnerLinkRefList partnerLinks) {
		if (isODEVersion(ODE_VERSION_135)) {
			ODEClientAdapter135.reexecuteExtInInstance(instanceID, act_xPath,
					snapshot_xPath, version, variables, partnerLinks);
		} else {
			MessageDialog
					.openWarning(
							Display.getCurrent().getActiveShell(),
							"Unsupported Operation for the currently used ODE version",
							"The ability to reexecute parts of a process instance is only supported by ODE version 1.3.5 or above.");
		}
	}

	public static TSnapshotInfoList getSnapshots(Long instanceID,
			String act_xPath) {
		if (isODEVersion(ODE_VERSION_135)) {
			return ODEClientAdapter135.getSnapshots(instanceID, act_xPath);
		} else {
			MessageDialog
					.openWarning(
							Display.getCurrent().getActiveShell(),
							"Unsupported Operation for the currently used ODE version",
							"The ability to get snapshot information of a process instance is only supported by ODE version 1.3.5 or above.");
		}

		return null;
	}

	public static TSnapshotInfo getSnapshotVersion(Long instanceID,
			String act_xPath) {
		if (isODEVersion(ODE_VERSION_135)) {
			return ODEClientAdapter135
					.getSnapshotVersion(instanceID, act_xPath);
		} else {
			MessageDialog
					.openWarning(
							Display.getCurrent().getActiveShell(),
							"Unsupported Operation for the currently used ODE version",
							"The ability to get snapshot information of a process instance is only supported by ODE version 1.3.5 or above.");
		}

		return null;
	}

	public static List<TVariableInfo> getSnapshotVariables(Long instanceID,
			Long snapshotID) {
		if (isODEVersion(ODE_VERSION_135)) {
			return ODEClientAdapter135.getSnapshotVariables(instanceID,
					snapshotID);
		} else {
			MessageDialog
					.openWarning(
							Display.getCurrent().getActiveShell(),
							"Unsupported Operation for the currently used ODE version",
							"The ability to get snapshot variables of a process instance is only supported by ODE version 1.3.5 or above.");
		}

		return null;
	}

	public static List<TPartnerLinkInfo> getSnapshotPartnerLinks(
			Long instanceID, Long snapshotID) {
		if (isODEVersion(ODE_VERSION_135)) {
			return ODEClientAdapter135.getSnapshotPartnerLinks(instanceID,
					snapshotID);
		} else {
			MessageDialog
					.openWarning(
							Display.getCurrent().getActiveShell(),
							"Unsupported Operation for the currently used ODE version",
							"The ability to get snapshot partnerLinks of a process instance is only supported by ODE version 1.3.5 or above.");
		}

		return null;
	}

	public static void terminateInstance(Long instanceID) {
		if (isODEVersion(ODE_VERSION_111)) {
			ODEClientAdapter111.terminateInstance(instanceID);
		} else if (isODEVersion(ODE_VERSION_135)) {
			ODEClientAdapter135.terminateInstance(instanceID);
		} else {
			ODEClientAdapter134.terminateInstance(instanceID);
		}
	}

	public static void finishInstance(Long instanceID) {
		if (isODEVersion(ODE_VERSION_135)) {
			ODEClientAdapter135.finishInstance(instanceID);
		} else {
			MessageDialog
					.openWarning(
							Display.getCurrent().getActiveShell(),
							"Unsupported Operation for the currently used ODE version",
							"The ability to finish the execution of a artificially kept alive process instance is only supported by ODE version 1.3.5 or above.");
		}
	}

	public static ByteArrayOutputStream zipFolder(File inFolder) {
		ByteArrayOutputStream dataOut = new ByteArrayOutputStream(BUFFER);

		try {
			// compress outfile stream
			ZipOutputStream out = new ZipOutputStream(dataOut);

			// writing stream
			BufferedInputStream in = null;

			byte[] data = new byte[BUFFER];
			String files[] = inFolder.list(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {
					boolean accept = true;
					// We don't want some files of eclipse in the zip
					if (name.endsWith(".settings") || name.endsWith(".project")
							|| name.endsWith(".log")) {
						accept = false;
					}
					return accept;
				}
			});

			for (int i = 0; i < files.length; i++) {
				// System.out.println("Adding: " + files[i]);
				in = new BufferedInputStream(new FileInputStream(
						inFolder.getPath() + "/" + files[i]), BUFFER);

				out.putNextEntry(new ZipEntry(files[i])); // write data header
				// (name, size, etc)
				int count;
				while ((count = in.read(data, 0, BUFFER)) != -1) {
					out.write(data, 0, count);
				}
				out.closeEntry(); // close each entry

				in.close();
			}
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dataOut;
	}

	public static String deployProcess(IPath path) throws IOException,
			ServiceException {
		if (isODEVersion(ODE_VERSION_111)) {
			return ODEClientAdapter111.deployProcess(path);
		} else if (isODEVersion(ODE_VERSION_135)) {
			return ODEClientAdapter135.deployProcess(path);
		} else {
			return ODEClientAdapter134.deployProcess(path);
		}
	}

	public static String deployNewVersionOfProcess(IPath path, Long instanceID)
			throws ServiceException, IOException {
		if (isODEVersion(ODE_VERSION_135)) {
			return ODEClientAdapter135.deployNewVersionOfProcess(path,
					instanceID);
		} else {
			MessageDialog
					.openWarning(
							Display.getCurrent().getActiveShell(),
							"Unsupported Operation for the currently used ODE version",
							"The ability to deploy a new version of a currently running process instance is only supported by ODE version 1.3.5 or above.");

			return null;
		}
	}

	public static void undeployProcess(String packageName) {
		if (isODEVersion(ODE_VERSION_111)) {
			ODEClientAdapter111.undeployProcess(packageName);
		} else if (isODEVersion(ODE_VERSION_135)) {
			try {
				ODEClientAdapter135.undeployProcess(packageName);
			} catch (Exception e) {
				MessageDialog.openError(Display.getCurrent().getActiveShell(),
						"Undeploy caused an exception", e.getMessage());
			}
		} else {
			ODEClientAdapter134.undeployProcess(packageName);
		}
	}

	/**
	 * Sends the given message to the given operation of a Web service
	 * represented by the given WSDL. The method is restricted as it takes only
	 * the first binding and service found in the WSDL. If there are several
	 * bindings and/or services defined, this might cause problems. Currently,
	 * messages are only sent in a fire-and-forget manner, i.e. only one-way
	 * operations are supported properly.
	 * 
	 * @param wsdl
	 *            the interface definition of the Web service
	 * @param message
	 *            the message to send to the service in XML string format.
	 * @param operation
	 *            the operation the message should be sent to
	 * @param activityName
	 *            name of the activity that implements the input part of the
	 *            operation to invoke. Can be left empty since it is only
	 *            important for a correct error message.
	 * @author sonntamo
	 */
	public static void invokeWS(Definition wsdl, String message,
			String operation, String activityName, boolean isTwoWay) {
		String epr = "";
		Map serviceMap = wsdl.getServices();
		Collection serviceCol = serviceMap.values();

		// Get SOAP action URI from the binding. We assume there is only 1
		// binding.
		String soapAction = "";
		Map bindingMap = wsdl.getBindings();
		Collection bindingCol = bindingMap.values();

		// Get the first binding.
		Binding binding = (Binding) bindingCol.iterator().next();

		// Iterate over all binding operations.
		for (Object opObj : binding.getBindingOperations()) {
			if (opObj instanceof BindingOperation) {
				BindingOperation bindOp = (BindingOperation) opObj;
				if (bindOp.getName().equals(operation)) {

					// We found the operation. Now get the SOAP action.
					SOAPOperation soapOp = (SOAPOperation) bindOp
							.getExtensibilityElements().iterator().next();
					soapAction = soapOp.getSoapActionURI();
				}
			}
		}

		// We assume that there is only one service
		Service service = (Service) serviceCol.iterator().next();

		// We assume that there is only one port
		Port port = (Port) service.getPorts().values().iterator().next();

		// We assume that the port has a SOAP address
		SOAPAddress soapAddress = (SOAPAddress) port.getExtensibilityElements()
				.iterator().next();
		epr = soapAddress.getLocationURI();

		// build the message
		ServiceClient client;

		try {

			// create and configure client
			client = new ServiceClient();
			Options options = new Options();
			EndpointReference target = new EndpointReference(epr);
			options.setTo(target);
			client.setOptions(options);
			options.setAction(soapAction);

			// create message from XML string
			StringReader sr = new StringReader(message);
			XMLInputFactory xif = XMLInputFactory.newInstance();
			XMLStreamReader reader = xif.createXMLStreamReader(sr);
			StAXOMBuilder builder = new StAXOMBuilder(reader);
			OMElement msg = builder.getDocumentElement();

			// call the service
			client.fireAndForget(msg);

		} catch (Exception e) {
			System.out.println(e.getMessage());
			MessageDialog.openError(Display.getCurrent().getActiveShell(),
					"Invocation of activity \""
							+ activityName
							+ "\" failed. \n"
							+ ""
							+ "Invocation details:\n\n"
							+ "Operation: "
							+ operation
							+ "\nPort Type: "
							+ port.getBinding().getPortType().getQName()
									.toString() + "\nAddress: " + epr,
					e.getMessage());
		}
	}

	public static void invokeWS(BPELMultipageEditorPart editor,
			List<String> strings, List<Variable> variableList) {
		// @hahnml: Check if a SOAP message file exists for the current project
		if (editor.getEditorFile().getParent().getFile(new Path("start.soap"))
				.exists()) {
			invokeWSWithExternalMessage(editor);
		} else {
			invokeWSWithParameterDialogMessage(editor, strings, variableList);
		}
	}

	/**
	 * Invokes a BPEL process.
	 * <p>
	 * Supported bindings:
	 * <ul>
	 * <li>SOAP RPC</li>
	 * <li>SOAP document/literal</li>
	 * </ul>
	 * </p>
	 * <p>
	 * Restrictions:
	 * <ul>
	 * <li>Only one service in the WSDL</li>
	 * <li>Only one port in the service</li>
	 * <li>Only one create-instance-activity in the BPEL</li>
	 * <li>If the create-instance-activity is a pick activity, then the first
	 * onMessage is selected</li>
	 * </ul>
	 * </p>
	 * 
	 * @param editor
	 *            the BPEL editor
	 */
	private static void invokeWSWithParameterDialogMessage(
			BPELMultipageEditorPart editor, List<String> strings,
			List<Variable> variableList) {

		String epr = "";
		Definition wsdl = editor.getDesignEditor().getArtifactsDefinition();
		Map serviceMap = wsdl.getServices();
		Collection serviceCol = serviceMap.values();

		// We assume that there is only one service
		Service service = (Service) serviceCol.iterator().next();

		// We assume that there is only one port
		Port port = (Port) service.getPorts().values().iterator().next();

		Binding binding = port.getBinding();
		SOAPBinding soapBinding = (SOAPBinding) port.getBinding()
				.getExtensibilityElements().iterator().next();

		// We assume that the port has a SOAP address
		SOAPAddress soapAddress = (SOAPAddress) port.getExtensibilityElements()
				.iterator().next();
		epr = soapAddress.getLocationURI();

		// @hahnml: Take the SOAP address and change the IP and port to the
		// values specified in the Eclipse preferences
		epr = changeToCorrectOdeAddress(epr);

		String wsdlNS = wsdl.getTargetNamespace();
		String method = "";
		javax.wsdl.Message inputMsg = null;

		// We are now searching for the create instance activity of the process
		// to get the operation name and the input message
		Activity processAct = editor.getProcess().getActivity();
		EList<Activity> activities = null;
		if (processAct instanceof Sequence) {

			// it's a sequence
			Sequence seq = (Sequence) processAct;
			activities = seq.getActivities();
		} else {

			// it's a flow
			Flow flo = (Flow) processAct;
			activities = flo.getActivities();

		}

		// we assume only a single create instance activity
		if (activities != null)
			for (Activity act : activities) {

				// only Receive or Pick activities can create a new process
				// instance
				if (act instanceof Receive) {
					Receive rec = (Receive) act;
					if (rec.getCreateInstance()) {
						method = rec.getOperation().getName();
						inputMsg = rec.getOperation().getInput().getMessage();
						break;
					}
				} else if (act instanceof Pick) {
					Pick pic = (Pick) act;
					if (pic.getCreateInstance()) {

						// we assume that the pick has only one onMessage
						for (OnMessage onMsg : pic.getMessages()) {
							method = onMsg.getOperation().getName();
							inputMsg = onMsg.getOperation().getInput()
									.getMessage();
							break;
						}
					}
				}
			}

		// get the message parts of the input message
		ArrayList<String> partNames = new ArrayList<String>();
		Iterator partsIt = inputMsg.getParts().values().iterator();
		while (partsIt.hasNext()) {
			Part part = (Part) partsIt.next();
			if (part.getTypeName() != null) {

				// it's a typed part
				partNames.add(part.getName());
			} else if (part.getElementName() != null) {

				// it's an element part
				partNames.add(part.getElementName().getLocalPart());
			}
		}
		String[] params = partNames.toArray(new String[0]);
		String[] values = params;

		// build the message
		ServiceClient client;
		try {
			client = new ServiceClient();

			Options options = new Options();
			EndpointReference target = new EndpointReference(epr);
			options.setTo(target);
			client.setOptions(options);

			OMElement root = null;

			BindingOperation bindingOp = null;
			SOAPOperation soapOp = null;
			for (Object bindingOpObj : binding.getBindingOperations()) {
				bindingOp = (BindingOperation) bindingOpObj;
				if (bindingOp.getName().equals(method)) {

					// set the soap operation
					soapOp = (SOAPOperation) bindingOp
							.getExtensibilityElements().iterator().next();
				}
			}

			String style = "";
			if (soapOp.getStyle() != null && !soapOp.getStyle().equals("")) {
				style = soapOp.getStyle();
			} else {
				style = soapBinding.getStyle();
			}
			if (style.equals("rpc")) {
				if (variableList == null) {
					// Create a dummy message
					OMFactory _factory = OMAbstractFactory.getOMFactory();
					OMNamespace pmns = _factory
							.createOMNamespace(wsdlNS, "ns1");
					root = _factory.createOMElement(method, pmns);
					for (int m = 0; m < params.length; m++) {
						OMElement omelmt = _factory.createOMElement(params[m],
								null);
						if (values[m] == null)
							omelmt.setText("");
						else if (values[m] instanceof String)
							omelmt.setText((String) values[m]);
						root.addChild(omelmt);
					}
				} else {
					root = rpcSoapBody(strings, variableList, wsdlNS, method);
				}
			} else {
				root = literalSoapBody(strings, variableList, wsdlNS, method,
						params, client, root, bindingOp, soapOp);
			}

			addHeaderWaitingTime(client);

			// @sonntamo
			addHeaderMetaData(client, editor);

			client.fireAndForget(root);

		} catch (AxisFault e1) {
			e1.printStackTrace();
		}
	}

	// @hahnml: New method to change the address of ODE contained in the process
	// model service addresses according to the value
	// specified in the preferences
	private static String changeToCorrectOdeAddress(String epr) {
		String result = epr;
		
		// Get only the suffix of the process service address (EPR without ODE address, e.g. http://localhost:8080/ode/processes/SimTechTestService -> /processes/SimTechTestService)
		String[] split = epr.split("http://\\w+:\\d{4}/ode");
		
		// Retrieve the correct address of ODE from the preference store of the
		// process management plugin
		String odeAddress = ProcessManagementUI.getDefault()
				.getPreferenceStore()
				.getString(IProcessManagementConstants.PREF_ODE_URL);
		
		if (odeAddress != null && !odeAddress.isEmpty() && split.length == 2) {
			result = odeAddress + split[1];
		}

		return result;
	}

	/**
	 * Builds a soap body if the rpc styl is chosen and returns the root
	 * OMElement
	 * 
	 * @param wsdlNS
	 * @param method
	 * @param params
	 * @param values
	 * @return root - OMElement
	 */
	private static OMElement rpcSoapBody(List<String> strings,
			List<Variable> variableList, String wsdlNS, String method) {
		OMElement root;
		// RPC binding
		OMFactory _factory = OMAbstractFactory.getOMFactory();
		OMNamespace pmns = _factory.createOMNamespace(wsdlNS, "ns1");
		root = _factory.createOMElement(method, pmns);
		if (strings != null && strings.size() > 0 && variableList != null
				&& variableList.size() > 0) {
			OMElement omelmt = null;
			for (int m = 0; m < strings.size(); m++) {
				omelmt = _factory.createOMElement(
						variableList.get(m).getName(), pmns);
				omelmt.setText(strings.get(m));
				root.addChild(omelmt);
			}
		} else {
			// not implemented yet
		}
		return root;
	}

	/**
	 * Builds a soap body if the literal style is chosen and returns the root
	 * OMElement
	 * 
	 * @param strings
	 * @param variableList
	 * @param wsdlNS
	 * @param method
	 * @param params
	 * @param client
	 * @param root
	 * @param bindingOp
	 * @param soapOp
	 * @return root - OMElement
	 */
	private static OMElement literalSoapBody(List<String> strings,
			List<Variable> variableList, String wsdlNS, String method,
			String[] params, ServiceClient client, OMElement root,
			BindingOperation bindingOp, SOAPOperation soapOp) {
		// it's document style binding
		SOAPBody soapBody = (SOAPBody) bindingOp.getBindingInput()
				.getExtensibilityElements().iterator().next();
		if (soapBody.getUse().equals("literal")) {

			// it's literal
			OMFactory _factory = OMAbstractFactory.getOMFactory();
			OMNamespace pmns = _factory.createOMNamespace(wsdlNS, "ns1");
			root = _factory.createOMElement(params[0], pmns);

			if (strings != null && strings.size() > 0 && variableList != null
					&& variableList.size() > 0) {
				OMElement parameters = _factory.createOMElement("parameters",
						pmns);
				root.addChild(parameters);
				OMElement element = null;
				for (int i = 0; i < strings.size(); i++) {
					element = _factory.createOMElement("parameter", pmns);
					element.addAttribute("name", variableList.get(i).getName(),
							pmns);
					element.setText(strings.get(i));
					parameters.addChild(element);
				}
			} else {
				root.setText(params[0]);
			}

			// set the soap action so that the engine knows which operation we
			// mean
			String soapAction = soapOp.getSoapActionURI();
			if (soapAction == null || soapAction.equals("")) {
				client.getOptions().setAction("urn:" + method);
			} else {
				client.getOptions().setAction(soapAction);
			}
		} else {

			// it's encoded
		}
		return root;
	}

	private static void invokeWSWithExternalMessage(
			BPELMultipageEditorPart editor) {

		// Read the SOAP file content
		String filePath = editor.getEditorFile().getParent()
				.getFile(new Path("start.soap")).getLocation().toOSString();
		OMElement documentElement = null;
		try {
			documentElement = new StAXOMBuilder(filePath).getDocumentElement();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (XMLStreamException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		String epr = "";
		Definition wsdl = editor.getDesignEditor().getArtifactsDefinition();
		Map serviceMap = wsdl.getServices();
		Collection serviceCol = serviceMap.values();

		// We assume that there is only one service
		Service service = (Service) serviceCol.iterator().next();

		// We assume that there is only one port
		Port port = (Port) service.getPorts().values().iterator().next();

		// We assume that the port has a SOAP address
		SOAPAddress soapAddress = (SOAPAddress) port.getExtensibilityElements()
				.iterator().next();
		epr = soapAddress.getLocationURI();

		// @hahnml: Take the SOAP address and change the IP and port to the
		// values specified in the Eclipse preferences
		epr = changeToCorrectOdeAddress(epr);

		// build the message
		ServiceClient client;

		try {
			client = new ServiceClient();

			Options options = new Options();
			EndpointReference target = new EndpointReference(epr);
			options.setTo(target);
			client.setOptions(options);

			OMElement root = documentElement;

			// Get the waiting time from the preference store and attach it as a
			// header of the SOAP message
			String waitingTime = BPELUIPlugin.INSTANCE.getPreferenceStore()
					.getString("INSTANCE_WAITING_TIME");

			addHeaderWaitingTime(client);

			// @sonntamo
			addHeaderMetaData(client, editor);

			client.fireAndForget(root);

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}

	/**
	 * 
	 * @param client
	 * @param editor
	 * @throws AxisFault
	 * 
	 * @author sonntamo
	 */
	private static void addHeaderMetaData(ServiceClient client,
			BPELMultipageEditorPart editor) throws AxisFault {

		// get meta data for the process
		IContainer container = editor.getEditorFile().getParent();
		HashMap<String, String> metaData = FileOperations
				.loadPropertiesFromDD(container);

		// create header
		OMElement metaDataElement;
		OMFactory _factory = OMAbstractFactory.getOMFactory();
		OMNamespace pmns = _factory.createOMNamespace(
				"http://simtech.uni-stuttgart.de", "simTech");
		metaDataElement = _factory.createOMElement("metaData", pmns);

		// create entries and add to header
		for (String key : metaData.keySet()) {
			String value = metaData.get(key);
			OMElement mdProperty = _factory.createOMElement("mdProperty", pmns);
			OMAttribute nameAttr = _factory
					.createOMAttribute("name", pmns, key);
			OMAttribute valueAttr = _factory.createOMAttribute("value", pmns,
					value);
			mdProperty.addAttribute(nameAttr);
			mdProperty.addAttribute(valueAttr);
			metaDataElement.addChild(mdProperty);
		}

		// add header
		client.addHeader(metaDataElement);
	}

	private static void addHeaderWaitingTime(ServiceClient client)
			throws AxisFault {

		// Get the waiting time from the preference store and attach it as a
		// header of the SOAP message
		String waitingTime = BPELUIPlugin.INSTANCE.getPreferenceStore()
				.getString("INSTANCE_WAITING_TIME");

		// Check if we got a real Long number, that we don't get an
		// exception in the WfMS
		Long waitingTimeLong = 0L;
		try {
			waitingTimeLong = Long.valueOf(waitingTime);
		} catch (NumberFormatException e) {
			MessageDialog
					.openError(
							Display.getCurrent().getActiveShell(),
							"Number Format Exception",
							"The specified waiting time at the SimTech preference page is no valid number. Please check the SimTech preferences to solve the problem.\n The process was invoked with 0ms waiting time.");
		}

		client.addStringHeader(new QName("http://simtech.uni-stuttgart.de",
				"invokeWaitingTime", "simTech"), waitingTimeLong.toString());
	}

	public static boolean isODEVersion(String version) {
		boolean isVersion = ProcessManagementUI.getDefault()
				.getPreferenceStore()
				.getString(IProcessManagementConstants.PREF_ODE_VERSION)
				.equals(version);

		return isVersion;
	}

}
