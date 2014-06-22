package org.eclipse.bpel.ui.widgets.filter;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.simpl.resource.management.data.DataContainer;

public class MultipleArgumentDataContainerFilter extends ViewerFilter {
  // search-arguments
  private String[] searchArguments = null;

  public MultipleArgumentDataContainerFilter(int length) {
    searchArguments = new String[length];
  }

  /**
   * Adds an search-argument.
   * 
   * @param argument
   *          the argument
   * @param position
   *          the column number
   */
  public void addArgument(String argument, int position) {
    // Search must be a substring of the existing value
    searchArguments[position] = ".*" + argument.toLowerCase() + ".*";
  }

  /**
   * Resets the filter.
   * 
   */
  public void resetFilter() {
    for (int i = 0; i < searchArguments.length; i++) {
      searchArguments[i] = null;
    }
  }

  /**
   * Checks whether a specified argument is true.
   * 
   * @param ds
   *          the data container
   * @param argument
   *          the search-argument
   * @param position
   *          the corresponding column number
   * @return
   */
  private boolean checkArgument(DataContainer dc, String argument, int position) {
    switch (position) {
    case 0:
      return !dc.getLogicalName().toLowerCase().matches(argument);
    case 1:
      return !dc.getDataSourceName().toLowerCase().matches(argument);
    case 2:
      return !dc.getLocalIdentifier().toLowerCase().matches(argument);
    default:
      return false;
    }
  }

  /**
   * Verifies whether an object (data source) satisfies all specified
   * conditions.
   * 
   */
  @Override
  public boolean select(Viewer viewer, Object parentElement, Object element) {
    if (element instanceof DataContainer) {
      DataContainer dc = (DataContainer) element;
      boolean success = true;
      for (int i = 0; i < searchArguments.length; i++) {
        if (searchArguments[i] != null) {
          if (checkArgument(dc, searchArguments[i], i)) {
            success = false;
          }
        }
      }
      return success;
    }
    return true;
  }
}
