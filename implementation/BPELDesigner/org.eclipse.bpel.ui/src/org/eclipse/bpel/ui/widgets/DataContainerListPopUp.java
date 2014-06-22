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
import org.simpl.resource.management.data.DataSource;

/**
 * This class creates a widget in order to show all registered data containers
 * of a specified data source to the user. The user can choose one of them and
 * after that the chosen data container is given back.
 * 
 */
public class DataContainerListPopUp extends Dialog {

  private Shell shell;

  private DataSourceTableViewer dataSourceTableViewer = null;
  private DataContainerTableViewer dataContainerTableViewer = null;

  private Button dsCancel;
  private Button dsNext;
  private Button dsReset;
  private Label dsNote;

  private Button dcCancel;
  private Button dcOk;
  private Button dcReset;
  private Label dcNote;

  private Object object;

  public DataContainerListPopUp(Shell parent, int style) {
    super(parent, style);
  }

  /**
   * Creates the dialog object.
   * 
   * @param parent
   */
  public DataContainerListPopUp(Shell parent) {
    this(parent, 0);
    setText("Resource Management Data Container Browser");
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

    // create view
    createDSView();

    shell.open();
    Display display = getParent().getDisplay();
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch())
        display.sleep();
    }
    return object;
  }

  private void createDSView() {
    GridData gridDataViewer = new GridData();
    gridDataViewer.horizontalAlignment = GridData.FILL;
    gridDataViewer.verticalAlignment = GridData.FILL;
    gridDataViewer.horizontalSpan = 4;
    gridDataViewer.grabExcessHorizontalSpace = true;
    gridDataViewer.grabExcessVerticalSpace = true;

    dataSourceTableViewer = new DataSourceTableViewer(shell, gridDataViewer);

    IDoubleClickListener iDoubleClickListener = new IDoubleClickListener() {

      @Override
      public void doubleClick(DoubleClickEvent arg0) {
        Object selection = dataSourceTableViewer.getSelection();
        if (selection == null) {
          dsNote.setText("Please select a data source.");
        } else {
          createDCView(((DataSource) selection).getName());
        }
        ;
      }
    };

    dataSourceTableViewer.addDoubleClickListener(iDoubleClickListener);

    // create buttons
    dsNext = new Button(shell, SWT.NONE);
    dsNext.setText("Next");
    dsNext.addSelectionListener(new SelectionListener() {

      public void widgetSelected(SelectionEvent arg0) {
        Object selection = dataSourceTableViewer.getSelection();
        if (selection == null) {
          dsNote.setText("Please select a data source.");
        } else {
          createDCView(((DataSource) selection).getName());
        }
      }

      public void widgetDefaultSelected(SelectionEvent arg0) {
        widgetSelected(arg0);
      }
    });

    dsCancel = new Button(shell, SWT.NONE);
    dsCancel.setText("Cancel");
    dsCancel.addSelectionListener(new SelectionListener() {

      public void widgetSelected(SelectionEvent arg0) {
        shell.dispose();
      }

      public void widgetDefaultSelected(SelectionEvent arg0) {
        widgetSelected(arg0);
      }
    });

    dsReset = new Button(shell, SWT.NONE);
    dsReset.setText("Reset Filters");
    dsReset.addSelectionListener(new SelectionListener() {

      public void widgetSelected(SelectionEvent arg0) {
        dataSourceTableViewer.resetFilterValues();
      }

      public void widgetDefaultSelected(SelectionEvent arg0) {
        widgetSelected(arg0);
      }
    });

    dsNote = new Label(shell, SWT.NONE);
    dsNote.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
    dsNote.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
        | GridData.HORIZONTAL_ALIGN_FILL));
  }

  private void createDCView(String dsName) {
    dataSourceTableViewer.dispose();
    dsCancel.dispose();
    dsNext.dispose();
    dsReset.dispose();
    dsNote.dispose();

    GridData gridDataViewer = new GridData();
    gridDataViewer.horizontalAlignment = GridData.FILL;
    gridDataViewer.verticalAlignment = GridData.FILL;
    gridDataViewer.horizontalSpan = 4;
    gridDataViewer.grabExcessHorizontalSpace = true;
    gridDataViewer.grabExcessVerticalSpace = true;

    dataContainerTableViewer = new DataContainerTableViewer(shell,
        gridDataViewer, dsName);

    IDoubleClickListener iDoubleClickListener = new IDoubleClickListener() {

      @Override
      public void doubleClick(DoubleClickEvent arg0) {
        Object selection = dataContainerTableViewer.getSelection();
        if (selection == null) {
          dcNote.setText("Please select a data container.");
        } else {
          object = selection;
          shell.dispose();
        }
        ;
      }
    };

    dataContainerTableViewer.addDoubleClickListener(iDoubleClickListener);

    // create buttons
    dcOk = new Button(shell, SWT.NONE);
    dcOk.setText("OK");
    dcOk.addSelectionListener(new SelectionListener() {

      public void widgetSelected(SelectionEvent arg0) {
        Object selection = dataContainerTableViewer.getSelection();
        if (selection == null) {
          dcNote.setText("Please select a data container.");
        } else {
          object = selection;
          shell.dispose();
        }
      }

      public void widgetDefaultSelected(SelectionEvent arg0) {
        widgetSelected(arg0);
      }
    });

    dcCancel = new Button(shell, SWT.NONE);
    dcCancel.setText("Cancel");
    dcCancel.addSelectionListener(new SelectionListener() {

      public void widgetSelected(SelectionEvent arg0) {
        shell.dispose();
      }

      public void widgetDefaultSelected(SelectionEvent arg0) {
        widgetSelected(arg0);
      }
    });

    dcReset = new Button(shell, SWT.NONE);
    dcReset.setText("Reset Filters");
    dcReset.addSelectionListener(new SelectionListener() {

      public void widgetSelected(SelectionEvent arg0) {
        dataContainerTableViewer.resetFilterValues();
      }

      public void widgetDefaultSelected(SelectionEvent arg0) {
        widgetSelected(arg0);
      }
    });

    dcNote = new Label(shell, SWT.NONE);
    dcNote.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
    dcNote.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
        | GridData.HORIZONTAL_ALIGN_FILL));

    Point p = shell.getSize();
    shell.pack();
    shell.setSize(p);
  }
}
