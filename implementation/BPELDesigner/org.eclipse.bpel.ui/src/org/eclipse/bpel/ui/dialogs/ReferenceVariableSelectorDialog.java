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
package org.eclipse.bpel.ui.dialogs;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.bpel.model.BPELFactory;
import org.eclipse.bpel.model.Process;
import org.eclipse.bpel.model.ReferenceVariable;
import org.eclipse.bpel.model.util.BPELUtils;
import org.eclipse.bpel.ui.Messages;
import org.eclipse.bpel.ui.commands.CompoundCommand;
import org.eclipse.bpel.ui.commands.InsertInContainerCommand;
import org.eclipse.bpel.ui.commands.SetNameCommand;
import org.eclipse.bpel.ui.properties.PropertiesLabelProvider;
import org.eclipse.bpel.ui.util.BPELUtil;
import org.eclipse.bpel.ui.util.ModelHelper;
import org.eclipse.bpel.ui.util.NameDialog;
import org.eclipse.bpel.ui.util.XSDUtils;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.FilteredList;


/**
 * Asks the user to select a variable.
 */
public class ReferenceVariableSelectorDialog extends SelectionAndCreationDialog {

	protected static class TypeLabelProvider extends LabelProvider {
		@Override
		public String getText(Object element) {
			if (element instanceof ReferenceVariable) {
				ReferenceVariable variable = (ReferenceVariable)element;
				if (variable.getValueType() != null) {
					return XSDUtils.getDisplayNameFromXSDType(variable.getValueType());
				}
				if (variable.getReferenceType() != null) {
					return variable.getReferenceType().toString();
				}
				if (variable.getPartnerLink() != null) {
					return variable.getPartnerLink().getName();
				}
				if (variable.getPeriod() != -1) {
					
				}
			}
			return "(none)"; 
		}
	}
	
	/**
	 * The sort order should be:
	 *   - variables with the desired type
	 *   - alphabetical by name 
	 */
	protected class VariableComparator implements Comparator {
		/**
		 * The given objects are the variable names.
		 */
		public int compare(Object o1, Object o2) {
			String name1 = (String)o1;
			String name2 = (String)o2;
			if (name1 == null || name2 == null) {
				// neither of the names should be null but we handle it here in case that happens
				if (name1 == null && name2 == null) return 0;
				if (name1 == null) return 1;
				return -1;
			}
			return 0;
		}
	}
	
	protected EObject modelObject;
	protected Map nameToVariable;
	protected ReferenceVariable[] variables;
	protected EObject variableType;
	protected Text filterText;
	
	/**
	 * The modelObject is the model element that indicates the scope in which the
	 * variable should be visible.
	 */
	public ReferenceVariableSelectorDialog(Shell parent, EObject modelObject) {
		super(parent, new PropertiesLabelProvider(), new TypeLabelProvider());
		this.modelObject = modelObject;
		nameToVariable = new HashMap();
		setMessage(Messages.VariableSelectorDialog_Variable_Name_wildcards_1); 
		setUpperListLabel(Messages.VariableSelectorDialog_Matches_2); 
		setLowerListLabel(Messages.VariableSelectorDialog_Variable_Type_4); 
		refreshReferenceVariables();
	}
	
	protected void refreshReferenceVariables() {
		variables = BPELUtil.getVisibleReferenceVariables(modelObject);
		setElements(variables);
		if (fFilteredList != null) {
			setListElements(variables);
		}
	}
	
	@Override
	protected FilteredList createFilteredList(Composite parent) {
		FilteredList result = super.createFilteredList(parent);
		result.setComparator(new VariableComparator());
		return result;
	}
	
	@Override
	protected void handleNewButtonPushed() {
		ReferenceVariable variable = BPELFactory.eINSTANCE.createReferenceVariable();
		Process process = BPELUtils.getProcess(modelObject);
		String newName = filterText.getText();
		newName = BPELUtil.generateUniqueModelName(process, newName, variable);
		NameDialog nameDialog = new NameDialog(getShell(), "New ReferenceVariable", "ReferenceVariable Name", 
			newName, BPELUtil.getNCNameValidator());
		if (nameDialog.open() == Window.OK) {
			String newVariableName = nameDialog.getValue();
			CompoundCommand command = new CompoundCommand();
			command.add(new InsertInContainerCommand(process, variable, null));
			command.add(new SetNameCommand(variable, newVariableName));
//			command.add(new SetVariableTypeCommand(variable, variableType));
			ModelHelper.getBPELEditor(process).getCommandStack().execute(command);
			refreshReferenceVariables();
			setFilter(variable.getName());
			setLowerSelectedElement(variable);
			
			// give focus to the dialog's OK button
			getOkButton().setFocus();
		}
	}
	
	/**
	 * Returns a variable from its name.
	 */
	protected ReferenceVariable getReferenceVariable(String name) {
		ReferenceVariable result = (ReferenceVariable)nameToVariable.get(name);
		if (result != null) return result;
		for (int i = 0; i < variables.length; i++) {
			if (name.equals(variables[i].getName())) {
				nameToVariable.put(name, variables[i]);
				return variables[i];
			}
		}
		return null; // should never happen
	}
	
	/**
	 * Returns the chosen variable or null.
	 */
	public ReferenceVariable getReferenceVariable() {
		if (getResult().length > 0) {
			return (ReferenceVariable)getResult()[0];
		}
		return null;
	}
	
	/**
	 * Only used to get the Text instance.
	 */
	@Override
	protected Text createFilterText(Composite parent) {
		filterText = super.createFilterText(parent);
		return filterText;
	}
}
