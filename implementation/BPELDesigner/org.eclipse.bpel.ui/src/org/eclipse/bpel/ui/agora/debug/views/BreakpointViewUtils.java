package org.eclipse.bpel.ui.agora.debug.views;

import org.eclipse.bpel.debug.debugmodel.BreakpointTypeEnum;
import org.eclipse.emf.common.util.EList;

public class BreakpointViewUtils {

	public static String getStringRepresentation(EList<BreakpointTypeEnum> types) {
		String text = "";
		
		for (BreakpointTypeEnum type : types) {
			text += type.getName();
			
			if (types.indexOf(type) != types.size()-1) {
				text += ", ";
			}
		}
		
		return text;
	}
}
