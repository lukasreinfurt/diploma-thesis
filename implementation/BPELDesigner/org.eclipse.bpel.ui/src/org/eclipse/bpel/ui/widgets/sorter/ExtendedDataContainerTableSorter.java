package org.eclipse.bpel.ui.widgets.sorter;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.simpl.resource.management.data.DataContainer;

public class ExtendedDataContainerTableSorter extends ViewerSorter {
  private int propertyIndex;
  // private static final int ASCENDING = 0;
  private static final int DESCENDING = 1;

  private int direction = DESCENDING;

  public ExtendedDataContainerTableSorter() {
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
    if ((!(e1 instanceof DataContainer)) || (!(e2 instanceof DataContainer))) {
      return 0;
    }
    DataContainer dc1 = (DataContainer) e1;
    DataContainer dc2 = (DataContainer) e2;
    int rc = 0;
    switch (propertyIndex) {
    case 0:
      rc = dc1.getLogicalName().toLowerCase()
          .compareTo(dc2.getLogicalName().toLowerCase());
      break;
    case 1:
      rc = dc1.getDataSourceName().toLowerCase()
          .compareTo(dc2.getDataSourceName().toLowerCase());
      break;
    case 2:
      rc = dc1.getLocalIdentifier().toLowerCase()
          .compareTo(dc2.getLocalIdentifier().toLowerCase());
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
