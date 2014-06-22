package org.eclipse.bpel.ui.parameters.ui.composite;

import org.eclipse.bpel.model.Variable;
import org.eclipse.swt.widgets.Composite;

/**
 * Abstract class for all entry classes. This class provides
 * methods for building new entry classes which will be inserted
 * in the parameter dialog. For every variable type a separate
 * entry class exists. This abstract class extends both super class
 * entries and leaf entries. That means that this class extends
 * components which hold other components or this class extends 
 * leaf components which don't hold other components. For more
 * information have a look at the composite pattern
 * 
 * @author tolevar
 *
 */
public abstract class DialogEntryComponent extends Composite {
	
	protected Variable variable;
	
	public DialogEntryComponent(Composite parent, int style, Variable variable) {
		super(parent, style);
		this.variable = variable;
	}
	
	/**
	 * Add a DialogEntryComponent 
	 * 
	 * @param component
	 */
	public void add(DialogEntryComponent component) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Remove a DialogEntryComponent
	 * 
	 * @param component
	 */
	public void remove(DialogEntryComponent component) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Returns a DialogEntryComponent from the given index
	 * 
	 * @param i
	 * @return dialogEntryComponent - {@link DialogEntryComponent}
	 */
	public DialogEntryComponent getChild(int i) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Returns the size of the list which holds the components in the
	 * underlying class
	 * 
	 * @return length - integer
	 */
	public int getLength() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Returns the corresponding parameter variable which belongs to the component
	 * 
	 * @return variable - {@link Variable}
	 */
	public Variable getVariable() {
		return this.variable;
	}
	
	/**
	 * Returns the value of the entry, which is the value for 
	 * the parameter variable. It is possible to get an array of values
	 * 
	 * @return values - String[]
	 */
	abstract public String[] getValues();
	
	/**
	 * Returns the name of the variable
	 * 
	 * @return variable name - String
	 */
	public String getVariableName() {
		return this.variable != null?variable.getName():null;
	}
	
	/**
	 * Builds the entry (composite) depending on the type of the variable.
	 * The variableName is used to identify the entry so the values can
	 * be filled to the correct variable
	 * 
	 * @param variableName
	 */
	abstract public void buildEntry(String variableName);

	/**
	 * Builds additional widgets for an entry. For example see {@link NumberEntry}
	 */
	public void buildAdditionEntry() {
		throw new UnsupportedOperationException();
	}

}
