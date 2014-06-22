package org.eclipse.bpel.ui.agora.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axis.AxisFault;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Process_Deployed;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Process_Undeployed;
import org.eclipse.apache.ode.processmanagement.compare.ProcessModelComparator;
import org.eclipse.apache.ode.processmanagement.compare.ProcessModelDeployData;
import org.eclipse.bpel.model.Process;
import org.eclipse.bpel.model.Variable;
import org.eclipse.bpel.model.debug.XPathMapProvider;
import org.eclipse.bpel.ui.BPELMultipageEditorPart;
import org.eclipse.bpel.ui.agora.communication.EngineProcessOutputEventHandler;
import org.eclipse.bpel.ui.agora.communication.ManagementAPIHandler;
import org.eclipse.bpel.ui.agora.debug.DebugManager;
import org.eclipse.bpel.ui.agora.provider.MonitoringProvider;
import org.eclipse.bpel.ui.parameters.handler.ParameterHandler;
import org.eclipse.bpel.ui.uiextensionmodel.ProcessExtension;
import org.eclipse.bpel.ui.util.ModelHelper;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.simtech.workflow.ode.auditing.communication.messages.InstanceInformation;

/**
 * @author hahnml
 * 
 */
public class ProcessManager {

	private BPELMultipageEditorPart editor = null;

	private boolean isDeployed = false;
	private String packageName = null;
	private QName processName = null;
	private Long processVersion = null;

	private HashMap<InstanceInformation, MonitorManager> instanceManagerMap = new HashMap<InstanceInformation, MonitorManager>();
	private List<MonitorManager> instanceManagers = new ArrayList<MonitorManager>();

	private Process process = null;

	private ParameterHandler parameterHandler = null;

	private StateBuffer stateBuffer = null;

	private DebugManager debugManager = null;

	private EngineProcessOutputEventHandler eventHandler = null;

	private ProcessManager(BPELMultipageEditorPart editor) {
		super();
		this.editor = editor;
		this.process = this.editor.getProcess();
		this.stateBuffer = new StateBuffer();
		this.parameterHandler = new ParameterHandler();
		this.debugManager = new DebugManager();
		this.eventHandler = new EngineProcessOutputEventHandler(this);
	}

	public static ProcessManager create(BPELMultipageEditorPart editor) {
		return new ProcessManager(editor);
	}

	public BPELMultipageEditorPart getEditor() {
		return this.editor;
	}

	public MonitorManager createMonitorManager(ProcessManager processManager,
			InstanceInformation information) {
		MonitorManager manager = MonitorManager.create(processManager,
				information);

		this.instanceManagerMap.put(information, manager);
		this.instanceManagers.add(manager);

		return manager;
	}

	public void deleteMonitorManager(MonitorManager monitorManager) {
		monitorManager.delete();

		this.instanceManagerMap.remove(monitorManager.getInstanceInformation());
		this.instanceManagers.remove(monitorManager);
	}

	public MonitorManager getLastStartedInstance() {
		if (this.instanceManagers.size() > 0) {
			return this.instanceManagers.get(this.instanceManagers.size() - 1);
		}

		return null;
	}

	public void delete() {
		this.instanceManagerMap.clear();

		for (MonitorManager manager : this.instanceManagers) {
			manager.delete();
		}

		this.instanceManagers.clear();

		XPathMapProvider.getInstance().deleteXPathMap(this.process);
	}

	public StateBuffer getStateBuffer() {
		return this.stateBuffer;
	}

	public Process getProcess() {
		return this.process;
	}

	public ParameterHandler getParameterHandler() {
		return this.parameterHandler;
	}

	public DebugManager getDebugManager() {
		return this.debugManager;
	}

	public boolean isDeployed() {
		return isDeployed;
	}

