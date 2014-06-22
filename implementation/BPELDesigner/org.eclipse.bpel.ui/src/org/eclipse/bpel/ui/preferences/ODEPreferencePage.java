package org.eclipse.bpel.ui.preferences;

import org.eclipse.apache.ode.processmanagement.IProcessManagementConstants;
import org.eclipse.apache.ode.processmanagement.ProcessManagementUI;
import org.eclipse.bpel.ui.agora.communication.ManagementAPIHandler;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class ODEPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	public ODEPreferencePage() {
		super(GRID);
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(ProcessManagementUI.getDefault().getPreferenceStore());
	}

	@Override
	protected void createFieldEditors() {
		addField(new StringFieldEditor(IProcessManagementConstants.PREF_ODE_URL,
				"ODE server url:", getFieldEditorParent()));
		addField(new RadioGroupFieldEditor(
				IProcessManagementConstants.PREF_ODE_VERSION,
				"ODE version",
				1,
				new String[][] {
						{ "Version 1.1.1", ManagementAPIHandler.ODE_VERSION_111 },
						{ "Version 1.3.4", ManagementAPIHandler.ODE_VERSION_134 },
						{ "Version 1.3.5", ManagementAPIHandler.ODE_VERSION_135 } },
				getFieldEditorParent(), true));
	}
}
