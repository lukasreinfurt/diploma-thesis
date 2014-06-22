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
public interface VariableExtension extends EObject {

	/**
	 * @model
	 */
	public static final int KIND_UNKNOWN = 0;
	/**
	 * @model
	 */
	public static final int KIND_SIMPLE = 1;
	/**
	 * @model
	 */
	public static final int KIND_DATATYPE = 2;
	/**
	 * @model
	 */
	public static final int KIND_INTERFACE = 3;
	/**
	 * @model
	 */
	public static final int KIND_ADVANCED = 4;

	/**
	 * @model transient="true"
	 */
	public int getAdvancedKind();

	/**
	 * Sets the value of the '{@link org.eclipse.bpel.ui.uiextensionmodel.VariableExtension#getAdvancedKind <em>Advanced Kind</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Advanced Kind</em>' attribute.
	 * @see #getAdvancedKind()
	 * @generated
	 */
	void setAdvancedKind(int value);

	/**
	 * @model
	 */
	public static final int ADVANCED_WSDL_MESSAGE = 0; 
	
	/**
	 * @model
	 */
	public static final int ADVANCED_XSD_TYPE = 1; 
	
	/**
	 * @model
	 */
	public static final int ADVANCED_XSD_ELEMENT = 2; 
	
	/**
	 * @model transient="true"
	 */
	public int getVariableKind();

	/**
	 * Sets the value of the '{@link org.eclipse.bpel.ui.uiextensionmodel.VariableExtension#getVariableKind <em>Variable Kind</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Variable Kind</em>' attribute.
	 * @see #getVariableKind()
	 * @generated
	 */
	void setVariableKind(int value);

	/**
	 * Returns the value of the '<em><b>Parameter</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Parameter</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Parameter</em>' attribute.
	 * @see #setParameter(boolean)
	 * @see org.eclipse.bpel.ui.uiextensionmodel.UiextensionmodelPackage#getVariableExtension_Parameter()
	 * @model
	 * @generated
	 */
	boolean isParameter();

	/**
	 * Sets the value of the '{@link org.eclipse.bpel.ui.uiextensionmodel.VariableExtension#isParameter <em>Parameter</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Parameter</em>' attribute.
	 * @see #isParameter()
	 * @generated
	 */
	void setParameter(boolean value);

	/**
	 * Returns the value of the '<em><b>Default</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Default</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Default</em>' attribute.
	 * @see #setDefault(String)
	 * @see org.eclipse.bpel.ui.uiextensionmodel.UiextensionmodelPackage#getVariableExtension_Default()
	 * @model
	 * @generated
	 */
	String getDefault();

	/**
	 * Sets the value of the '{@link org.eclipse.bpel.ui.uiextensionmodel.VariableExtension#getDefault <em>Default</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Default</em>' attribute.
	 * @see #getDefault()
	 * @generated
	 */
	void setDefault(String value);

}
