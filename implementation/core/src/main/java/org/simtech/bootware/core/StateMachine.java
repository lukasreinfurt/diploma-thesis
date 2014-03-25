package org.simtech.bootware.core;

import java.io.File;

import org.squirrelframework.foundation.fsm.UntypedStateMachineImporter;
import org.squirrelframework.foundation.fsm.UntypedStateMachineBuilder;
import org.squirrelframework.foundation.fsm.UntypedStateMachine;
import org.squirrelframework.foundation.fsm.impl.AbstractUntypedStateMachine;
import org.squirrelframework.foundation.fsm.annotation.StateMachineParameters;

import org.simtech.bootware.core.events.SimpleEvent;

public class StateMachine {

	private UntypedStateMachine stateMachine;
	private EventBus eventBus;

	private enum FSMEvent {
		Continue, Abort
	}

	@StateMachineParameters(stateType=String.class, eventType=FSMEvent.class, contextType=Integer.class)
	static class Machine extends AbstractUntypedStateMachine {

		protected void transition(String from, String to, FSMEvent event, Integer context) {
			System.out.println("Transition from '" + from + "' to '" + to + "' on event '" + event + "'.");
		}

	}

	public StateMachine(EventBus eventBus, String path) {
		this.eventBus                      = eventBus;
		File importFile                    = new File(path);
		UntypedStateMachineBuilder builder = new UntypedStateMachineImporter().importDefinition(importFile);
		stateMachine                       = builder.newAnyStateMachine("A");
	}

	public void run() {
		stateMachine.start(10);
		while(true) {
			Object currentState = stateMachine.getCurrentState();
			SimpleEvent event   = new SimpleEvent();
			event.setMessage(currentState.toString());
			eventBus.publish(event);
			if (currentState.toString().equals("C")) {
				break;
			}
			stateMachine.fire(FSMEvent.Continue, 10);
		}
		stateMachine.terminate(10);
	}

	public void export() {
		System.out.println(stateMachine.exportXMLDefinition(true));
	}

}
