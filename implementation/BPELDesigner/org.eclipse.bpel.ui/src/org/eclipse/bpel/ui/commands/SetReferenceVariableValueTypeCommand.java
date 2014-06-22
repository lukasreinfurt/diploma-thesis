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
package org.eclipse.bpel.ui.commands;

import org.eclipse.bpel.model.ReferenceVariable;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xsd.XSDTypeDefinition;


public class SetReferenceVariableValueTypeCommand extends SetCommand {

	public String getDefaultLabel() { return "ReferenceVariable ValueType Change"; }

	public SetReferenceVariableValueTypeCommand(ReferenceVariable target, EObject newVariableType)  {
		super(target, newVariableType);
	}
	
	@Override
	public Object get() {
		return ((ReferenceVariable)fTarget).getValueType();
	}
	
	@Override
	public void set(Object o) {
		ReferenceVariable variable = (ReferenceVariable)fTarget;
		if (o instanceof XSDTypeDefinition) {
			variable.setValueType((XSDTypeDefinition)o);
		} else {
			variable.setValueType(null);
		}
	}
}
