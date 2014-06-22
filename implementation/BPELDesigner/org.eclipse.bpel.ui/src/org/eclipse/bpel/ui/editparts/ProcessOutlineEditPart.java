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
package org.eclipse.bpel.ui.editparts;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.bpel.model.BPELPackage;
import org.eclipse.bpel.model.ContainerReferenceVariables;
import org.eclipse.bpel.model.CorrelationSets;
import org.eclipse.bpel.model.DataSourceReferenceVariables;
import org.eclipse.bpel.model.BPELExtensibleElement;
import org.eclipse.bpel.model.MessageExchanges;
import org.eclipse.bpel.model.PartnerLinks;
import org.eclipse.bpel.model.Process;
import org.eclipse.bpel.model.ReferenceVariables;
import org.eclipse.bpel.model.Variable;
import org.eclipse.bpel.model.Variables;
import org.eclipse.bpel.ui.adapters.IContainer;
import org.eclipse.bpel.ui.adapters.delegates.ActivityContainer;
import org.eclipse.xsd.XSDTypeDefinition;

public class ProcessOutlineEditPart extends OutlineTreeEditPart {

	// protected ReferencePartnerLinks referencePartners =
	// UiextensionmodelFactory.eINSTANCE.createReferencePartnerLinks();

	@Override
	protected void createEditPolicies() {
		super.createEditPolicies();
		// installEditPolicy(EditPolicy.LAYOUT_ROLE, new
		// ProcessLayoutEditPolicy());
	}

	@Override
	protected void addAllAdapters() {
		super.addAllAdapters();
		if (getModel() instanceof Process) {
			Process process = (Process) getModel();
			if (process.getVariables() != null)
				adapter.addToObject(process.getVariables());
			if (process.getReferenceVariables() != null)
				adapter.addToObject(process.getReferenceVariables());
			if (process.getContainerReferenceVariables() != null)
				adapter.addToObject(process.getContainerReferenceVariables());
			if (process.getDataSourceReferenceVariables() != null)
				adapter.addToObject(process.getDataSourceReferenceVariables());
			if (process.getPartnerLinks() != null)
				adapter.addToObject(process.getPartnerLinks());
			if (process.getCorrelationSets() != null)
				adapter.addToObject(process.getCorrelationSets());
			if (process.getMessageExchanges() != null)
				adapter.addToObject(process.getMessageExchanges());
		}
	}

	@Override
	protected List<BPELExtensibleElement> getModelChildren() {
		Process process = (Process)getModel();
		List<BPELExtensibleElement> list = new ArrayList<BPELExtensibleElement>();

		PartnerLinks links = process.getPartnerLinks();
		if (links != null) {
			list.add(links);
			//
			// referencePartners.setPartnerLinks(links);
			// list.add(referencePartners);
		}

		Variables variables = process.getVariables();

		if (variables != null) {
			// SIMPL: filter container reference and data source reference
			// variables as they should not be visible in the outline
			List<BPELExtensibleElement> cleanedVariables = new ArrayList<BPELExtensibleElement>();
			for (Variable variable : variables.getChildren()) {
				XSDTypeDefinition type = variable.getType();
				if (type != null) {
          if (type.getBaseType() != null && !type.getBaseType().getName().equals(ContainerReferenceVariablesEditPart.DATA_TYPE)
              && !type.getName().equals(DataSourceReferenceVariablesEditPart.DATA_TYPE)) {
            cleanedVariables.add(variable);
          }
        } else {
          cleanedVariables.add(variable);
        }
			}

			list.addAll(cleanedVariables);
		}

		ReferenceVariables referenceVariables = process.getReferenceVariables();
		if (referenceVariables != null) {
			list.add(referenceVariables);
		}

		ContainerReferenceVariables containerReferenceVariables = process
				.getContainerReferenceVariables();
		if (containerReferenceVariables != null) {
			list.add(containerReferenceVariables);
		}

		DataSourceReferenceVariables dataSourceReferenceVariables = process
				.getDataSourceReferenceVariables();
		if (dataSourceReferenceVariables != null) {
			list.add(dataSourceReferenceVariables);
		}

		CorrelationSets sets = process.getCorrelationSets();
		if (sets != null) {
			list.add(sets);
		}

		MessageExchanges exchanges = process.getMessageExchanges();
		if (exchanges != null) {
			list.add(exchanges);
		}

		IContainer container = new ActivityContainer(
				BPELPackage.eINSTANCE.getProcess_Activity());
		List list2 = container.getChildren(process);
		list.addAll(list2);
		return list;
	}
}
