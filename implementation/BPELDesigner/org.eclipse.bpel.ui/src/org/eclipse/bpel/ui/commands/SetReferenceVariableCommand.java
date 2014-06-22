package org.eclipse.bpel.ui.commands;

import org.eclipse.bpel.model.ReferenceVariable;
import org.eclipse.bpel.ui.IBPELUIConstants;
import org.eclipse.emf.ecore.EObject;

/**
 * <b>Purpose:</b> <br>
 * <b>Description:</b> <br>
 * <b>Copyright:</b>  Licensed under the Apache License, Version 2.0. http://www.apache.org/licenses/LICENSE-2.0<br>
 * <b>Company:</b> SIMPL<br>
 * 
 * @author Michael Hahn <hahnml@studi.informatik.uni-stuttgart.de> <br>
 * @version $Id$ <br>
 * @link http://code.google.com/p/simpl09/
 *
 */
public class SetReferenceVariableCommand extends SetCommand {
	
	public String getDefaultLabel() { return IBPELUIConstants.CMD_SELECT_VARIABLE; }
	
	public SetReferenceVariableCommand(EObject target, ReferenceVariable newVariable)  {
		super(target, newVariable);	
	}

	@Override
	public Object get() {
//		return ModelHelper.getVariable(fTarget);
		return null;
	}
	@Override
	public void set(Object o) {
//		ModelHelper.setVariable(fTarget, (ReferenceVariable)o);
	}
}
