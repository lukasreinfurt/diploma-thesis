...

	protected void connect(final String from,
	                       final String to,
	                       final String fsmEvent) {
		try {
			connection = communicationPlugin.connect(instance);
		}
		catch (ConnectConnectionException e) {
			stateMachine.fire(StateMachineEvents.FAILURE);
		}
		stateMachine.fire(StateMachineEvents.SUCCESS);
	}

...
