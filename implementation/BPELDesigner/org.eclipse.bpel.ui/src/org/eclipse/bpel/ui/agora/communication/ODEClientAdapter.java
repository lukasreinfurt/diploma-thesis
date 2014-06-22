package org.eclipse.bpel.ui.agora.communication;

import java.net.URL;

import org.eclipse.apache.ode.processmanagement.IProcessManagementConstants;
import org.eclipse.apache.ode.processmanagement.ProcessManagementUI;

public abstract class ODEClientAdapter {

	protected static URL getODEUrl(String wsdlPath) {
		String serverUrl = ProcessManagementUI.getDefault().getPreferenceStore().getString(IProcessManagementConstants.PREF_ODE_URL);
		
		URL url = null;

		try {
			url = new URL(serverUrl+wsdlPath);
		} catch (java.net.MalformedURLException e) {
			e.printStackTrace();
		}

		return url;
	}
}
