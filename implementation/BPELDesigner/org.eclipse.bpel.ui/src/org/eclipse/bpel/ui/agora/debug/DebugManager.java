package org.eclipse.bpel.ui.agora.debug;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import javax.xml.namespace.QName;

import org.apache.ode.bpel.extensions.comm.messages.engineIn.Complete_Activity;
import org.apache.ode.bpel.extensions.comm.messages.engineIn.Continue;
import org.apache.ode.bpel.extensions.comm.messages.engineIn.Continue_Loop;
import org.apache.ode.bpel.extensions.comm.messages.engineIn.Continue_Loop_Execution;
import org.apache.ode.bpel.extensions.comm.messages.engineIn.Finish_Loop_Execution;
import org.apache.ode.bpel.extensions.comm.messages.engineIn.IncomingMessageBase;
import org.apache.ode.bpel.extensions.comm.messages.engineIn.RegisterRequestMessage;
import org.apache.ode.bpel.extensions.comm.messages.engineIn.RegisterRequestMessage.Requested_Blocking_Events;
import org.apache.ode.bpel.extensions.comm.messages.engineIn.RequestRegistrationInformation;
import org.apache.ode.bpel.extensions.comm.messages.engineIn.Set_Link_State;
import org.apache.ode.bpel.extensions.comm.messages.engineIn.Start_Activity;
import org.apache.ode.bpel.extensions.comm.messages.engineIn.UnregisterRequestMessage;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.ActivityEventMessage;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Activity_Executed;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Activity_Faulted;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Activity_Ready;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Evaluating_TransitionCondition_Faulted;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.InstanceEventMessage;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.LinkEventMessage;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Link_Evaluated;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Loop_Condition_False;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Loop_Condition_True;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Loop_Iteration_Complete;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Scope_Compensating;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Scope_Complete_With_Fault;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Scope_Handling_Fault;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Scope_Handling_Termination;
import org.eclipse.bpel.common.ui.command.ICommandFramework;
import org.eclipse.bpel.debug.debugmodel.Breakpoint;
import org.eclipse.bpel.debug.debugmodel.BreakpointStateEnum;
import org.eclipse.bpel.debug.debugmodel.BreakpointTypeEnum;
import org.eclipse.bpel.debug.debugmodel.Breakpoints;
import org.eclipse.bpel.debug.debugmodel.Debug;
import org.eclipse.bpel.model.Activity;
import org.eclipse.bpel.model.BPELExtensibleElement;
import org.eclipse.bpel.model.Link;
import org.eclipse.bpel.model.Process;
import org.eclipse.bpel.model.debug.XPathMapProvider;
import org.eclipse.bpel.ui.BPELEditDomain;
import org.eclipse.bpel.ui.BPELEditor;
import org.eclipse.bpel.ui.agora.AgoraStates;
import org.eclipse.bpel.ui.agora.communication.JMSCommunication;
import org.eclipse.bpel.ui.agora.debug.commands.RemoveBreakpointCommand;
import org.eclipse.bpel.ui.agora.instances.InstanceHelper;
import org.eclipse.bpel.ui.agora.manager.MonitorManager;
import org.eclipse.bpel.ui.agora.manager.StateMachine;
import org.eclipse.bpel.ui.agora.manager.XPathMapper;
import org.eclipse.bpel.ui.agora.views.IViewListener;
import org.eclipse.bpel.ui.uiextensionmodel.InstanceState;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.simtech.workflow.ode.auditing.agora.BPELStates;
import org.w3c.dom.Element;

public class DebugManager {

	private boolean blockingRegistered;

	private BPELExtensibleElement selectedActivity;

	private MonitorManager manager = null;

	private HashMap<String, InstanceEventMessage> eventMessages = new HashMap<String, InstanceEventMessage>();

	private IViewListener viewListener;

	private List<Breakpoint> breakpoints = new ArrayList<Breakpoint>();
	private ICommandFramework commandFramework;
	private Debug debug = null;

	private Stack<String> blockedElementXPaths = new Stack<String>();

