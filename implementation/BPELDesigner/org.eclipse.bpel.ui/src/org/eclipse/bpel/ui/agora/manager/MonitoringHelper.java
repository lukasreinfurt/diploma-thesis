package org.eclipse.bpel.ui.agora.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.bpel.model.Activity;
import org.eclipse.bpel.model.BPELExtensibleElement;
import org.eclipse.bpel.model.BPELFactory;
import org.eclipse.bpel.model.BPELPackage;
import org.eclipse.bpel.model.Flow;
import org.eclipse.bpel.model.ForEach;
import org.eclipse.bpel.model.If;
import org.eclipse.bpel.model.Invoke;
import org.eclipse.bpel.model.Link;
import org.eclipse.bpel.model.PartnerLink;
import org.eclipse.bpel.model.Pick;
import org.eclipse.bpel.model.Process;
import org.eclipse.bpel.model.RepeatUntil;
import org.eclipse.bpel.model.Scope;
import org.eclipse.bpel.model.Sequence;
import org.eclipse.bpel.model.Source;
import org.eclipse.bpel.model.Target;
import org.eclipse.bpel.model.Variable;
import org.eclipse.bpel.model.While;
import org.eclipse.bpel.model.debug.XPathMapProvider;
import org.eclipse.bpel.model.impl.BPELFactoryImpl;
import org.eclipse.bpel.model.impl.BPELPackageImpl;
import org.eclipse.bpel.model.util.BPELUtils;
import org.eclipse.bpel.ui.util.BPELUtil;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.wst.wsdl.WSDLElement;
import org.simtech.workflow.ode.auditing.agora.BPELStates;

public class MonitoringHelper {

	private HashMap<String, Integer> loopActivityCounters = new HashMap<String, Integer>();

	private List<String> iterationBodyList;

	public void increaseLoopCounter(String activityXPath) {
		Integer oldCounter = loopActivityCounters.get(activityXPath);
		if (oldCounter != null) {
			loopActivityCounters.put(activityXPath, oldCounter + 1);
		} else {
			loopActivityCounters.put(activityXPath, 1);
		}
	}

	public String getLoopCounter(Object model) {
		if (model instanceof ForEach || model instanceof While
				|| model instanceof RepeatUntil) {
			String counter = getLoopCounter(((BPELExtensibleElement) model)
					.getXPath());
			return counter;
		}

		return "";
	}

	public String getLoopCounter(String activityXPath) {
		Integer counter = loopActivityCounters.get(activityXPath);

		if (counter != null) {
			return "iterations: " + String.valueOf(counter);
		} else {
			return "";
		}
	}

	public void reset() {
		loopActivityCounters.clear();
	}

	public void resetStateOfChildActivities(String activityXPath,
			org.eclipse.bpel.model.Process process, MonitorManager manager) {
		BPELExtensibleElement loopActivity = XPathMapProvider.getInstance()
				.getXPathMap(process).getElementByXPath(activityXPath);

		this.resetRecursiveChildActivityStates(loopActivity, manager);
	}

	private void resetRecursiveChildActivityStates(
			BPELExtensibleElement element, MonitorManager manager) {
		if (element != null) {
			for (EObject eObject : element.eContents()) {
				if (eObject != null && eObject instanceof BPELExtensibleElement) {
					BPELExtensibleElement extElement = (BPELExtensibleElement) eObject;

					XPathMapper.setState(extElement.getXPath(),
							BPELStates.Inactive, manager.getProcess());
					
					// Reset the counter of nested loop activities
					resetCounterForLoopActivity(extElement);

					if (eObject.eContents() != null
							&& !eObject.eContents().isEmpty()) {
						resetRecursiveChildActivityStates(extElement, manager);
					}
				}
			}
		}
	}

	private void resetCounterForLoopActivity(BPELExtensibleElement activity) {
		if (activity instanceof ForEach || activity instanceof While
				|| activity instanceof RepeatUntil) {
			loopActivityCounters.remove(activity.getXPath());
		}
	}

