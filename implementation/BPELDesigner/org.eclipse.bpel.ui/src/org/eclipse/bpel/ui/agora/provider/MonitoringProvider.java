package org.eclipse.bpel.ui.agora.provider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.eclipse.apache.ode.processmanagement.compare.ProcessModelComparator;
import org.eclipse.apache.ode.processmanagement.compare.ProcessModelDeployData;
import org.eclipse.bpel.model.Process;
import org.eclipse.bpel.ui.BPELMultipageEditorPart;
import org.eclipse.bpel.ui.agora.communication.EngineOutputMessageDispatcher;
import org.eclipse.bpel.ui.agora.debug.views.BreakpointManagementView;
import org.eclipse.bpel.ui.agora.manager.MonitorManager;
import org.eclipse.bpel.ui.agora.manager.ProcessManager;
import org.eclipse.bpel.ui.agora.views.AuditingView;
import org.eclipse.bpel.ui.agora.views.EventMessage;
import org.eclipse.bpel.ui.uiextensionmodel.ProcessExtension;
import org.eclipse.bpel.ui.util.ModelHelper;
import org.eclipse.swt.widgets.Display;
import org.simtech.workflow.ode.auditing.communication.messages.InstanceInformation;

/**
 * The <b>MonitoringProvider</b> holds the started instances with the
 * corresponding Monitor Manager. Every instance has its own Monitor Manager and
 * manages the life cycle of an instance. Also the <b>MonitoringProvider</b>
 * holds the process current instance. That is the last started instance.
 * 
 * @author tolevar
 * @author hahnml
 * 
 */
public class MonitoringProvider {

	private static MonitoringProvider provider = null;

	private EngineOutputMessageDispatcher dispatcher = null;

	private Map<BPELMultipageEditorPart, ProcessManager> processManagers = null;

	private AuditingView auditingView;

	private BreakpointManagementView breakpointManagementView;

	private BPELMultipageEditorPart activeEditor;

	private MonitoringProvider() {
		processManagers = new HashMap<BPELMultipageEditorPart, ProcessManager>();
		this.dispatcher = new EngineOutputMessageDispatcher();
	}

	/**
	 * If an object of the <b>MonitoringProvider</b> exists return this object,
	 * if not create a new Monitoring Provider object.
	 * 
	 * @return provider - MonitoringProvider
	 */
	public static MonitoringProvider getInstance() {
		if (provider == null) {
			provider = new MonitoringProvider();
		}
		return provider;
	}

	public ProcessManager createProcessManager(BPELMultipageEditorPart editor) {
		ProcessManager manager = ProcessManager.create(editor);

		this.processManagers.put(editor, manager);

		// Check if a process name and version are stored in the process
		// extension (*.bpelex file)
		// If no data is available in the extension ask ODE if the opened model
		// exists. If an equal model exists, query its name and version to store
		// the data in the process extension.
		// This enables the "Show instance" functionality for process models
		// which are not executed locally.
		ProcessExtension processExtension = (ProcessExtension) ModelHelper
				.getExtension(editor.getProcess());

		if (processExtension != null) {
			String processName = processExtension.getProcessName();
			Long processVersion = processExtension.getProcessVersion();
			if (processName == null || processName.isEmpty()
					|| processVersion == null) {
				// Create a comparator and check if the model is deployed
				// already
				ProcessModelComparator comparator = new ProcessModelComparator(
						editor.getProcess());

				// Check if process models with the name of the process are
				// deployed
				if (comparator.isProcessModelPotentiallyDeployed()) {
					ProcessModelDeployData data = comparator
							.getDeployedProcessModel();
					if (data != null) {
						// An equivalent process model was found. Update the
						// data at
						// the process extension.
						processExtension.setProcessName(data.getProcessName()
								.toString());
						processExtension.setProcessVersion(data.getVersion());

						// Save the changes
						MonitoringProvider.saveBPELExFile(editor);
					}
				}
			}
		}

		return manager;
	}

	public ProcessManager getProcessManager(BPELMultipageEditorPart editor) {
		return this.processManagers.get(editor);
	}

	public ProcessManager getProcessManager(Process process) {
		ProcessManager result = null;

		for (ProcessManager manager : this.processManagers.values()) {
			if (process.equals(manager.getProcess())) {
				result = manager;
				break;
			}
		}

		return result;
	}

