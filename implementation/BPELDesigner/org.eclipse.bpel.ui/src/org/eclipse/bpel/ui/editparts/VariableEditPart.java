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

import org.eclipse.bpel.model.Variable;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.xsd.XSDTypeDefinition;

public class VariableEditPart extends BPELTrayCategoryEntryEditPart {
  /**
   * Overridden to hide the variable clones of container and data source reference variables in the
   * GUI when the BPEL Editor is opened.
   */
  @Override
  public IFigure createFigure() {
    IFigure figure = super.createFigure();
    Variable variable = (Variable) this.getModel();
    XSDTypeDefinition type = variable.getType();

    if (type != null && type.getRootType() != null) {
      type = type.getRootType();
    }
    
    if (type != null
        && (type.getName().equals(ContainerReferenceVariablesEditPart.DATA_TYPE) || type.getName()
            .equals(DataSourceReferenceVariablesEditPart.DATA_TYPE))) {
      figure.setFocusTraversable(false);
      figure.setRequestFocusEnabled(false);
      figure.setEnabled(false);
      figure.setForegroundColor(new Color(Display.getCurrent(), 211, 211, 211));
      figure.setPreferredSize(new Dimension(0, 0));
      figure.setVisible(false);
    }

    return figure;
  }
}
