package org.eclipse.bpel.ui.widgets;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * This class creates a widget in order to show all data sources to the user.
 * The user can choose one of them and after that the chosen data source is
 * given back.
 * 
 */
public class DataSourceListPopUp extends Dialog {

  private Shell shell;
  private Label note;

  private DataSourceTableViewer dataSourceTableViewer = null;

  private Object object;

  public DataSourceListPopUp(Shell parent, int style) {
    super(parent, style);
  }
  
  /**
   * Creates the dialog object.
   * 
   * @param parent
   */
  public DataSourceListPopUp(Shell parent) {
    this(parent, 0);
    setText("Resource Management Data Source Browser");
  }

  /**
   * Creates the required UI components and opens the shell.
   * 
   * @return
   */
  public Object open() {
    GridLayout gridLayout = new GridLayout(4, false);
    shell = new Shell(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);
    shell.setBackground(Display.getCurrent().getSystemColor(
        SWT.COLOR_WIDGET_BACKGROUND));
    shell.setLayout(gridLayout);
    shell.setSize(new Point(600, 480));
    shell.setText(getText());
    //
    // layout the viewer
    GridData gridDataViewer = new GridData();
    gridDataViewer.verticalAlignment = GridData.FILL;
    gridDataViewer.horizontalSpan = 4;
    gridDataViewer.grabExcessHorizontalSpace = true;
    gridDataViewer.grabExcessVerticalSpace = true;
    gridDataViewer.horizontalAlignment = GridData.FILL;

    dataSourceTableViewer = new DataSourceTableViewer(shell, gridDataViewer);

    IDoubleClickListener iDoubleClickListener = new IDoubleClickListener() {

      @Override
      public void doubleClick(DoubleClickEvent arg0) {
        Object selection = dataSourceTableViewer.getSelection();
        if (selection == null) {
          note.setText("Please select a data source.");
        } else {
          object = selection;
          shell.dispose();
        }
        ;
      }
    };
    dataSourceTableViewer.addDoubleClickListener(iDoubleClickListener);

    // create buttons
    Button buttonCancel = new Button(shell, SWT.NONE);
    buttonCancel.setText("Cancel");
    buttonCancel.setSelection(true);
    buttonCancel.addSelectionListener(new SelectionListener() {

      public void widgetSelected(SelectionEvent arg0) {
        shell.dispose();
      }

      public void widgetDefaultSelected(SelectionEvent arg0) {
        widgetSelected(arg0);
      }
    });

    Button buttonOK = new Button(shell, SWT.NONE);
    buttonOK.setText("OK");
    buttonOK.addSelectionListener(new SelectionListener() {

      public void widgetSelected(SelectionEvent arg0) {
        Object selection = dataSourceTableViewer.getSelection();
        if (selection == null) {
          note.setText("Please select a data source.");
        } else {
          object = selection;
          shell.dispose();
        }
      }

      public void widgetDefaultSelected(SelectionEvent arg0) {
        widgetSelected(arg0);
      }
    });

    Button buttonReset = new Button(shell, SWT.NONE);
    buttonReset.setText("Reset Filters");
    buttonReset.addSelectionListener(new SelectionListener() {

      public void widgetSelected(SelectionEvent arg0) {
        dataSourceTableViewer.resetFilterValues();
      }

      public void widgetDefaultSelected(SelectionEvent arg0) {
        widgetSelected(arg0);
      }
    });

    note = new Label(shell, SWT.NONE);
    note.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
    note.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
        | GridData.HORIZONTAL_ALIGN_FILL));
    //
    shell.open();
    Display display = getParent().getDisplay();
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch())
        display.sleep();
    }
    return object;
  }
}
