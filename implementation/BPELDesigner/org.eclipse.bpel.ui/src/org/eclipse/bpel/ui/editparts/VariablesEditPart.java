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

import java.util.Iterator;
import java.util.List;

import org.eclipse.bpel.model.BPELPackage;
import org.eclipse.bpel.model.Variable;
import org.eclipse.bpel.model.Variables;
import org.eclipse.bpel.ui.Messages;
import org.eclipse.bpel.ui.factories.UIObjectFactoryProvider;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.emf.common.util.EList;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.xsd.XSDTypeDefinition;

public class VariablesEditPart extends BPELTrayCategoryEditPart {

  @Override
  protected EList<Variable> getModelChildren() {
    return getVariables().getChildren();
  }

  protected Variables getVariables() {
    return (Variables) getModel();
  }

  @Override
  protected CreationFactory getCreationFactory() {
    return UIObjectFactoryProvider.getInstance().getFactoryFor(
        BPELPackage.eINSTANCE.getVariable());
  }

  @Override
  protected IFigure getAddToolTip() {
    return new Label(Messages.VariablesEditPart_Add_Variable_1);
  }

  @Override
  protected IFigure getRemoveToolTip() {
    return new Label(Messages.VariablesEditPart_Remove_Variable_1);
  }

  @Override
  public void setModel(Object model) {
    // TODO Auto-generated method stub
    super.setModel(model);
  }

  /**
   * Overridden to select a variable that is not a hidden clone variable of a container or
   * data source reference variable, when a variable is removed.
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  protected void removeEntry() {
    super.removeEntry();
    int selectedIndex = 0;
    Variable variable = null;
    XSDTypeDefinition type = null;

    // find selected index
    List selectedParts = getViewer().getSelectedEditParts();
    Iterator<EditPart> iter = selectedParts.iterator();
    EditPart selectedPart = iter.next();
    selectedIndex = this.getModelChildren().indexOf(selectedPart.getModel());

    // select next variable that is not a SIMPL variable clone
    if (selectedIndex > 0) {
      variable = this.getModelChildren().get(selectedIndex);
      type = variable.getType();

      while (type != null
          && (type.getName().equals(ContainerReferenceVariablesEditPart.DATA_TYPE) || type
              .getName().equals(DataSourceReferenceVariablesEditPart.DATA_TYPE))
          && selectedIndex > 0) {
        selectedIndex--;
        variable = this.getModelChildren().get(selectedIndex);
        type = variable.getType();
      }
    }

    selectEditPart(getModelChildren().get(selectedIndex));
  }
}
