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
package org.eclipse.bpel.ui.agora.debug.properties;

import org.eclipse.bpel.debug.debugmodel.Breakpoint;
import org.eclipse.bpel.debug.debugmodel.Breakpoints;
import org.eclipse.bpel.debug.debugmodel.Debug;
import org.eclipse.bpel.debug.debugmodel.DebugPackage;
import org.eclipse.bpel.model.Activity;
import org.eclipse.bpel.model.BPELExtensibleElement;
import org.eclipse.bpel.model.Link;
import org.eclipse.bpel.model.Process;
import org.eclipse.bpel.ui.agora.debug.DebugModelHelper;
import org.eclipse.bpel.ui.agora.debug.commands.AddBreakpointCommand;
import org.eclipse.bpel.ui.expressions.IEditorConstants;
import org.eclipse.bpel.ui.properties.ExpressionSection;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.swt.widgets.Composite;


/**
 * Details section for the CaseCondition of an activity (a boolean expression).
 */
public class BreakpointConditionSection extends ExpressionSection {
	

	@Override
	protected String getExpressionType() { 
		return IEditorConstants.ET_BOOLEAN ; 
	}
	
	
	@Override
	protected Composite createNoEditorWidgets(Composite composite) {
		
		return createNoEditorWidgetsCreateComposite(composite,			
				"This Breakpoint currently does not have a condition specified." + NL + NL +
				"To specify a condition choose the type of the condition from the ComboBox above, or click the button below to create a new expression of the default type for your Process." ,				
				"Create a new condition");
	}
	
	@Override
	protected EStructuralFeature getStructuralFeature (EObject object) {
		return DebugPackage.eINSTANCE.getBreakpoint_Condition();
	}

	@Override
	protected void basicSetInput (EObject newInput) {
		
		Debug debug = getBPELEditor().getDebug();
		Breakpoint breakpoint;
		Breakpoints breakpointsType;
		
		if (newInput != null) {

			String elementName = "";
			String elementXPath = "";

			if (newInput instanceof Process) {
				breakpointsType = debug.getGlobalBreakpoints();

				elementName = getProcess().getName();
				elementXPath = getProcess().getXPath();
			} else {
				breakpointsType = debug.getLocalBreakpoints();

				BPELExtensibleElement element = DebugModelHelper.getElement(newInput);
				if (element instanceof Activity) {
					elementName = ((Activity)element).getName();
					elementXPath = ((Activity)element).getXPath();
				} else if (element instanceof Link){
					elementName = ((Link)element).getName();
					elementXPath = ((Link)element).getXPath();
				}
			}

			breakpoint = DebugModelHelper.getBreakpoint(elementXPath,
					breakpointsType);

			if (breakpoint == null) {
				breakpoint = DebugModelHelper.createNewDefaultBreakpoint(
						elementXPath, elementName);

				getCommandFramework().execute(
						new AddBreakpointCommand(breakpointsType,
								breakpoint));
			}
			
			super.basicSetInput(breakpoint);
		}

		/** Figure out based in the input, what EMF structural feature we are setting */
		setStructuralFeature ( getStructuralFeature (newInput) );
		
		// A different input may have different expression language settings.
		expressionLanguageViewer.refresh(true);
				 
		// Reveal the right selection in the widget.
		updateExpressionLanguageWidgets();		
	}
}
