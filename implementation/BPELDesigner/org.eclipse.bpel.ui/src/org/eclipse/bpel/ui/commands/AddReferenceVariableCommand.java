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
import org.eclipse.bpel.ui.IBPELUIConstants;
import org.eclipse.bpel.ui.util.ModelHelper;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;


/**
 * Adds a ReferenceVariable to a process.
 * 
 */
public class AddReferenceVariableCommand extends AddToListCommand {
		
	public AddReferenceVariableCommand (EObject context, ReferenceVariable var) {
		super(ModelHelper.getContainingScope(context), var, IBPELUIConstants.CMD_ADD_REFERENCE_VARIABLE);	
	}
	
	@Override
	protected EList<ReferenceVariable> getList() {
		return ModelHelper.getReferenceVariables( target ).getChildren();		
	}
}
