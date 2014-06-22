package org.eclipse.bpel.ui.properties;

import org.eclipse.bpel.model.BPELFactory;

import org.eclipse.bpel.model.BPELPackage;
import org.eclipse.bpel.model.DataSourceReferenceVariable;
import org.eclipse.bpel.model.From;
import org.eclipse.bpel.ui.commands.SetCommand;
import org.eclipse.bpel.ui.commands.util.UpdateModelCommand;
import org.eclipse.bpel.ui.util.BPELUtil;
import org.eclipse.bpel.ui.util.InitializeReferencesHelper;
import org.eclipse.bpel.ui.util.MultiObjectAdapter;
import org.eclipse.bpel.ui.widgets.DataSourceListPopUp;
import org.eclipse.core.resources.IMarker;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.commands.Command;
import org.eclipse.simpl.communication.ResourceManagementCommunication;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.simpl.resource.management.data.DataSource;

/**
 * NOTE: This is based on a copy of: org.eclipse.bpel.ui\\src\\org\eclipse\\bpel\\ui\\properties\VariableInitializationSection.java
 *
 */
public class DataSourceReferenceVariableInitSection extends BPELPropertySection {

  final String before = "<dataSourceReference xmlns:simpl=\"http://www.example.org/simpl\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.example.org/simpl simpl.wsdl\">"
      + System.getProperty("line.separator") + "  <name>";
  final String after = "</name>" + System.getProperty("line.separator")
      + "  <requirements/>" + System.getProperty("line.separator")
      + "  <strategy/>" + System.getProperty("line.separator")
      + "</dataSourceReference>";

  private DataSourceReferenceVariable fVariable;

  private DataSourceListPopUp dataSourceListPopUp = null;

