/**
 * 
 */
package org.eclipse.bpel.ui.agora.manager;

import org.eclipse.bpel.model.BPELExtensibleElement;
import org.eclipse.bpel.model.CorrelationSet;
import org.eclipse.bpel.model.PartnerLink;
import org.eclipse.bpel.model.Process;
import org.eclipse.bpel.model.Variable;
import org.eclipse.bpel.model.debug.XPathMapProvider;
import org.eclipse.bpel.ui.agora.provider.MonitoringProvider;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.swt.widgets.Display;
import org.simtech.workflow.ode.auditing.agora.BPELStates;

/**
 * Maps with an xpath-expression to an element at the EMF-model.
 * 
 * @author aeichel
 * 
 *         Some modifications in case that the XPath is initially set on the
 *         ExtensibleElements while loading the model.
 * @author hahnml
 * @author tolevar
 * 
 *         Added the manager to the handleXPath method, so the correct process
 *         and the corresponding xpaths are chosen
 */
public class XPathMapper {

	static final String XPATH_EMPTY = "";
	static BPELStates state;

	public static BPELStates getState(String xpath, Process process) {
		final BPELExtensibleElement actualElement = handleXPath(xpath, process);

		state = null;

		// Start the thread only if we know the element
		if (actualElement != null) {
			Thread t = new Thread(new Runnable() {

				@Override
				public void run() {
					Display.getDefault().syncExec(new Runnable() {
						@Override
						public void run() {
							String elementState = actualElement.getState();
							if (elementState != null
									&& elementState.compareTo("") != 0) {
								state = BPELStates.valueOf(elementState);
							} else {
								state = BPELStates.Inactive;
							}
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
		} else {
			// Use the StateBuffer for the AuditingView
			state = MonitoringProvider.getInstance().getProcessManager(process)
					.getStateBuffer().getStateFromBuffer(xpath);
		}

		return state;
	}

	public static void setState(String xpath, final BPELStates state,
			Process process) {
		final BPELExtensibleElement actualElement = handleXPath(xpath, process);

		// Start the thread only if we know the element
		if (actualElement != null) {
			Thread t = new Thread(new Runnable() {

				@Override
				public void run() {
					Display.getDefault().syncExec(new Runnable() {
						@Override
						public void run() {
							actualElement.setState(state.name());
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
		} else {
			// Use the StateBuffer for the AuditingView
			MonitoringProvider.getInstance().getProcessManager(process)
					.getStateBuffer().updateStateInBuffer(xpath, state);
		}
	}

	public static void setLinkState(String xpath, final BPELStates state,
			Process process) {
		final BPELExtensibleElement actualElement = handleXPath(xpath, process);

		// Start the thread only if we know the element
		if (actualElement != null) {
			Thread t = new Thread(new Runnable() {

				@Override
				public void run() {
					Display.getDefault().syncExec(new Runnable() {
						@Override
						public void run() {
							actualElement.setState(state.name());
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

	public static void setVariable(String xpath, final String value,
			final Long scopeID, Process process) {
		BPELExtensibleElement extElement = handleXPath(xpath, process);

		final Variable actualElement = (Variable) extElement;

		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						if (actualElement != null) {
							actualElement.setValue(value);
							actualElement.setScopeID(scopeID);
						}
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

	// @sonntamo
	public static void setCorrelationSet(String xpath, final String[] values,
			final Long scopeID, Process process) {
		BPELExtensibleElement extElement = handleXPath(xpath, process);

		final CorrelationSet actualElement = (CorrelationSet) extElement;

		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						String vals = "";
						for (String val : values) {
							vals += val + "&&";
						}
						vals = vals.substring(vals.length() - 2);
						actualElement.setValues(vals);
						actualElement.setScopeID(scopeID);
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

	// @author: sonntamo
	public static void setPartnerLink(String xpath, final String value,
			final Long scopeID, Process process) {
		BPELExtensibleElement extElement = handleXPath(xpath, process);

		final PartnerLink actualElement = (PartnerLink) extElement;

		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						actualElement.setValue(value);
						actualElement.setScopeID(scopeID);
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

	// @hahnml: Get the element from the XPathMap
	public static BPELExtensibleElement handleXPath(String xpath,
			Process process) {
		return XPathMapProvider.getInstance().getXPathMap(process)
				.getElementByXPath(xpath);
	}

	// resets all the coloring of the model
	public static void resetAllStates(Process process) {
		resetMethod(process);
		// Clear all buffered states
		MonitoringProvider.getInstance().getProcessManager(process)
				.getStateBuffer().clearStateBuffer();
	}

	private static void resetMethod(BPELExtensibleElement element) {
		if (element instanceof Process) {
			element.setState(BPELStates.Inactive.name());
		}

		for (EObject eObject : element.eContents()) {
			if (eObject instanceof BPELExtensibleElement) {
				BPELExtensibleElement extElement = (BPELExtensibleElement) eObject;
				extElement.setState(BPELStates.Inactive.name());

				if (eObject.eContents() != null
						&& !eObject.eContents().isEmpty()) {
					resetMethod(extElement);
				}
			}
		}
	}
}