	public List<String> resetStateOfSuccessorActivities(Activity activity) {

		// this list will store all activities the state of which needed
		// to be reset.
		iterationBodyList = new ArrayList<String>();

		// if state needs to be reset, insert the activity to the list
		if (activity.getState() != null && !activity.getState().equals("")
				&& !activity.getState().equals(BPELStates.Inactive.name())) {
			iterationBodyList.add(activity.getXPath());
		}

		// Reset the activity state
		activity.setState(BPELStates.Inactive.name());

		// Reset counter
		resetCounterForLoopActivity(activity);
		
		// If it's an invoke, reset CH and FH
		resetInvokeHandlers(activity);

		// Reset the state of all process handlers
		Process process = BPELUtils.getProcess(activity);
		if (process.getFaultHandlers() != null) {
			resetRecursiveChildActivityStatesWithoutAThread(process
					.getFaultHandlers());
		}
		if (process.getEventHandlers() != null) {
			resetRecursiveChildActivityStatesWithoutAThread(process
					.getEventHandlers());
		}

		// Reset the state of all successor elements which are in other
		// containers than the activity
		resetStateOfSuccessorContainersAndContent(activity);
		return iterationBodyList;
	}

	private void resetStateOfSuccessorContainersAndContent(WSDLElement element) {

		// Check if the selected element is a container activity
		// so we have to handle the child elements
		if (element instanceof Sequence || element instanceof If
				|| element instanceof ForEach || element instanceof RepeatUntil
				|| element instanceof While || element instanceof Scope
				|| element instanceof Flow || element instanceof Pick) {
			resetRecursiveChildActivityStatesWithoutAThread((BPELExtensibleElement) element);

			// If the selected activity is a scope, we clear the status of all
			// associated handlers and their child activities
			resetScopeHandlers((Activity)element);
		}

		// Update the state of all other affected containers and reset the state
		// of all containers (and their children) which are successors of the
		// activity container.
		WSDLElement container = element.getContainer();
		while (container != null && !(container instanceof Process)) {

			// Set the state of all parent containers back to "Executing" if
			// they are in another state (completed, waiting, faulted, ...)
			String state = ((BPELExtensibleElement) container).getState() == null ? ""
					: ((BPELExtensibleElement) container).getState();
			if (!state.equals(BPELStates.Executing.name())) {
				((BPELExtensibleElement) container)
						.setState(BPELStates.Executing.name());
			}

			// Special handling for flows: The index of the elements can not be
			// used because the layout of flow activities in the designer
			// doesn't have to correspond to the modeled
			// control flow of the process like in a sequence.
			if (container instanceof Flow && element instanceof Activity) {
				Flow flow = (Flow) container;

				resetFlowChildActivityStates(flow, (Activity) element);
			} else if (container instanceof Scope
					&& element instanceof Activity) {
				resetScopeHandlers((Scope) container);
			} else {

				// Get the index of the element in the container
				int elementIndex = container.eContents().indexOf(element);

				BPELExtensibleElement current = null;
				for (int i = elementIndex + 1; i < container.eContents().size(); i++) {
					if (container.eContents().get(i) instanceof BPELExtensibleElement) {
						current = (BPELExtensibleElement) container.eContents()
								.get(i);

						// if state needs to be reset, insert the activity to
						// the list
						if (current.getState() != null
								&& !current.getState().equals("")
								&& !current.getState().equals(
										BPELStates.Inactive.name())) {
							iterationBodyList.add(current.getXPath());
						}
						
						// Reset the element state
						current.setState(BPELStates.Inactive.name());

						// Reset loop counter
						resetCounterForLoopActivity(current);
						
						// If it's an invoke, reset CH and FH
						resetInvokeHandlers(current);

						// Check if the current element is a container activity
						// so we have to handle the child elements
						if (current instanceof Sequence
								|| current instanceof If
								|| current instanceof ForEach
								|| current instanceof RepeatUntil
								|| current instanceof While
								|| current instanceof Scope
								|| current instanceof Flow
								|| current instanceof Pick) {
							resetRecursiveChildActivityStatesWithoutAThread(current);
							resetScopeHandlers(current);
						}
					}
				}
			}

			// Move one layer up the hierarchy
			element = container;
			container = container.getContainer();

			// If we started reseting the state from inside of a flow we have to
			// skip the flow itself so that we don't move downwards again.
//			if (container instanceof Flow) {
//
//				// Set the state of the flow back to "Executing" if
//				// it is in another state (completed, waiting, faulted, ...)
//				state = ((BPELExtensibleElement) container).getState() == null ? ""
//						: ((BPELExtensibleElement) container).getState();
//				if (!state.equals(BPELStates.Executing.name())) {
//					((BPELExtensibleElement) container)
//							.setState(BPELStates.Executing.name());
//				}
//
//				// Move one layer up the hierarchy
//				element = container;
//				container = container.getContainer();
//			}
		}
	}