  private Color white = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);

  private String[] titles = { "Address", "Type", "Subtype", "Language",
      "API Type", "Data Format" };

  private Text dataSourceText = null;
  private Label[] rightSideLabels = null;
  private Text[] rightSideTextFields;

  public DataSourceReferenceVariableInitSection() {
    super();
  }

  protected boolean isFromAffected(Notification n) {
    return n.getFeature() == BPELPackage.eINSTANCE
        .getDataSourceReferenceVariable_From();
  }

  @Override
  protected MultiObjectAdapter[] createAdapters() {
    return new MultiObjectAdapter[] {
    /* model object */
    new MultiObjectAdapter() {

      @Override
      public void notify(Notification n) {
        if (isFromAffected(n)) {
          updateUI(null);
        }
      }
    }, };
  }

  @Override
  protected void createClient(Composite parent) {
    rightSideLabels = new Label[titles.length];
    rightSideTextFields = new Text[titles.length];

    Composite composite = createFlatFormComposite(parent);
    GridLayout compositeLayout = new GridLayout(2, true);
    composite.setBackground(white);
    composite.setLayout(compositeLayout);

    Composite leftSide = new Composite(composite, SWT.NONE);
    Composite rightSide = new Composite(composite, SWT.NONE);

    leftSide.setBackground(white);
    rightSide.setBackground(white);

    GridLayout leftSideLayout = new GridLayout(4, false);
    GridLayout rightSideLayout = new GridLayout(2, false);

    leftSide.setLayout(leftSideLayout);
    rightSide.setLayout(rightSideLayout);

    GridData gridDataLeftSide = new GridData();
    gridDataLeftSide.verticalAlignment = GridData.FILL;
    gridDataLeftSide.grabExcessHorizontalSpace = true;
    gridDataLeftSide.grabExcessVerticalSpace = true;
    gridDataLeftSide.horizontalAlignment = GridData.FILL;
    leftSide.setLayoutData(gridDataLeftSide);

    GridData gridDataRightSide = new GridData();
    gridDataRightSide.verticalAlignment = GridData.FILL;
    gridDataRightSide.grabExcessHorizontalSpace = true;
    gridDataRightSide.grabExcessVerticalSpace = true;
    gridDataRightSide.horizontalAlignment = GridData.FILL;
    rightSide.setLayoutData(gridDataLeftSide);

    GridData gridDataTextFields = new GridData(GridData.GRAB_HORIZONTAL
        | GridData.HORIZONTAL_ALIGN_FILL);

    Label dataSourceLabel = new Label(leftSide, SWT.NONE);
    dataSourceLabel.setText("Data Source:");
    dataSourceLabel.setBackground(white);
    dataSourceText = new Text(leftSide, SWT.BORDER);
    dataSourceText.setLayoutData(gridDataTextFields);

    Button browseButton = new Button(leftSide, SWT.NONE);
    browseButton.setText("Browse");
    browseButton.addSelectionListener(new SelectionListener() {

      public void widgetSelected(SelectionEvent arg0) {
        dataSourceListPopUp = new DataSourceListPopUp(Display.getCurrent()
            .getActiveShell());
        Object result = dataSourceListPopUp.open();
        updateFrom((DataSource) result);
      }

      public void widgetDefaultSelected(SelectionEvent arg0) {
        widgetSelected(arg0);
      }
    });

    Button saveButton = new Button(leftSide, SWT.NONE);
    saveButton.setText("Save");
    saveButton.addSelectionListener(new SelectionListener() {

      @Override
      public void widgetSelected(SelectionEvent arg0) {
        DataSource ds = ResourceManagementCommunication.getInstance()
            .findDataSourceByName(dataSourceText.getText());
        if (ds != null) {
          updateFrom(ds);
        }
      }

      @Override
      public void widgetDefaultSelected(SelectionEvent arg0) {
        widgetSelected(arg0);
      }
    });

    for (int i = 0; i < 6; i++) {
      rightSideLabels[i] = new Label(rightSide, SWT.NONE);
      rightSideLabels[i].setText(titles[i]);
      rightSideLabels[i].setBackground(white);
      rightSideTextFields[i] = new Text(rightSide, SWT.BORDER);
      rightSideTextFields[i].setEditable(false);
      rightSideTextFields[i].setEnabled(false);
      rightSideTextFields[i].setLayoutData(gridDataTextFields);
      rightSideTextFields[i].setBackground(white);

    }
  }

  // Total Hack until we have Copy objects in graphical editor
  @Override
  protected void basicSetInput(EObject newInput) {
    super.basicSetInput(newInput);

    fVariable = getModel();
    updateUI(null);
  }

  /**
   * @see org.eclipse.ui.views.properties.tabbed.AbstractPropertySection#shouldUseExtraSpace()
   */
  @Override
  public boolean shouldUseExtraSpace() {
    return true;
  }

  /**
   * @see org.eclipse.bpel.ui.properties.BPELPropertySection#refresh()
   */
  @Override
  public void refresh() {
    super.refresh();
  }

  /**
   * @see org.eclipse.bpel.ui.properties.BPELPropertySection#aboutToBeHidden()
   */
  @Override
  public void aboutToBeHidden() {
    super.aboutToBeHidden();
  }

  /**
   * @see org.eclipse.bpel.ui.properties.BPELPropertySection#aboutToBeShown()
   */
  @Override
  public void aboutToBeShown() {
    super.aboutToBeShown();
  }

  /**
   * @see org.eclipse.bpel.ui.properties.BPELPropertySection#getUserContext()
   */
  @Override
  public Object getUserContext() {
    return null;
  }

  /**
   * @see org.eclipse.bpel.ui.properties.BPELPropertySection#restoreUserContext(java.lang.Object)
   */
  @SuppressWarnings("boxing")
  @Override
  public void restoreUserContext(Object userContext) {

  }

  /**
   * @see org.eclipse.bpel.ui.properties.BPELPropertySection#gotoMarker(org.eclipse.core.resources.IMarker)
   */
  @Override
  public void gotoMarker(IMarker marker) {
    refresh();
  }

  /**
   * Updates the UI.
   * 
   * @param dataSource
   */
  private void updateUI(DataSource dataSource) {
    DataSource ds = dataSource;
    if (fVariable.getFrom() == null) {
      Command cmd = new SetCommand(getInput(),
          BPELFactory.eINSTANCE.createFrom(),
          BPELPackage.eINSTANCE.getDataSourceReferenceVariable_From());
      // Execute this right away.
      getBPELEditor().getCommandFramework().execute(cmd);
    }
    if (fVariable.getFrom() != null && fVariable.getFrom().getLiteral() != null) {
      String literal = fVariable.getFrom().getLiteral();
      int start = literal.indexOf("<name>") + 6;
      int end = literal.indexOf("</name>");

      dataSourceText.setText(literal.substring(start, end));

      if (ds == null) {
        ds = ResourceManagementCommunication.getInstance()
            .findDataSourceByName(dataSourceText.getText());
      }
      for (int i = 0; i < rightSideTextFields.length; i++) {
        if (i == 0) {
          rightSideTextFields[i].setText(ds.getAddress());
        } else if (i == 1) {
          rightSideTextFields[i].setText(ds.getType());
        } else if (i == 2) {
          rightSideTextFields[i].setText(ds.getSubType());
        } else if (i == 3) {
          rightSideTextFields[i].setText(ds.getLanguage());
        } else if (i == 4) {
          rightSideTextFields[i].setText(ds.getAPIType());
        } else if (i == 5) {
          rightSideTextFields[i].setText(ds.getConnector().getDataConverter()
              .getWorkflowDataFormat());
        }

      }
    }
  }
  
  /**
   * Creates the fixed value and sets the belonging literal.
   * 
   * @param dataSource
   */
  private void updateFrom(final DataSource dataSource) {
    if (dataSource != null) {
      Command cmd = wrapInShowContextCommand(new UpdateModelCommand(
          fVariable.getFrom(), "Modify ..!") {
        @Override
        public void doExecute() {
          From from = BPELUtil.adapt(fVariable.getFrom(), From.class);
          from.setLiteral(before + dataSource.getName() + after);
        }
      });
      cmd.execute();
      InitializeReferencesHelper.initializeReference(getProcess(), fVariable);
      updateUI(dataSource);
    }
  }
}