	private boolean isUpdatingBreakpoints = false;

	public void initialize(Debug debugModel, ICommandFramework iCommandFramework) {
		if (debugModel != null) {
			this.breakpoints.addAll(debugModel.getGlobalBreakpoints()
					.getBreakpoint());
			this.breakpoints.addAll(debugModel.getLocalBreakpoints()
					.getBreakpoint());

			this.debug = debugModel;
		}

		this.commandFramework = iCommandFramework;
	}

	public void setMonitorManager(MonitorManager manager) {
		this.manager = manager;
	}

	public List<Breakpoint> getAllBreakpoints() {
		return this.breakpoints;
	}

	public ICommandFramework getCommandFramework() {
		return this.commandFramework;
	}

	/**
	 * This method is used to register the view as listener.
	 * 
	 * @param listener
	 *            The view which should be listen on model changes.
	 */
	public void setViewListener(IViewListener listener) {
		this.viewListener = listener;
	}

	public void removeBreakpoint(Breakpoints breakpoints,
			Breakpoint currentBreakpoint) {

		if (currentBreakpoint.getState() != BreakpointStateEnum.BLOCKING) {
			getCommandFramework()
					.execute(
							new RemoveBreakpointCommand(breakpoints,
									currentBreakpoint));

			this.breakpoints.remove(currentBreakpoint);

			updateView();
		} else {
			MessageDialog
					.openInformation(
							Display.getCurrent().getActiveShell(),
							"Breakpoint is not removeable",
							"The breakpoint is currently blocking and can't be removed until the blocking event is released.");
		}
	}

	/**
	 * Clears the whole data of the model.
	 */
	public void clear() {
		this.breakpoints.clear();

		// Updating the display in the view
		updateView();
	}

	/**
	 * Refresh the whole data of the model.
	 */
	public void refresh() {
		this.breakpoints.clear();

		if (this.debug != null) {
			this.breakpoints.addAll(this.debug.getGlobalBreakpoints()
					.getBreakpoint());
			this.breakpoints.addAll(this.debug.getLocalBreakpoints()
					.getBreakpoint());
		}

		// Updating the display in the view
		updateView();
	}

	/**
	 * Forces the view which is registered as listener to update.
	 */
	private void updateView() {
		if (this.viewListener != null) {
			this.viewListener.update();
		}
	}

	public void changeBreakpointState(Breakpoint breakpoint,
			BreakpointStateEnum state) {
		breakpoint.setState(state);
		updateView();
	}

	public void registerBlockingEventsOnEngine(QName processName,
			Long processInstID) {
		// TODO: Check if sending this message is required?!
		RequestRegistrationInformation mess = new RequestRegistrationInformation();
		mess.setMsgID(JMSCommunication.getInstance().getMessageID());
		JMSCommunication.getInstance().send(mess);

		// Create a new registration message
		RegisterRequestMessage message = new RegisterRequestMessage();
		message.setMsgID(JMSCommunication.getInstance().getMessageID());

		// Get the debug model
		Debug debugModel = this.manager.getEditor().getDesignEditor()
				.getDebug();

		// Validate all breakpoints if the corresponding activities exist any
		// more
		validateAllBreakpoints(this.manager.getEditor().getDesignEditor());

		// Handle the global process breakpoint
		Requested_Blocking_Events instEvts = message
				.getNewWanted_Blocking_Events(false, false, false, false,
						false, false, false, false, false, false, false, false,
						false, false);

		if (!debugModel.getGlobalBreakpoints().getBreakpoint().isEmpty()) {

			Breakpoint globBreak = debugModel.getGlobalBreakpoints()
					.getBreakpoint().get(0);

			if (globBreak.isEnabled()) {
				instEvts = generateBlockingFromBreakpointType(
						globBreak.getType(), message);

				this.blockingRegistered = true;

				globBreak.setState(BreakpointStateEnum.REGISTERED);
			}
		}

		// Handle all local breakpoints
		HashMap<String, Requested_Blocking_Events> activityEvents = new HashMap<String, Requested_Blocking_Events>();
		HashMap<String, Element> activityEventConditions = new HashMap<String, Element>();

		for (Breakpoint locBreak : debugModel.getLocalBreakpoints()
				.getBreakpoint()) {

			if (locBreak.isEnabled()) {
				Requested_Blocking_Events evts = generateBlockingFromBreakpointType(
						locBreak.getType(), message);

				activityEvents.put(locBreak.getTargetXPath(), evts);

				if (locBreak.getCondition() != null) {
					activityEventConditions.put(
							locBreak.getTargetXPath(),
							DebugModelHelper.expression2XML(
									locBreak.getCondition(), "condition"));
				}

				this.blockingRegistered = true;

				locBreak.setState(BreakpointStateEnum.REGISTERED);
			}
		}

		message.addInstanceBlockingEvent(processName, processInstID, instEvts,
				activityEvents, activityEventConditions);

		if (this.isBlockingRegistered()) {
			JMSCommunication.getInstance().send(message);

			this.refresh();
		}
	}

