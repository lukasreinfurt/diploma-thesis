/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.eclipse.bpel.ui.uiextensionmodel;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.Enumerator;

/**
 * <!-- begin-user-doc -->
 * A representation of the literals of the enumeration '<em><b>BPEL States</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * @see org.eclipse.bpel.ui.uiextensionmodel.UiextensionmodelPackage#getBPELStates()
 * @model
 * @generated
 */
public enum BPELStates implements Enumerator {
	/**
	 * The '<em><b>Initial</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #INITIAL_VALUE
	 * @generated
	 * @ordered
	 */
	INITIAL(0, "Initial", "Initial"),

	/**
	 * The '<em><b>Executing</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #EXECUTING_VALUE
	 * @generated
	 * @ordered
	 */
	EXECUTING(1, "Executing", "Executing"),

	/**
	 * The '<em><b>Suspended</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #SUSPENDED_VALUE
	 * @generated
	 * @ordered
	 */
	SUSPENDED(2, "Suspended", "Suspended"),

	/**
	 * The '<em><b>Faulted</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #FAULTED_VALUE
	 * @generated
	 * @ordered
	 */
	FAULTED(3, "Faulted", "Faulted"),

	/**
	 * The '<em><b>Terminated</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #TERMINATED_VALUE
	 * @generated
	 * @ordered
	 */
	TERMINATED(4, "Terminated", "Terminated"),

	/**
	 * The '<em><b>Completed</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #COMPLETED_VALUE
	 * @generated
	 * @ordered
	 */
	COMPLETED(5, "Completed", "Completed"), /**
	 * The '<em><b>Blocking</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #BLOCKING_VALUE
	 * @generated
	 * @ordered
	 */
	BLOCKING(6, "Blocking", "Event Blocking");

	/**
	 * The '<em><b>Initial</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>Initial</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #INITIAL
	 * @model name="Initial"
	 * @generated
	 * @ordered
	 */
	public static final int INITIAL_VALUE = 0;

	/**
	 * The '<em><b>Executing</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>Executing</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #EXECUTING
	 * @model name="Executing"
	 * @generated
	 * @ordered
	 */
	public static final int EXECUTING_VALUE = 1;

	/**
	 * The '<em><b>Suspended</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>Suspended</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #SUSPENDED
	 * @model name="Suspended"
	 * @generated
	 * @ordered
	 */
	public static final int SUSPENDED_VALUE = 2;

	/**
	 * The '<em><b>Faulted</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>Faulted</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #FAULTED
	 * @model name="Faulted"
	 * @generated
	 * @ordered
	 */
	public static final int FAULTED_VALUE = 3;

	/**
	 * The '<em><b>Terminated</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>Terminated</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #TERMINATED
	 * @model name="Terminated"
	 * @generated
	 * @ordered
	 */
	public static final int TERMINATED_VALUE = 4;

	/**
	 * The '<em><b>Completed</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>Completed</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #COMPLETED
	 * @model name="Completed"
	 * @generated
	 * @ordered
	 */
	public static final int COMPLETED_VALUE = 5;

	/**
	 * The '<em><b>Blocking</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>Blocking</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #BLOCKING
	 * @model name="Blocking" literal="Event Blocking"
	 * @generated
	 * @ordered
	 */
	public static final int BLOCKING_VALUE = 6;

	/**
	 * An array of all the '<em><b>BPEL States</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final BPELStates[] VALUES_ARRAY =
		new BPELStates[] {
			INITIAL,
			EXECUTING,
			SUSPENDED,
			FAULTED,
			TERMINATED,
			COMPLETED,
			BLOCKING,
		};

	/**
	 * A public read-only list of all the '<em><b>BPEL States</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List<BPELStates> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>BPEL States</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static BPELStates get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			BPELStates result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>BPEL States</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static BPELStates getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			BPELStates result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>BPEL States</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static BPELStates get(int value) {
		switch (value) {
			case INITIAL_VALUE: return INITIAL;
			case EXECUTING_VALUE: return EXECUTING;
			case SUSPENDED_VALUE: return SUSPENDED;
			case FAULTED_VALUE: return FAULTED;
			case TERMINATED_VALUE: return TERMINATED;
			case COMPLETED_VALUE: return COMPLETED;
			case BLOCKING_VALUE: return BLOCKING;
		}
		return null;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private final int value;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private final String name;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private final String literal;

	/**
	 * Only this class can construct instances.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private BPELStates(int value, String name, String literal) {
		this.value = value;
		this.name = name;
		this.literal = literal;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getValue() {
	  return value;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getName() {
	  return name;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getLiteral() {
	  return literal;
	}

	/**
	 * Returns the literal value of the enumerator, which is its string representation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		return literal;
	}
	
} //BPELStates
