package org.eclipse.bpel.ui.widgets.provider;

import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.simpl.resource.management.data.DataSource;

public class DataSourceLabelProvider extends LabelProvider implements
    ITableLabelProvider, ITableColorProvider {

  @Override
  public String getColumnText(Object element, int columnIndex) {
    if (element instanceof DataSource) {
      DataSource datasource = (DataSource) element;

      switch (columnIndex) {
      case 0:
        return datasource.getName();
      case 1:
        return datasource.getAddress();
      case 2:
        return datasource.getType();
      case 3:
        return datasource.getSubType();
      case 4:
        return datasource.getLanguage();
      case 5:
        return datasource.getAPIType();
      case 6:
        return datasource.getDriverName();
      case 7:
        return datasource.getAddressPrefix();
      case 8:
        return datasource.getConnector().getDataConverter()
            .getWorkflowDataFormat();
      case 9:
        return datasource.getAuthentication().getUser();
      case 10:
        return datasource.getAuthentication().getPassword()
            .replaceAll(".", "*");
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