	public void setDeployed(Object message) {
		if (message instanceof Process_Deployed) {
			Process_Deployed msg = (Process_Deployed) message;

			this.isDeployed = true;
			this.processName = msg.getProcessName();
			this.processVersion = msg.getVersion();

			// Update the process name and version in the *.bpelex file
			ProcessExtension ext = (ProcessExtension) ModelHelper
					.getExtension(this.process);
			ext.setProcessName(this.processName.toString());
			ext.setProcessVersion(this.processVersion);

			// Save the changes
			MonitoringProvider.saveBPELExFile(editor);

		} else if (message instanceof Process_Undeployed) {
			this.isDeployed = false;
			this.processName = null;
			this.processVersion = null;
			this.packageName = null;
			if (getLastStartedInstance() != null) {
				getLastStartedInstance().getInstanceInformation()
						.setPackageName(this.packageName);
			}
		}
	}

	public QName getProcessName() {
		return processName;
	}

	public Long getProcessVersion() {
		return processVersion;
	}

	public EngineProcessOutputEventHandler getEventHandler() {
		return this.eventHandler;
	}

	public void prepareAndStartProcessInstance(List<List<String>> list,
			List<Variable> variableList) {

		if (!isDeployed) {
			// Create a comparator and check if the model is deployed already
			ProcessModelComparator comparator = new ProcessModelComparator(
					this.process);

			// Check if process models with the name of the process are deployed
			if (comparator.isProcessModelPotentiallyDeployed()) {
				ProcessModelDeployData data = comparator
						.getDeployedProcessModel();
				if (data != null) {
					// An equivalent process model was found to start a new
					// instance
					// Set the data to the ProcessManager and its MonitorManager
					this.isDeployed = true;
					this.packageName = data.getPackageName();
					this.processName = data.getProcessName();
					this.processVersion = data.getVersion();
					
					// Update the process name and version in the *.bpelex file
					ProcessExtension ext = (ProcessExtension) ModelHelper
							.getExtension(this.process);
					ext.setProcessName(this.processName.toString());
					ext.setProcessVersion(this.processVersion);

					// Save the changes
					MonitoringProvider.saveBPELExFile(editor);

					getLastStartedInstance().getInstanceInformation()
							.setPackageName(data.getPackageName());
					getLastStartedInstance().getInstanceInformation()
							.setProcessVersion(data.getVersion());
					getLastStartedInstance().getInstanceInformation()
							.setProcessName(data.getProcessName());
					getLastStartedInstance().getInstanceInformation()
							.setTimestamp(data.getDeployDate());
				} else {
					// No equivalent process model found
					this.isDeployed = deployProcessModel();
				}
			} else {
				this.isDeployed = deployProcessModel();
			}

			comparator.delete();
			comparator = null;
		}

		if (this.isDeployed) {
			if (list != null && variableList != null) {
				for (List<String> strings : list) {
					this.getLastStartedInstance().startWorkflow(variableList,
							strings);
				}
			} else {
				this.getLastStartedInstance().startWorkflow(null, null);
			}
		}
	}

	private boolean deployProcessModel() {
		boolean doExec = true;
		try {
			this.packageName = ManagementAPIHandler.deployProcess(this.editor
					.getEditorFile().getLocation());
			getLastStartedInstance().getInstanceInformation().setPackageName(
					this.packageName);
		} catch (Exception e) {
			doExec = false;

			String msg = "The deployment of the process model failed."
					+ "\n\nReason:\n\n";
			if (e instanceof AxisFault) {
				AxisFault af = (AxisFault) e;
				msg += af.getFaultCode().toString() + "\n\n"
						+ af.getFaultString();
			} else {
				msg += e.getMessage();
			}
			MessageDialog.openError(Display.getCurrent().getActiveShell(),
					"Deployment failed.", msg);
		}

		return doExec;
	}

	public void undeployProcessModel() {
		if (this.packageName != null) {
			ManagementAPIHandler.undeployProcess(this.packageName);
			this.packageName = null;

			if (getLastStartedInstance() != null) {
				getLastStartedInstance().getInstanceInformation()
						.setPackageName(this.packageName);
			}
		}
	}

	public String getPackageName() {
		return this.packageName;
	}
}
