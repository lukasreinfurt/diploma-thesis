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
package org.eclipse.bpel.ui.adapters;

import java.util.List;

import org.eclipse.bpel.model.adapters.AbstractAdapter;
import org.eclipse.bpel.ui.BPELUIPlugin;
import org.eclipse.bpel.ui.IBPELUIConstants;
import org.eclipse.bpel.ui.actions.editpart.IEditPartAction;
import org.eclipse.bpel.ui.editparts.InstanceStateNodeEditPart;
import org.eclipse.bpel.ui.uiextensionmodel.BPELStates;
import org.eclipse.bpel.ui.uiextensionmodel.InstanceState;
import org.eclipse.bpel.ui.uiextensionmodel.impl.InstanceStateImpl;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.swt.graphics.Image;

/**
 * This class realizes that the state of the actually monitored instance will be
 * shown in a figure in the upper left corner.
 * 
 * @author hahnml
 * 
 */
public class InstanceStateNodeAdapter extends AbstractAdapter implements
		EditPartFactory, ILabeledElement, IEditPartActionContributor {

	/* ILabeledElement */

	public Image getSmallImage(Object object) {
		if (object instanceof InstanceStateImpl) {
			BPELStates state = ((InstanceState) object).getState();
			switch (state) {
			case TERMINATED:
				return BPELUIPlugin.INSTANCE
						.getImage(IBPELUIConstants.ICON_STOP_16);
			case EXECUTING:
				return BPELUIPlugin.INSTANCE
						.getImage(IBPELUIConstants.ICON_RUN_16);
			case SUSPENDED:
				return BPELUIPlugin.INSTANCE
						.getImage(IBPELUIConstants.ICON_SUSPEND_16);
			case COMPLETED:
				return BPELUIPlugin.INSTANCE
						.getImage(IBPELUIConstants.ICON_STOP_16);
			case FAULTED:
				return BPELUIPlugin.INSTANCE
						.getImage(IBPELUIConstants.ICON_FAULT_16);
			case BLOCKING:
				return BPELUIPlugin.INSTANCE
						.getImage(IBPELUIConstants.ICON_BLOCKING_16);
			default:
				return BPELUIPlugin.INSTANCE
						.getImage(IBPELUIConstants.ICON_EMPTY_16);
			}
		}

		return BPELUIPlugin.INSTANCE.getImage(IBPELUIConstants.ICON_EMPTY_16);
	}

	public Image getLargeImage(Object object) {
		// There is no large image for the start node.
		return null;
	}

	public String getLabel(Object object) {
		return getTypeLabel(object);
	}

	public String getTypeLabel(Object object) {
		if (object instanceof InstanceState) {
			return ((InstanceState) object).getState().getLiteral();
		}
		return "Initial";
	}

	/* EditPartFactory */

	public EditPart createEditPart(EditPart context, Object model) {
		EditPart result = new InstanceStateNodeEditPart();
		result.setModel(model);
		return result;
	}

	/* IEditPartActionContributor */

	public List<IEditPartAction> getEditPartActions(final EditPart editPart) {
		return null;
	}
}
