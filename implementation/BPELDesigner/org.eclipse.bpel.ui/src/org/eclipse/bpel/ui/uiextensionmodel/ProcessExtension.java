/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.bpel.ui.uiextensionmodel;


import org.eclipse.emf.ecore.EObject;

/**
 * @model
 */
public interface ProcessExtension extends EObject {

	/**
	 * @model
	 */
	public boolean isSpecCompliant();

	/**
	 * Sets the value of the '{@link org.eclipse.bpel.ui.uiextensionmodel.ProcessExtension#isSpecCompliant <em>Spec Compliant</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Spec Compliant</em>' attribute.
	 * @see #isSpecCompliant()
	 * @generated
	 */
	void setSpecCompliant(boolean value);

	/**
	 * @model
	 */
	public long getModificationStamp();
	/**
	 * Sets the value of the '{@link org.eclipse.bpel.ui.uiextensionmodel.ProcessExtension#getModificationStamp <em>Modification Stamp</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Modification Stamp</em>' attribute.
	 * @see #getModificationStamp()
	 * @generated
	 */
	void setModificationStamp(long value);

	/**
	 * Returns the value of the '<em><b>Process Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Process Name</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Process Name</em>' attribute.
	 * @see #setProcessName(String)
	 * @see org.eclipse.bpel.ui.uiextensionmodel.UiextensionmodelPackage#getProcessExtension_ProcessName()
	 * @model
	 * @generated
	 */
	String getProcessName();

	/**
	 * Sets the value of the '{@link org.eclipse.bpel.ui.uiextensionmodel.ProcessExtension#getProcessName <em>Process Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Process Name</em>' attribute.
	 * @see #getProcessName()
	 * @generated
	 */
	void setProcessName(String value);

	/**
	 * Returns the value of the '<em><b>Process Version</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Process Version</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Process Version</em>' attribute.
	 * @see #setProcessVersion(long)
	 * @see org.eclipse.bpel.ui.uiextensionmodel.UiextensionmodelPackage#getProcessExtension_ProcessVersion()
	 * @model
	 * @generated
	 */
	long getProcessVersion();

	/**
	 * Sets the value of the '{@link org.eclipse.bpel.ui.uiextensionmodel.ProcessExtension#getProcessVersion <em>Process Version</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Process Version</em>' attribute.
	 * @see #getProcessVersion()
	 * @generated
	 */
	void setProcessVersion(long value);

}
