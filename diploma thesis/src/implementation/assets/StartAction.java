...

	public void run(IAction arg0) {

		fEditor.refreshEditor();

		IExtensionRegistry reg = Platform.getExtensionRegistry();
		IConfigurationElement[] extensions =
			reg.getConfigurationElementsFor("org.eclipse.bpel.ui.bootware");
		for (int i = 0; i < extensions.length; i++) {
			IConfigurationElement element = extensions[i];
			IBootwarePlugin plugin =
				(IBootwarePlugin) element.createExecutableExtension("class");
			plugin.execute(); // Can be any method defined in IBootwarePlugin
		}

		// continue with original code
...
