/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.eclipse.bpel.ui.uiextensionmodel;

import org.eclipse.bpel.model.Process;
import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Instance State</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.bpel.ui.uiextensionmodel.InstanceState#getState <em>State</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.bpel.ui.uiextensionmodel.UiextensionmodelPackage#getInstanceState()
 * @model
 * @generated
 */
public interface InstanceState extends EObject {
	/**
	 * Returns the value of the '<em><b>State</b></em>' attribute.
	 * The literals are from the enumeration {@link org.eclipse.bpel.ui.uiextensionmodel.BPELStates}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>State</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>State</em>' attribute.
	 * @see org.eclipse.bpel.ui.uiextensionmodel.BPELStates
	 * @see #setState(BPELStates)
	 * @see org.eclipse.bpel.ui.uiextensionmodel.UiextensionmodelPackage#getInstanceState_State()
	 * @model
	 * @generated
	 */
	BPELStates getState();

	/**
	 * Sets the value of the '{@link org.eclipse.bpel.ui.uiextensionmodel.InstanceState#getState <em>State</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>State</em>' attribute.
	 * @see org.eclipse.bpel.ui.uiextensionmodel.BPELStates
	 * @see #getState()
	 * @generated
	 */
	void setState(BPELStates value);
	
	public Process getProcess();
	void setProcess(Process process);

} // InstanceState