	public void removeAllBlockingEvents() {
		if (this.blockingRegistered) {
			UnregisterRequestMessage message = new UnregisterRequestMessage();

			JMSCommunication.getInstance().send(message);

			for (Breakpoint current : this.breakpoints) {
				current.setState(BreakpointStateEnum.UNREGISTERED);
			}
			this.updateView();
		}
		this.blockingRegistered = false;
	}

	public void releaseAllOutstandingBlockingEvents() {
		if (!this.blockedElementXPaths.isEmpty()) {
			while (!this.blockedElementXPaths.isEmpty()) {
				String act_xPath = this.blockedElementXPaths.remove(0);

				Breakpoint breakpoint = this
						.getCorrespondingBreakpoint(act_xPath);

				this.releaseBlockingEvent(breakpoint);

				this.changeBreakpointState(breakpoint,
						BreakpointStateEnum.UNREGISTERED);
			}
		}
	}

	public void updateBreakpointsOnEngine() {
		boolean doSuspend = false;

		// Check if the instance is suspended already
		if (manager.getApplicationState() != AgoraStates.angehalten) {
			this.isUpdatingBreakpoints = true;

			// Suspend the instance
			manager.suspend();

			doSuspend = true;
		}

		// Unregister all blocking events
		this.removeAllBlockingEvents();

		// Register the updated blocking events for debugging
		this.registerBlockingEventsOnEngine(manager.getInstanceInformation()
				.getProcessName(), manager.getInstanceInformation()
				.getInstanceID());

		// If the instance was suspended by this method, resume it again
		if (doSuspend) {
			manager.resume();

			// Check if there is still an activity blocked
			if (!blockedElementXPaths.isEmpty()) {
				String act_xPath = blockedElementXPaths.firstElement();

				BPELExtensibleElement activity = XPathMapper.handleXPath(
						act_xPath, manager.getProcess());

				if (activity.getState().equals(BPELStates.Blocking.name())) {
					// Change the breakpoint state if the activity is blocked
					this.changeBreakpointState(
							this.getCorrespondingBreakpoint(act_xPath),
							BreakpointStateEnum.BLOCKING);
				} else {
					// Remove the activity xpath from the stack if the the
					// activity is not blocked anymore
					blockedElementXPaths.remove(0);
				}
			}
		}
	}