	/**
	 * Resets the state of all activities in a flow starting at the given
	 * activity by following the modeled control flow links.
	 * 
	 * @param flow
	 *            The parent flow activity
	 * @param activity
	 *            The activity to start the reset process on
	 */
	private void resetFlowChildActivityStates(Flow flow, Activity activity) {
		Set<Link> activityLinks = new HashSet<Link>();

		// if state needs to be reset, insert the activity to the list
		if (activity.getState() != null && !activity.getState().equals("")
				&& !activity.getState().equals(BPELStates.Inactive.name())) {
			iterationBodyList.add(activity.getXPath());
		}

		// Reset the activity state
		activity.setState(BPELStates.Inactive.name());

		// Reset loop counter
		resetCounterForLoopActivity(activity);
		
		// if it's an invoke, reset CH and FH
		resetInvokeHandlers(activity);

		// Check if we followed a link which target activity is located in a
		// nested flow
		if (activity.getContainer() instanceof Flow
				&& activity.getContainer() != flow) {
			// Set the nested flow status back to "Executing"
			((Flow) activity.getContainer()).setState(BPELStates.Executing
					.name());
		}

		// Check if the current element is a container activity
		// so we have to handle the child elements
		if (activity instanceof Sequence || activity instanceof If
				|| activity instanceof ForEach
				|| activity instanceof RepeatUntil || activity instanceof While
				|| activity instanceof Scope || activity instanceof Flow
				|| activity instanceof Pick) {
			resetRecursiveChildActivityStatesWithoutAThread(activity);

			// If the selected activity is a scope, we clear the status of all
			// associated handlers and their child activities
			resetScopeHandlers(activity);
		}

		// Collect all links which start at the given activity
		for (Link link : flow.getLinks().getChildren()) {
			for (Source source : link.getSources()) {
				if (source.getActivity().equals(activity)) {
					activityLinks.add(link);
				}
			}
		}

		// Follow all the collected links to their target activity and invoke
		// this method recursive
		for (Link link : activityLinks) {
			// Reset the link state
			link.setState(BPELStates.Inactive.name());

			for (Target target : link.getTargets()) {
				if (target.getActivity() != null) {
					resetFlowChildActivityStates(flow, target.getActivity());
				}
			}
		}
	}

	private void resetRecursiveChildActivityStatesWithoutAThread(
			BPELExtensibleElement element) {
		if (element != null) {
			for (EObject eObject : element.eContents()) {
				if (eObject != null && eObject instanceof BPELExtensibleElement) {
					BPELExtensibleElement extElement = (BPELExtensibleElement) eObject;

					// if state needs to be reset, insert the activity to the
					// list
					if (extElement.getState() != null
							&& !extElement.getState().equals("")
							&& !extElement.getState().equals(
									BPELStates.Inactive.name())) {
						iterationBodyList.add(extElement.getXPath());
					}

					// reset the state
					extElement.setState(BPELStates.Inactive.name());

					// Reset loop counter
					resetCounterForLoopActivity((BPELExtensibleElement) eObject);

					// If it's a scope, reset all handlers
					resetScopeHandlers(extElement);
					
					// If it's an invoke, reset CH and FH
					resetInvokeHandlers(extElement);
					
					if (eObject.eContents() != null
							&& !eObject.eContents().isEmpty()) {
						resetRecursiveChildActivityStatesWithoutAThread(extElement);
					}
				}
			}
		}
	}

