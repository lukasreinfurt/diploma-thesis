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

import org.eclipse.bpel.model.BPELPackage;
import org.eclipse.bpel.ui.BPELUIPlugin;
import org.eclipse.bpel.ui.IBPELUIConstants;
import org.eclipse.bpel.ui.adapters.delegates.ReferenceContainer;
import org.eclipse.bpel.ui.editparts.DataSourceReferenceVariablesEditPart;
import org.eclipse.bpel.ui.editparts.OutlineTreeEditPart;
import org.eclipse.bpel.ui.properties.PropertiesLabelProvider;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.swt.graphics.Image;


public class DataSourceReferenceVariablesAdapter extends ContainerAdapter implements EditPartFactory,
	ILabeledElement, IOutlineEditPartFactory, ITrayEditPartFactory
{

	/* IDescriptor delegate */

	@Override
	public IContainer createContainerDelegate() {
		return new ReferenceContainer(BPELPackage.eINSTANCE.getDataSourceReferenceVariables_Children());
	}

	/* EditPartFactory */

	public EditPart createEditPart(EditPart context, Object model) {
		DataSourceReferenceVariablesEditPart result = new DataSourceReferenceVariablesEditPart();
		result.setLabelProvider(PropertiesLabelProvider.getInstance());
		result.setModel(model);
		return result;
	}
		
	/* ITrayEditPartFactory */
	
	public EditPart createTrayEditPart(EditPart context, Object model) {
		return createEditPart(context, model);
	}

	/* ILabeledElement */
	
	public Image getSmallImage(Object object) {
		return BPELUIPlugin.INSTANCE.getImage(IBPELUIConstants.ICON_DATA_SOURCE_REFERENCE_VARIABLE_16);
	}

	public Image getLargeImage(Object object) {
		return BPELUIPlugin.INSTANCE.getImage(IBPELUIConstants.ICON_DATA_SOURCE_REFERENCE_VARIABLE_16);
	}	
	
	public String getTypeLabel(Object object) {
		return "Data Source References";  
	}
	
	public String getLabel(Object object) {
		return "Data Source References"; 
	}	

	/* IOutlineEditPartFactory */
	
	public EditPart createOutlineEditPart(EditPart context, Object model) {
		EditPart result = new OutlineTreeEditPart();
		result.setModel(model);
		return result;
	}
}
