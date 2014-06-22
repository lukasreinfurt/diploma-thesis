package org.eclipse.bpel.ui.widgets.provider;

import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.simpl.resource.management.data.DataContainer;

public class DataContainerLabelProvider extends LabelProvider implements
    ITableLabelProvider, ITableColorProvider {

  @Override
  public String getColumnText(Object element, int columnIndex) {
    if (element instanceof DataContainer) {
      DataContainer dataContainer = (DataContainer) element;

      switch (columnIndex) {
      case 0:
        return dataContainer.getLogicalName();
      case 1:
        return dataContainer.getDataSourceName();
      case 2:
        return dataContainer.getLocalIdentifier().replaceAll(">\\s*<", "><");
      default:
        throw new RuntimeException("Too many columns.");
      }

    } else if (element instanceof ExtendedStringArray) {

      return ((ExtendedStringArray) element).getStringArray()[columnIndex];
    }

    return null;
  }

  @Override
  public Image getColumnImage(Object element, int columnIndex) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Color getBackground(Object arg0, int arg1) {
    if (arg0 instanceof org.simpl.resource.management.data.DataSource) {
      return Display.getDefault().getSystemColor(SWT.COLOR_WHITE);
    } else if (arg0 instanceof ExtendedStringArray) {
      return ((ExtendedStringArray) arg0).getColor();
    }
    return null;
  }

  @Override
  public Color getForeground(Object arg0, int arg1) {
    return Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
  }
}