	public ProcessManager getProcessManager(QName processName,
			Long processVersion) {
		for (ProcessManager processManager : this.processManagers.values()) {

			if (processManager.getProcessName() != null
					&& processManager.getProcessVersion() != null) {
				if (processManager.getProcessName().equals(processName)
						&& processManager.getProcessVersion().equals(
								processVersion)) {
					return processManager;
				}
			}
		}

		return null;
	}

	public MonitorManager getMonitorManager(InstanceInformation information) {
		MonitorManager result = null;

		for (ProcessManager manager : this.processManagers.values()) {
			if (manager.getLastStartedInstance() != null) {
				if (manager.getLastStartedInstance().getInstanceInformation()
						.equals(information)) {
					result = manager.getLastStartedInstance();
					break;
				}
			}
		}

		return result;
	}

	public MonitorManager getMonitorManager(Long instanceID) {
		MonitorManager result = null;

		for (ProcessManager manager : this.processManagers.values()) {
			if (manager.getLastStartedInstance() != null) {
				if (manager.getLastStartedInstance().getInstanceInformation()
						.getInstanceID() != null) {
					if (manager.getLastStartedInstance()
							.getInstanceInformation().getInstanceID()
							.equals(instanceID)) {
						result = manager.getLastStartedInstance();
						break;
					}
				}
			}
		}

		return result;
	}

	public MonitorManager getMonitorManager(Process process) {
		MonitorManager result = null;

		ProcessManager manager = getProcessManager(process);
		if (manager != null) {
			result = manager.getLastStartedInstance();
		}

		return result;
	}

	public MonitorManager getMonitorManager(BPELMultipageEditorPart editor) {
		MonitorManager result = null;

		ProcessManager manager = getProcessManager(editor);
		if (manager != null) {
			result = manager.getLastStartedInstance();
		}

		return result;
	}

	public void deleteProcessManager(ProcessManager processManager) {
		this.processManagers.remove(processManager.getEditor());

		processManager.delete();
	}

	public void registerAuditingView(AuditingView auditingView) {
		this.auditingView = auditingView;
	}

	public void registerBreakpointView(
			BreakpointManagementView breakpointManagementView) {
		this.breakpointManagementView = breakpointManagementView;
	}

	public void changeActiveEditor(BPELMultipageEditorPart newActiveEditor) {
		this.activeEditor = newActiveEditor;

		ProcessManager processManager = getProcessManager(newActiveEditor);
		MonitorManager manager = processManager.getLastStartedInstance();

		if (this.auditingView != null) {
			if (manager != null) {
				// Update the MonitorManager in the AuditingView
				this.auditingView.setInputMonitorManager(manager);
			} else {
				// Initialize the AuditingView with an empty list
				this.auditingView.getViewer().setInput(
						new ArrayList<EventMessage>());
			}
		}

		if (this.breakpointManagementView != null) {
			this.breakpointManagementView
					.setInputMonitorManager(processManager);
		}

	}

	public MonitorManager getActiveMonitorManager() {
		return this.getProcessManager(this.activeEditor)
				.getLastStartedInstance();
	}

	public ProcessManager getActiveProcessManager() {
		return this.getProcessManager(this.activeEditor);
	}

	public BPELMultipageEditorPart getActiveEditor() {
		return activeEditor;
	}

	public static void saveBPELExFile(final BPELMultipageEditorPart editor) {
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						try {
							editor.getDesignEditor().getEditModelClient()
									.getExtensionsResourceInfo().save();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
			}

		});

		t.start();
		try {
			t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	public static void saveBPELExFile(final QName processName,
			final Long version, final MonitorManager manager) {
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						try {
							ProcessExtension ext = (ProcessExtension) ModelHelper
									.getExtension(manager.getProcess());
							ext.setProcessName(processName.toString());
							ext.setProcessVersion(version);

							manager.getEditor().getDesignEditor().getEditModelClient()
									.getExtensionsResourceInfo().save();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
			}

		});

		t.start();
		try {
			t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * Provides the {@link EngineOutputMessageDispatcher} instance for later registration, e.g. if the ActiveMQ URL is changed during runtime.
	 * 
	 * @return The {@link EngineOutputMessageDispatcher} instance.
	 */
	public EngineOutputMessageDispatcher getDispatcher() {
		return dispatcher;
	}
}