	private void resetInvokeHandlers(BPELExtensibleElement activity) {
		if (activity instanceof Invoke) {
			Invoke invoke = (Invoke)activity;
			if (invoke.getCompensationHandler() != null) {
				resetRecursiveChildActivityStatesWithoutAThread(invoke
						.getCompensationHandler());
			}
			if (invoke.getFaultHandler() != null) {
				resetRecursiveChildActivityStatesWithoutAThread(invoke
						.getFaultHandler());
			}
		}
	}
	
	private void resetScopeHandlers(BPELExtensibleElement activity) {
		if (activity instanceof Scope) {
			Scope scope = (Scope)activity;
			if (scope.getCompensationHandler() != null) {
				resetRecursiveChildActivityStatesWithoutAThread(scope
						.getCompensationHandler());
			}
			if (scope.getTerminationHandler() != null) {
				resetRecursiveChildActivityStatesWithoutAThread(scope
						.getTerminationHandler());
			}
			if (scope.getFaultHandlers() != null) {
				resetRecursiveChildActivityStatesWithoutAThread(scope
						.getFaultHandlers());
			}
			if (scope.getEventHandlers() != null) {
				resetRecursiveChildActivityStatesWithoutAThread(scope
						.getEventHandlers());
			}
		}
	}

	public void resetChildLoopCounter(String activityXPath, Process process,
			MonitorManager manager) {
		BPELExtensibleElement loopActivity = XPathMapProvider.getInstance()
		.getXPathMap(process).getElementByXPath(activityXPath);

		this.resetRecursiveChildLoopCounter(loopActivity, manager);
	}
	
	private void resetRecursiveChildLoopCounter(
			BPELExtensibleElement element, MonitorManager manager) {
		if (element != null) {
			for (EObject eObject : element.eContents()) {
				if (eObject != null && eObject instanceof BPELExtensibleElement) {
					BPELExtensibleElement extElement = (BPELExtensibleElement) eObject;

					// Reset the counter of nested loop activities
					resetCounterForLoopActivity(extElement);

					if (eObject.eContents() != null
							&& !eObject.eContents().isEmpty()) {
						resetRecursiveChildLoopCounter(extElement, manager);
					}
				}
			}
		}
	}
	
	// @vonstepk Resets all scopeIDs in all variables and partner links
	public void resetScopeIDs(Process process) {
		//Reset scopeIds of child variables of process scope
		Iterator<Variable> it_varp = process.getVariables().getChildren().iterator();
		while( it_varp.hasNext() ) {
			Variable var = it_varp.next();
			var.setScopeID(null);
		}
		
		//Reset scopeIds of child partner links of process scope
		Iterator<PartnerLink> it_plp = process.getPartnerLinks().getChildren().iterator();
		while( it_plp.hasNext() ) {
			PartnerLink pl = it_plp.next();
			pl.setScopeID(null);		
		}
				
		//For all scopes...
		Iterator<EObject> it_scope = BPELUtil.getAllEObjectsOfType(process, BPELPackage.Literals.SCOPE).iterator();
		while( it_scope.hasNext() ){
			Scope obj = (Scope) it_scope.next();
			//...reset scopeIDs of child variables...
			Iterator<Variable> it_var = obj.getVariables().getChildren().iterator();
			while( it_var.hasNext() ) {
				Variable var = it_var.next();
				var.setScopeID(null);
			}
			//...and scopeIDs of child partner links.
			Iterator<PartnerLink> it_pl = obj.getPartnerLinks().getChildren().iterator();
			while( it_pl.hasNext() ) {
				PartnerLink pl = it_pl.next();
				pl.setScopeID(null);
			}
			
		}
	}
}
