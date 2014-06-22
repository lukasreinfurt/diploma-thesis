package org.eclipse.bpel.ui.widgets;

import java.util.LinkedList;

import org.eclipse.bpel.ui.widgets.filter.MultipleArgumentDatasourceFilter;
import org.eclipse.bpel.ui.widgets.provider.DataSourceContentProvider;
import org.eclipse.bpel.ui.widgets.provider.DataSourceLabelProvider;
import org.eclipse.bpel.ui.widgets.provider.ExtendedStringArray;
import org.eclipse.bpel.ui.widgets.sorter.ExtendedDataSourceTableSorter;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.simpl.communication.ResourceManagementCommunication;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.simpl.resource.management.data.DataSource;

public class DataSourceTableViewer {

  private TableViewer tableViewer;
  private ExtendedDataSourceTableSorter tableSorter;
  private MultipleArgumentDatasourceFilter dataSourceFilter;

  private LinkedList<Object> tableViewerInput = null;
  private String[] titles = { "Name", "Address", "Type", "Subtype", "Language",
      "API Type", "Driver Name", "Address Prefix", "Data Format", "User",
      "Password" };

  private TableEditor[] editors = null;
  private Text[] textFields = null;

  /**
   * Creates a data source table viewer.
   * 
   * @param composite
   * @param gridData
   */
  public DataSourceTableViewer(Composite composite, GridData gridData) {

    // create the table
    tableViewer = new TableViewer(composite, SWT.SINGLE | SWT.H_SCROLL
        | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
    // create columns
    createColumns(tableViewer);

    tableViewer.setContentProvider(new DataSourceContentProvider());
    tableViewer.setLabelProvider(new DataSourceLabelProvider());

    // create and set input
    // NOTE: the first three rows are used to create an UI for the filter
    tableViewerInput = new LinkedList<Object>();
    // the resources
    tableViewerInput.addAll(ResourceManagementCommunication.getInstance()
        .getDataSources());
    // the third line
    tableViewerInput.addFirst(new ExtendedStringArray(new String[] {
        "Data Sources:", "", "", "", "", "", "", "", "", "", "" }, Display
        .getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND)));
    // the second line (filter values)
    tableViewerInput.addFirst(new ExtendedStringArray(new String[] { "", "",
        "", "", "", "", "", "", "", "", "" }, Display.getDefault()
        .getSystemColor(SWT.COLOR_WHITE)));
    // the first line
    tableViewerInput.addFirst(new ExtendedStringArray(new String[] { "Filter:",
        "", "", "", "", "", "", "", "", "", "" }, Display.getDefault()
        .getSystemColor(SWT.COLOR_WIDGET_BACKGROUND)));
    tableViewer.setInput(tableViewerInput);

    // create editable text fields (for the filter)
    editors = new TableEditor[titles.length];
    textFields = new Text[titles.length];
    for (int i = 0; i < titles.length; i++) {
      // create editor
      editors[i] = new TableEditor(tableViewer.getTable());
      editors[i].horizontalAlignment = SWT.LEFT;
      editors[i].grabHorizontal = true;
    }
    createTextFields();

    tableViewer.getTable().setDragDetect(false);

    // sorter and filter
    tableSorter = new ExtendedDataSourceTableSorter();
    dataSourceFilter = new MultipleArgumentDatasourceFilter(titles.length);

    tableViewer.setSorter(tableSorter);
    tableViewer.addFilter(dataSourceFilter);

    tableViewer.getControl().setLayoutData(gridData);

  }

  /**
   * Adds a double click listener.
   * 
   * @param iDoubleClickListener
   */
  public void addDoubleClickListener(IDoubleClickListener iDoubleClickListener) {
    tableViewer.addDoubleClickListener(iDoubleClickListener);
  }

  /**
   * Disposes the internal table viewer.
   * 
   */
  public void dispose() {
    tableViewer.getTable().dispose();
  }

  /**
   * Returns the actual selection.
   * 
   * @return
   */
  public DataSource getSelection() {
    if (tableViewer.getTable().getSelectionIndex() < 3
        || tableViewer.getTable().getSelection() == null) {
      return null;
    } else {
      return (DataSource) ((IStructuredSelection) tableViewer.getSelection())
          .getFirstElement();
    }
  }

  /**
   * Resets the filter values.
   * 
   */
  public void resetFilterValues() {
    for (int i = 0; i < titles.length; i++) {
      // reset filter values (UI)
      ((ExtendedStringArray) tableViewerInput.get(1)).getStringArray()[i] = "";
      textFields[i].setText("");

    }
    // reset the filter
    dataSourceFilter.resetFilter();
    tableViewer.refresh();
  }

  /**
   * Creates the required columns of the table.
   * 
   * @param tableViewer
   *          the tableViewer
   */
  private void createColumns(final TableViewer tableViewer) {
    int[] bounds = { 125, 200, 100, 100, 100, 100, 100, 100, 100, 100, 100 };

    for (int i = 0; i < titles.length; i++) {
      final int index = i;
      final TableViewerColumn viewerColumn = new TableViewerColumn(tableViewer,
          SWT.NONE);
      final TableColumn column = viewerColumn.getColumn();

      column.setText(titles[i]);
      column.setWidth(bounds[i]);
      column.setResizable(true);
      column.setMoveable(true);

      // when a resize occurs, the text fields for the filter values must be
      // adapted
      column.addListener(SWT.Move, new Listener() {

        @Override
        public void handleEvent(Event arg0) {
          for (int i = 0; i < titles.length; i++) {
            textFields[i].dispose();
          }
          createTextFields();
        }
      });

      // setting the right sorter
      column.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          tableSorter.setColumn(index);
          int dir = tableViewer.getTable().getSortDirection();
          if (tableViewer.getTable().getSortColumn() == column) {
            dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
          } else {
            dir = SWT.DOWN;
          }
          tableViewer.getTable().setSortDirection(dir);
          tableViewer.getTable().setSortColumn(column);
          tableViewer.refresh();
        }
      });
    }

    Table table = tableViewer.getTable();
    table.setHeaderVisible(true);
    table.setLinesVisible(true);
  }

  /**
   * Creates the fields of the filter values.
   * 
   */
  private void createTextFields() {
    final TableItem item = tableViewer.getTable().getItem(1);
    for (int i = 0; i < titles.length; i++) {
      final int column = i;

      // create fields
      textFields[i] = new Text(tableViewer.getTable(), SWT.NONE);
      textFields[i].setBackground(Display.getDefault().getSystemColor(
          SWT.COLOR_WHITE));
      textFields[i].setText(item.getText(i));
      textFields[i].selectAll();

      textFields[i].addKeyListener(new KeyAdapter() {
        public void keyReleased(KeyEvent ke) {
          item.setText(column, textFields[column].getText());
          // add argument (filter)
          dataSourceFilter.addArgument(textFields[column].getText(), column);
          ((ExtendedStringArray) tableViewerInput.get(1)).getStringArray()[column] = textFields[column]
              .getText();
          tableViewer.refresh();
        }
      });

      // mapping
      editors[i].setEditor(textFields[i], item, i);
    }
  }
}