	public void registerBlockingMessage(Object message) {
		if (message instanceof ActivityEventMessage) {
			ActivityEventMessage act_message = (ActivityEventMessage) message;

			this.eventMessages.put(act_message.getActivityXPath(), act_message);
			this.blockedElementXPaths.push(act_message.getActivityXPath());

			changeBreakpointState(
					getCorrespondingBreakpoint(act_message.getActivityXPath()),
					BreakpointStateEnum.BLOCKING);
		} else if (message instanceof LinkEventMessage) {
			LinkEventMessage link_message = (LinkEventMessage) message;

			this.eventMessages.put(link_message.getLinkXPath(), link_message);
			this.blockedElementXPaths.push(link_message.getLinkXPath());
		}

		// Show in the instance state figure that the process instance is
		// blocked
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						InstanceState instState = ((BPELEditDomain) manager
								.getEditor().getEditDomain()).getInstanceNode();

						instState.setState(InstanceHelper
								.mapToUIExtensionState(BPELStates.Blocking
										.name()));
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

	public boolean releaseBlockingEvent() {
		boolean success = false;

		String act_xPath = "";
		BPELExtensibleElement activity = this.selectedActivity;

		if (!(this.selectedActivity instanceof Process)) {
			act_xPath = this.selectedActivity.getXPath();
		} else {
			// If the process was selected use the "oldest" element on the stack
			if (!this.blockedElementXPaths.isEmpty()) {
				act_xPath = this.blockedElementXPaths.remove(0);
				activity = XPathMapper.handleXPath(act_xPath,
						manager.getProcess());
			}
		}

		if (this.eventMessages.containsKey(act_xPath)) {
			if ((activity instanceof Activity && activity.getState().equals(
					BPELStates.Blocking.name()))
					|| (activity instanceof Link && activity.getState().equals(
							BPELStates.Link_Blocking.name()))) {

				InstanceEventMessage message = this.eventMessages
						.get(act_xPath);

				IncomingMessageBase mesg = getCorrespondingReplyMessage(message);

				JMSCommunication.getInstance().send(mesg);

				// Change the activity state in the process model
				BPELStates state = BPELStates.Blocking;
				if (message instanceof LinkEventMessage) {
					state = StateMachine
							.computeLinkStateAfterBlocking((LinkEventMessage) message);
				} else {
					state = StateMachine
							.computeActivityStateAfterBlocking(message);
				}

				activity.setState(state.name());

				// Show in the instance state figure that the process instance
				// is executed again
				InstanceState instState = ((BPELEditDomain) manager.getEditor()
						.getEditDomain()).getInstanceNode();

				instState.setState(InstanceHelper
						.mapToUIExtensionState(BPELStates.Executing.name()));

				changeBreakpointState(getCorrespondingBreakpoint(act_xPath),
						BreakpointStateEnum.REGISTERED);

				// Remove the entry from the map
				this.eventMessages.remove(act_xPath);
			}
			success = true;
		}

		return success;
	}

	public boolean releaseBlockingEvent(Breakpoint currentBreakpoint) {
		boolean success = false;

		String act_xPath = currentBreakpoint.getTargetXPath();

		if (currentBreakpoint.getTargetXPath().equals("/process")) {
			// If nothing was selected or the global breakpoint is selected, use
			// the "oldest" element on the stack
			if (!this.blockedElementXPaths.isEmpty()) {
				act_xPath = this.blockedElementXPaths.remove(0);
			}
		}

		BPELExtensibleElement activity = XPathMapper.handleXPath(act_xPath,
				manager.getProcess());

		if (this.eventMessages.containsKey(act_xPath)) {
			if (activity.getState().equals(BPELStates.Blocking.name())) {

				InstanceEventMessage message = this.eventMessages
						.get(act_xPath);

				IncomingMessageBase mesg = getCorrespondingReplyMessage(message);

				JMSCommunication.getInstance().send(mesg);

				// Change the activity state in the process model
				BPELStates state = BPELStates.Blocking;
				if (message instanceof LinkEventMessage) {
					state = StateMachine
							.computeLinkStateAfterBlocking((LinkEventMessage) message);
				} else {
					state = StateMachine
							.computeActivityStateAfterBlocking(message);
				}

				activity.setState(state.name());

				// Show in the instance state figure that the process instance
				// is executed again
				InstanceState instState = ((BPELEditDomain) manager.getEditor()
						.getEditDomain()).getInstanceNode();

				instState.setState(InstanceHelper
						.mapToUIExtensionState(BPELStates.Executing.name()));

				changeBreakpointState(currentBreakpoint,
						BreakpointStateEnum.REGISTERED);

				// Remove the entry from the map
				this.eventMessages.remove(act_xPath);
			}
			success = true;
		}

		return success;
	}

	public boolean isBlockingRegistered() {
		return this.blockingRegistered;
	}

	private IncomingMessageBase getCorrespondingReplyMessage(
			InstanceEventMessage message) {

		IncomingMessageBase result = null;

		if (message instanceof Activity_Ready) {
			Start_Activity mesg = new Start_Activity();
			mesg.setMsgID(JMSCommunication.getInstance().getMessageID());
			mesg.setReplyToMsgID(message.getMessageID());

			result = mesg;
		}

		else if (message instanceof Activity_Executed) {
			Complete_Activity mesg = new Complete_Activity();
			mesg.setMsgID(JMSCommunication.getInstance().getMessageID());
			mesg.setReplyToMsgID(message.getMessageID());

			result = mesg;
		}

		else if (message instanceof Activity_Faulted) {
			Continue mesg = new Continue();
			mesg.setMsgID(JMSCommunication.getInstance().getMessageID());
			mesg.setReplyToMsgID(message.getMessageID());

			result = mesg;
		}

		else if (message instanceof Evaluating_TransitionCondition_Faulted) {
			Continue mesg = new Continue();
			mesg.setMsgID(JMSCommunication.getInstance().getMessageID());
			mesg.setReplyToMsgID(message.getMessageID());

			result = mesg;
		}

		else if (message instanceof Scope_Compensating) {
			Continue mesg = new Continue();
			mesg.setMsgID(JMSCommunication.getInstance().getMessageID());
			mesg.setReplyToMsgID(message.getMessageID());

			result = mesg;
		}

		else if (message instanceof Scope_Handling_Termination) {
			Continue mesg = new Continue();
			mesg.setMsgID(JMSCommunication.getInstance().getMessageID());
			mesg.setReplyToMsgID(message.getMessageID());

			result = mesg;
		}

		else if (message instanceof Scope_Handling_Fault) {
			Continue mesg = new Continue();
			mesg.setMsgID(JMSCommunication.getInstance().getMessageID());
			mesg.setReplyToMsgID(message.getMessageID());

			result = mesg;

		} else if (message instanceof Scope_Complete_With_Fault) {
			Continue mesg = new Continue();
			mesg.setMsgID(JMSCommunication.getInstance().getMessageID());
			mesg.setReplyToMsgID(message.getMessageID());

			result = mesg;
		}

		else if (message instanceof Loop_Iteration_Complete) {
			Continue_Loop mesg = new Continue_Loop();
			mesg.setMsgID(JMSCommunication.getInstance().getMessageID());
			mesg.setReplyToMsgID(message.getMessageID());

			result = mesg;
		}

		else if (message instanceof Loop_Condition_True) {
			Continue_Loop_Execution mesg = new Continue_Loop_Execution();
			mesg.setMsgID(JMSCommunication.getInstance().getMessageID());
			mesg.setReplyToMsgID(message.getMessageID());

			result = mesg;
		}

		else if (message instanceof Loop_Condition_False) {
			Finish_Loop_Execution mesg = new Finish_Loop_Execution();
			mesg.setMsgID(JMSCommunication.getInstance().getMessageID());
			mesg.setReplyToMsgID(message.getMessageID());

			result = mesg;
		}

		else if (message instanceof Link_Evaluated) {
			Set_Link_State mesg = new Set_Link_State();
			mesg.setMsgID(JMSCommunication.getInstance().getMessageID());
			mesg.setReplyToMsgID(message.getMessageID());
			mesg.setValue(((Link_Evaluated) message).getValue());

			result = mesg;
		}

		return result;
	}

	private Requested_Blocking_Events generateBlockingFromBreakpointType(
			EList<BreakpointTypeEnum> breakpointTypes,
			RegisterRequestMessage message) {
		// Create a new default blocking events object
		Requested_Blocking_Events blocking = message
				.getNewWanted_Blocking_Events(false, false, false, false,
						false, false, false, false, false, false, false, false,
						false, false);

		for (BreakpointTypeEnum type : breakpointTypes) {
			switch (type) {
			case ACTIVITY_READY:
				blocking.Activity_Ready = true;
				break;
			case ACTIVITY_EXECUTED:
				blocking.Activity_Executed = true;
				break;
			case ACTIVITY_FAULTED:
				blocking.Activity_Faulted = true;
				break;
			case EVALUATING_TRANSITION_CONDITION_FAULTED:
				blocking.Evaluating_TransitionCondition_Faulted = true;
				break;
			case SCOPE_COMPENSATING:
				blocking.Scope_Compensating = true;
				break;
			case SCOPE_COMPLETE_WITH_FAULT:
				blocking.Scope_Complete_With_Fault = true;
				break;
			case SCOPE_HANDLING_FAULT:
				blocking.Scope_Handling_Fault = true;
				break;
			case SCOPE_HANDLING_TERMINATION:
				blocking.Scope_Handling_Termination = true;
				break;
			case LOOP_ITERATION_COMPLETE:
				blocking.Loop_Iteration_Complete = true;
				break;
			case LOOP_CONDITION_FALSE:
				blocking.Loop_Condition_False = true;
				break;
			case LOOP_CONDITION_TRUE:
				blocking.Loop_Condition_True = true;
				break;
			case LINK_EVALUATED:
				blocking.Link_Evaluated = true;
				break;
			}
		}

		return blocking;
	}

	public void setSelectedActivity(BPELExtensibleElement selectedActivity) {
		this.selectedActivity = selectedActivity;
	}

	public BPELExtensibleElement getSelectedActivity() {
		return selectedActivity;
	}

	/**
	 * Validates all availabe breakpoints of the debug model against the
	 * XPathMap. This means if the debug model contains a breakpoint with a
	 * xPath value that isn't hold in the XPathMap any more the breakpoint will
	 * be removed from the model.
	 * 
	 */
	private void validateAllBreakpoints(BPELEditor designEditor) {
		final ICommandFramework commandFramework = designEditor
				.getCommandFramework();

		final Debug debugModel = designEditor.getDebug();

		List<Breakpoint> breakpoints = new ArrayList<Breakpoint>();
		breakpoints.addAll(debugModel.getLocalBreakpoints().getBreakpoint());

		for (final Breakpoint breakpoint : breakpoints) {
			boolean valid = XPathMapProvider.getInstance()
					.getXPathMap(designEditor.getProcess())
					.isActivityWithXPathRegistered(breakpoint.getTargetXPath());

			if (!valid) {
				Thread t = new Thread(new Runnable() {
					@Override
					public void run() {
						Display.getDefault().syncExec(new Runnable() {
							@Override
							public void run() {
								// Remove the breakpoint definition from the
								// debug model
								commandFramework
										.execute(new RemoveBreakpointCommand(
												debugModel
														.getLocalBreakpoints(),
												breakpoint));
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
		}
	}

	private Breakpoint getCorrespondingBreakpoint(String act_xPath) {
		Breakpoint result = null;
		Breakpoint processBreak = null;

		for (Breakpoint current : this.breakpoints) {
			if (current.getTargetXPath().equals("/process")) {
				processBreak = current;
			}

			if (current.getTargetXPath().equals(act_xPath)) {
				if (current.isEnabled()) {
					result = current;
				}
				break;
			}
		}

		if (result == null) {
			result = processBreak;
		}

		return result;
	}

	public Breakpoint getBreakpoint(String xPath) {
		Breakpoint result = null;

		for (Breakpoint current : this.breakpoints) {
			if (current.getTargetXPath().equals(xPath)) {
				result = current;
				break;
			}
		}

		return result;
	}

	public boolean isUpdatingBreakpoints() {
		return this.isUpdatingBreakpoints;
	}

	public void setIsUpdatingBreakpoints(boolean isUpdating) {
		this.isUpdatingBreakpoints = isUpdating;
	}

	public boolean isBlocking() {
		return !this.blockedElementXPaths.isEmpty();
	}
}
