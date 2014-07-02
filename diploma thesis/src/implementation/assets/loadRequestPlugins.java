...

protected void loadRequestPlugins(final String from, final String to, final String fsmEvent) {
	try {
		infrastructurePlugin = pluginManager.loadPlugin(InfrastructurePlugin.class, infrastructurePluginPath + context.getInfrastructurePlugin());
		connectionPlugin     = pluginManager.loadPlugin(ConnectionPlugin.class, connectionPluginPath + context.getConnectionPlugin());
		payloadPlugin        = pluginManager.loadPlugin(PayloadPlugin.class, payloadPluginPath + context.getPayloadPlugin());
	}
	catch (LoadPluginException e) {
		e.printStackTrace();
		stateMachine.fire(StateMachineEvents.FAILURE);
	}
	stateMachine.fire(StateMachineEvents.DEPLOY);
	//stateMachine.fire(StateMachineEvents.UNDEPLOY);
}

...
