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

import org.eclipse.bpel.model.ReferenceType;
import org.eclipse.bpel.model.ReferenceVariable;


public class SetReferenceVariableReferenceTypeCommand extends SetCommand {

	public String getDefaultLabel() { return "ReferenceVariable ReferenceType Change"; }

	public SetReferenceVariableReferenceTypeCommand(ReferenceVariable target, ReferenceType referenceType)  {
		super(target, referenceType);
	}
	
	@Override
	public Object get() {
		return ((ReferenceVariable)fTarget).getReferenceType();
	}
	
	@Override
	public void set(Object o) {
		ReferenceVariable variable = (ReferenceVariable)fTarget;
		if (o instanceof ReferenceType) {
			variable.setReferenceType((ReferenceType)o);
		}
	}
}
