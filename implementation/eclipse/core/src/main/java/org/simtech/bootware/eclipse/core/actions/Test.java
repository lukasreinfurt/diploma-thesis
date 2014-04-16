package org.simtech.bootware.eclipse.core.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import org.simtech.bootware.eclipse.core.ExtensionPointPlugin;

public class Test implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;

	public Test() {
	}

	public void run(IAction action) {
		String response = "";
		StringBuffer buffer = new StringBuffer();
		IExtensionRegistry reg = Platform.getExtensionRegistry();
		IConfigurationElement[] extensions = reg.getConfigurationElementsFor("org.simtech.bootware.eclipse.core");
		for (int i = 0; i < extensions.length; i++) {
			IConfigurationElement element = extensions[i];
			if (element.getAttribute("class") == null) {
				// handle
			} else {
				try {
					ExtensionPointPlugin plugin = (ExtensionPointPlugin) element.createExecutableExtension("class");
					response = plugin.execute();
				} catch (CoreException e) {
					response = e.toString();
				}
			}
			buffer.append(response);
			buffer.append('\n');
		}
		MessageDialog.openInformation(window.getShell(), "Extension Point Test", buffer.toString());
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
}
