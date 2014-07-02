...

protected void unloadEventPlugins(final String from,
                                  final String to,
                                  final String fsmEvent) {
	try {
		pluginManager.unloadAllPlugins();
	}
	catch (UnloadPluginException e) {
		stateMachine.fire(StateMachineEvents.FAILURE);
	}
	stateMachine.fire(StateMachineEvents.SUCCESS);
}

...
