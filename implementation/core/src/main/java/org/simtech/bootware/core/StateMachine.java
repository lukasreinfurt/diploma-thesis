package org.simtech.bootware.core;

import org.squirrelframework.foundation.fsm.StateMachineBuilderFactory;
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

	public StateMachine(EventBus eventBus) {
		this.eventBus = eventBus;
		UntypedStateMachineBuilder builder = StateMachineBuilderFactory.create(Machine.class);

		builder.transit().fromAny().toAny().onAny().callMethod("transition");

		builder.externalTransition().from("A").to("B").on(FSMEvent.Continue);
		builder.externalTransition().from("A").to("A'").on(FSMEvent.Abort);

		builder.externalTransition().from("B").to("C").on(FSMEvent.Continue);
		builder.externalTransition().from("B").to("B'").on(FSMEvent.Abort);

		stateMachine = builder.newStateMachine("A");
	}

	public void run() {
		stateMachine.start(10);
		while(true) {
			SimpleEvent event = new SimpleEvent();
			event.setMessage(stateMachine.getCurrentState().toString());
			eventBus.publish(event);
			stateMachine.fire(FSMEvent.Continue, 10);
			if (stateMachine.getCurrentState() == "C") {
				break;
			}
		}
		stateMachine.terminate(10);
	}

}
