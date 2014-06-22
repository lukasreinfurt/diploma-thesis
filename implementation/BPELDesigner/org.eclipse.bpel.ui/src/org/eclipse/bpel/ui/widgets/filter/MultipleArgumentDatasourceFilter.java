package org.eclipse.bpel.ui.widgets.filter;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.simpl.resource.management.data.DataSource;

public class MultipleArgumentDatasourceFilter extends ViewerFilter {

  // search-arguments
  private String[] searchArguments = null;

  public MultipleArgumentDatasourceFilter(int length) {
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
   *          the data source
   * @param argument
   *          the search-argument
   * @param position
   *          the corresponding column number
   * @return
   */
  private boolean checkArgument(DataSource ds, String argument, int position) {
    switch (position) {
    case 0:
      return !ds.getName().toLowerCase().matches(argument);
    case 1:
      return !ds.getAddress().toLowerCase().matches(argument);
    case 2:
      return !ds.getType().toLowerCase().matches(argument);
    case 3:
      return !ds.getSubType().toLowerCase().matches(argument);
    case 4:
      return !ds.getLanguage().toLowerCase().matches(argument);
    case 5:
      return !ds.getAPIType().toLowerCase().matches(argument);
    case 6:
      return !ds.getDriverName().toLowerCase().matches(argument);
    case 7:
      return !ds.getAddressPrefix().toLowerCase().matches(argument);
    case 8:
      return !ds.getConnector().getDataConverter().getWorkflowDataFormat()
          .toLowerCase().matches(argument);
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
    if (element instanceof DataSource) {
      DataSource ds = (DataSource) element;
      boolean success = true;
      for (int i = 0; i < searchArguments.length; i++) {
        if (searchArguments[i] != null) {
          if (checkArgument(ds, searchArguments[i], i)) {
            success = false;
          }
        }
      }
      return success;
    }
    return true;
  }
}
