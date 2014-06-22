/**
 * 
 */
package org.eclipse.bpel.ui.agora;

import org.eclipse.bpel.model.Link;
import org.eclipse.bpel.model.impl.BPELExtensibleElementImpl;
import org.eclipse.bpel.ui.BPELUIPlugin;
import org.eclipse.bpel.ui.IBPELUIConstants;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.swt.graphics.Color;
import org.simtech.workflow.ode.auditing.agora.BPELStates;

/**
 * Helper class for Agora
 * 
 * @author aeichel
 * 
 */
public class AgoraHelper {

	public static Color colorFigure(Object model){
		return colorFigure(model, false);
	}
	
	public static Color colorFigure(Object model, boolean value) {
		ColorRegistry registry = BPELUIPlugin.INSTANCE.getColorRegistry();
		Color resultColor = registry.get(IBPELUIConstants.COLOR_WHITE);
		if(value){
			resultColor = registry.get(IBPELUIConstants.COLOR_BLACK);
		}

		if (model instanceof BPELExtensibleElementImpl) {
			BPELExtensibleElementImpl extensibleElement = (BPELExtensibleElementImpl) model;

			if (extensibleElement.getState() != null && extensibleElement.getState().compareTo("")!=0) {
				
				BPELStates state = BPELStates.Inactive;
				try {
					state = BPELStates.valueOf(extensibleElement.getState());
				} catch (IllegalArgumentException e) {
					System.out.println("Error" + extensibleElement.getState());
				}
				switch (state) {
				case Initial:
				case Inactive:
				case Ready:
					if(value){
						resultColor = registry.get(IBPELUIConstants.COLOR_BLACK);
					} else{
						resultColor = registry.get(IBPELUIConstants.COLOR_WHITE);
					}
					break;
				case Executing:
				case EventHandling:
				case Waiting:
					resultColor = registry.get(IBPELUIConstants.COLOR_YELLOW);
					break;
				case Completed:
					resultColor = registry.get(IBPELUIConstants.COLOR_GREEN);
					break;
				case Finished:
					resultColor = registry.get(IBPELUIConstants.COLOR_GREEN);
					break;
				case TerminationHandling:
				case Terminated:
				case FaultHandling:
				case Faulted:
					resultColor = registry.get(IBPELUIConstants.COLOR_RED);
					break;
				case CompletedWithFault:
					resultColor = registry.get(IBPELUIConstants.COLOR_RED);
					break;
				case CompensationExecuting:
					resultColor = registry.get(IBPELUIConstants.COLOR_YELLOW);
					break;
				case Compensated:
					resultColor = registry.get(IBPELUIConstants.COLOR_ACTIVITY_COMPENSATED);
					break;
				case Blocking:
					resultColor = registry.get(IBPELUIConstants.COLOR_ACTIVITY_BLOCKED);
					break;
				case DeadPath:
					resultColor = registry.get(IBPELUIConstants.COLOR_DARK_GRAY);
					break;
				}

			}

		}
		return resultColor;
	}
	
	//@hahnml
	public static Color colorLink (Object model){
		ColorRegistry registry = BPELUIPlugin.INSTANCE.getColorRegistry();
		Color resultColor = registry.get(IBPELUIConstants.COLOR_BLACK);

		if (model instanceof Link) {
			Link link = (Link) model;

			if (link.getState() != null && link.getState().compareTo("")!=0) {
				
				LinkStates state = LinkStates.Inactive;
				try {
					state = LinkStates.valueOf(link.getState());
				} catch (IllegalArgumentException e) {
					System.out.println("Error" + link.getState());
				}
				
				switch (state) {
				case Inactive:
					resultColor = registry.get(IBPELUIConstants.COLOR_LINK_TWO);
					break;
				case Link_Ready:
					resultColor = registry.get(IBPELUIConstants.COLOR_WHITE);
					break;
				case Link_Evaluated:
					resultColor = registry.get(IBPELUIConstants.COLOR_YELLOW);
					break;
				case Link_Set_False:
					resultColor = registry.get(IBPELUIConstants.COLOR_RED);
					break;
				case Link_Set_True:
					resultColor = registry.get(IBPELUIConstants.COLOR_GREEN);
					break;
				case Link_Blocking:
					resultColor = registry.get(IBPELUIConstants.COLOR_ACTIVITY_BLOCKED);
					break;
				}

			}

		}
		return resultColor;
	}
}
