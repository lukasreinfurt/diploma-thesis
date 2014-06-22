package org.eclipse.bpel.ui.widgets.sorter;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.simpl.resource.management.data.DataSource;

public class ExtendedDataSourceTableSorter extends ViewerSorter {
  private int propertyIndex;
  // private static final int ASCENDING = 0;
  private static final int DESCENDING = 1;

  private int direction = DESCENDING;

  public ExtendedDataSourceTableSorter() {
    this.propertyIndex = 0;
    direction = DESCENDING;
  }

  public void setColumn(int column) {
    if (column == this.propertyIndex) {
      // Same column as last sort; toggle the direction
      direction = 1 - direction;
    } else {
      // New column; do an ascending sort
      this.propertyIndex = column;
      direction = DESCENDING;
    }
  }

  @Override
  public int compare(Viewer viewer, Object e1, Object e2) {
    // only data sources will be sorted
    if ((!(e1 instanceof DataSource)) || (!(e2 instanceof DataSource))) {
      return 0;
    }
    DataSource ds1 = (DataSource) e1;
    DataSource ds2 = (DataSource) e2;
    int rc = 0;
    switch (propertyIndex) {
    case 0:
      rc = ds1.getName().toLowerCase().compareTo(ds2.getName().toLowerCase());
      break;
    case 1:
      rc = ds1.getAddress().toLowerCase()
          .compareTo(ds2.getAddress().toLowerCase());
      break;
    case 2:
      rc = ds1.getType().toLowerCase().compareTo(ds2.getType().toLowerCase());
      break;
    case 3:
      rc = ds1.getSubType().toLowerCase()
          .compareTo(ds2.getSubType().toLowerCase());
      break;
    case 4:
      rc = ds1.getLanguage().toLowerCase()
          .compareTo(ds2.getLanguage().toLowerCase());
      break;
    case 5:
      rc = ds1.getAPIType().toLowerCase()
          .compareTo(ds2.getAPIType().toLowerCase());
      break;
    case 6:
      rc = ds1.getDriverName().toLowerCase()
          .compareTo(ds2.getDriverName().toLowerCase());
      break;
    case 7:
      rc = ds1.getAddressPrefix().toLowerCase()
          .compareTo(ds2.getAddressPrefix().toLowerCase());
      break;
    case 8:
      rc = ds1
          .getConnector()
          .getDataConverter()
          .getWorkflowDataFormat()
          .toLowerCase()
          .compareTo(
              ds2.getConnector().getDataConverter().getWorkflowDataFormat()
                  .toLowerCase());
      break;
    default:
      rc = 0;
    }
    // If descending order, flip the direction
    if (direction == DESCENDING) {
      rc = -rc;
    }
    return rc;
  }
}
