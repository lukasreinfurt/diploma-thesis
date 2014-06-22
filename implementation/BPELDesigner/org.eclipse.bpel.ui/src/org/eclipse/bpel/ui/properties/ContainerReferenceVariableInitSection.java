package org.eclipse.bpel.ui.properties;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.bpel.model.BPELFactory;
import org.eclipse.bpel.model.BPELPackage;
import org.eclipse.bpel.model.ContainerReferenceVariable;
import org.eclipse.bpel.model.DataSourceReferenceVariable;
import org.eclipse.bpel.model.DataSourceReferenceVariables;
import org.eclipse.bpel.model.From;
import org.eclipse.bpel.ui.commands.SetCommand;
import org.eclipse.bpel.ui.commands.util.UpdateModelCommand;
import org.eclipse.bpel.ui.util.BPELUtil;
import org.eclipse.bpel.ui.util.InitializeReferencesHelper;
import org.eclipse.bpel.ui.util.ModelHelper;
import org.eclipse.bpel.ui.util.MultiObjectAdapter;
import org.eclipse.bpel.ui.widgets.DataContainerListPopUp;
import org.eclipse.core.resources.IMarker;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.commands.Command;
import org.eclipse.simpl.communication.ResourceManagementCommunication;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.xsd.XSDTypeDefinition;
import org.simpl.resource.management.data.DataContainer;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

/**
 * NOTE: This is based on a copy of: org.eclipse.bpel.ui\\src\\org\eclipse\\bpel\\ui\\properties\VariableInitializationSection.java
 *
 */
public class ContainerReferenceVariableInitSection extends BPELPropertySection {

  private ContainerReferenceVariable fVariable;
  private String dataTypeName = "";

  private Composite composite;
  private Composite mainSide;

  private List<Label> elementLabels = new LinkedList<Label>();
  private List<Object> elementTextFields = new LinkedList<Object>();
  private List<Label> attributLabels = new LinkedList<Label>();
  private List<Object> attributTextFields = new LinkedList<Object>();

  private Color white = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);

  private DataContainerListPopUp dataContainerListPopUp = null;

  public ContainerReferenceVariableInitSection() {
    super();
  }

  protected boolean isFromAffected(Notification n) {
    return n.getFeature() == BPELPackage.eINSTANCE
        .getContainerReferenceVariable_From();
  }

  @Override
  protected MultiObjectAdapter[] createAdapters() {
    return new MultiObjectAdapter[] {
    /* model object */
    new MultiObjectAdapter() {

      @Override
      public void notify(Notification n) {
        if (isFromAffected(n)) {
          updateUI();
        }
      }
    }, };
  }

  @Override
  protected void createClient(Composite parent) {
    composite = createFlatFormComposite(parent);
    GridLayout compositeLayout = new GridLayout(1, false);
    composite.setBackground(white);
    composite.setLayout(compositeLayout);

    createMainSideComposite();
  }

  // Total Hack until we have Copy objects in graphical editor
  @Override
  protected void basicSetInput(EObject newInput) {
    super.basicSetInput(newInput);

    fVariable = getModel();
    updateUI();
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
    if (fVariable.getType() != null && !fVariable.getType().equals(dataTypeName)) {
      dataTypeName = fVariable.getName();
      updateUI();
    }
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
   */
  private void updateUI() {
    if (fVariable.getFrom() == null) {
      Command cmd = new SetCommand(getInput(),
          BPELFactory.eINSTANCE.createFrom(),
          BPELPackage.eINSTANCE.getContainerReferenceVariable_From());
      getBPELEditor().getCommandFramework().execute(cmd);
    }

    XSDTypeDefinition xsdType = fVariable.getType();
    if (xsdType != null
        && !xsdType.getName().equals("DataContainerReferenceType")
        && !xsdType.getName().equals("LocalDataContainerReferenceType")) {
      if (!xsdType.getName().equals(dataTypeName)) {
        createTextFields(xsdType);
        updateTextFieldValues();
      } else {
        updateTextFieldValues();
      }
    } else {
      mainSide.dispose();
      createMainSideComposite();

      Label label = new Label(mainSide, SWT.NONE);
      label.setBackground(white);
      label.setText("Please select a data type.");
    }

    Point p = composite.getSize();
    composite.pack();
    composite.setSize(p);
  }

  /**
   * Adds a new composite to the parent composite.
   * 
   */
  private void createMainSideComposite() {
    mainSide = new Composite(composite, SWT.NONE);
    GridLayout mainSideLayout = new GridLayout(3, false);
    mainSide.setBackground(white);
    mainSide.setLayout(mainSideLayout);

    GridData gridDataMainSide = new GridData();
    gridDataMainSide.verticalAlignment = GridData.FILL;
    gridDataMainSide.grabExcessHorizontalSpace = true;
    gridDataMainSide.grabExcessVerticalSpace = true;
    gridDataMainSide.horizontalAlignment = GridData.FILL;
    mainSide.setLayoutData(gridDataMainSide);
  }

  /**
   * Creates the UI.
   * 
   * @param xsdType
   */
  private void createTextFields(XSDTypeDefinition xsdType) {
    mainSide.dispose();
    elementLabels.clear();
    elementTextFields.clear();
    attributLabels.clear();
    attributTextFields.clear();

    createMainSideComposite();

    GridData gridDataLabels = new GridData();
    gridDataLabels.horizontalSpan = 2;
    GridData gridDataTextFields = new GridData(GridData.GRAB_HORIZONTAL
        | GridData.HORIZONTAL_ALIGN_FILL);
    // TODO xsdType.getComplexType().getElement().getChildNodes() fuehrt
    // zu einer NullPointerException, wenn Eigenschaften
    // (DataSourceReferenceVariable) vererbt werden sollen. Aus diesem
    // Grund werden an dieser Stelle alle Kindelemente durchlaufen.
    // (Zukunft: schauen, warum die Exception auftritt)
    //  siehe: org.eclipse.simpl.statementtest\src\org\eclipse\simpl\statementtest\model\variables\ContainerVariable.java
    if (!xsdType.getName().equals("LogicalDataContainerReferenceType")) {

      Label label = new Label(mainSide, SWT.NONE);
      label.setText("dataSourceReferenceVariable");
      label.setBackground(white);
      label.setLayoutData(gridDataLabels);
      elementLabels.add(label);

      CCombo combo = new CCombo(mainSide, SWT.BORDER);
      combo.setLayoutData(gridDataTextFields);
      elementTextFields.add(combo);
      DataSourceReferenceVariables vars = ModelHelper
          .getDataSourceReferenceVariables(getProcess());
      List<String> dsVars = new ArrayList<String>();
      if (vars != null) {
        for (DataSourceReferenceVariable var : vars.getChildren()) {
          dsVars.add(var.getName());
        }
      }
      combo.setItems(dsVars.toArray(new String[0]));
      
      Label labelDataFormat = new Label(mainSide, SWT.NONE);
      labelDataFormat.setText("dataFormat");
      labelDataFormat.setBackground(white);
      labelDataFormat.setLayoutData(gridDataLabels);
      elementLabels.add(labelDataFormat);
      
      CCombo comboDataFormat = new CCombo(mainSide, SWT.BORDER);
      comboDataFormat.setLayoutData(gridDataTextFields);
      elementTextFields.add(comboDataFormat);
        comboDataFormat.setItems(ResourceManagementCommunication
            .getInstance().getDataFormatTypeNames().toArray(new String[0]));
     
      rekNodeList(xsdType.getElement().getChildNodes(), gridDataTextFields,
          gridDataLabels);   
    } else {
      rekNodeList(xsdType.getElement().getChildNodes(), gridDataTextFields,
          gridDataLabels);

      Button browseButton = new Button(mainSide, SWT.NONE);
      browseButton.setText("Browse");
      browseButton.addSelectionListener(new SelectionListener() {

        @Override
        public void widgetSelected(SelectionEvent arg0) {
          dataContainerListPopUp = new DataContainerListPopUp(Display
              .getCurrent().getActiveShell());
          Object result = dataContainerListPopUp.open();
          if (result != null) {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer
                .append("<simpl:container stringPattern=\"logicalIdentifier\" xmlns:simpl=\"http://www.example.org/simpl\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.example.org/simpl simpl.xsd \">");
            stringBuffer.append(System.getProperty("line.separator"));
            stringBuffer.append("  <logicalIdentifier>"
                + ((DataContainer) result).getLogicalName()
                + "</logicalIdentifier>");
            stringBuffer.append(System.getProperty("line.separator"));
            stringBuffer.append("</simpl:container>");
            execute(stringBuffer);
            updateUI();
          }
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent arg0) {
          widgetSelected(arg0);
        }
      });
    }
    Button saveButton = new Button(mainSide, SWT.NONE);
    saveButton.setText("Save");
    saveButton.addSelectionListener(new SelectionListener() {

      @Override
      public void widgetSelected(SelectionEvent arg0) {
        updateFrom();
      }

      @Override
      public void widgetDefaultSelected(SelectionEvent arg0) {
        widgetDefaultSelected(arg0);
      }
    });
  }

  /**
   * Cycles through the elements of the type definition and creates UI
   * components for these elements.
   * 
   * @param nodeList
   * @param gridDataTextFields
   * @param gridDataLabels
   */
  private void rekNodeList(NodeList nodeList, GridData gridDataTextFields,
      GridData gridDataLabels) {
    for (int i = 0; i < nodeList.getLength(); i++) {
      if (nodeList.item(i).hasChildNodes()) {
        rekNodeList(nodeList.item(i).getChildNodes(), gridDataTextFields,
            gridDataLabels);
      }
      NamedNodeMap attributes = null;
      if (nodeList.item(i).hasAttributes()) {
        attributes = nodeList.item(i).getAttributes();
        if (attributes.getNamedItem("name") != null) {
          String value = attributes.getNamedItem("name").getNodeValue();
          Label label = new Label(mainSide, SWT.NONE);
          label.setText(value);
          label.setBackground(white);
          label.setLayoutData(gridDataLabels);

          Text text = new Text(mainSide, SWT.BORDER);
          text.setLayoutData(gridDataTextFields);

          if (value.equals("stringPattern")) {
            text.setBackground(white);
            text.setEditable(false);
            text.setEnabled(false);
            text.setText(attributes.getNamedItem("fixed").getNodeValue());
            text.setVisible(true);
          }

          if (nodeList.item(i).getLocalName().equals("element")) {
            elementLabels.add(label);
            elementTextFields.add(text);
          } else {
            attributLabels.add(label);
            attributTextFields.add(text);
          }
        }
      }
    }
  }

  /**
   * Updates the values of the UI components (label-, text-objects.
   * 
   */
  private void updateTextFieldValues() {
    if (fVariable.getFrom() != null && fVariable.getFrom().getLiteral() != null) {
      String literal = fVariable.getFrom().getLiteral();
      @SuppressWarnings("unused")
      String s;
      for (int i = 0; i < elementLabels.size(); i++) {
        int valueStart = literal.indexOf("<" + elementLabels.get(i).getText()
            + ">")
            + elementLabels.get(i).getText().length() + 2;
        int valueStop = literal.indexOf("</" + elementLabels.get(i).getText()
            + ">", valueStart);

        if (valueStart > -1 && valueStop > -1) {
          String value = literal.substring(valueStart, valueStop);
          if (elementTextFields.get(i) instanceof Text) {
            ((Text) elementTextFields.get(i)).setText(value);
          } else {
            ((CCombo) elementTextFields.get(i)).setText(value);
          }
        }
      }
      for (int i = 0; i < attributLabels.size(); i++) {
        int valueStart = literal.indexOf("<" + attributLabels.get(i).getText()
            + ">")
            + attributLabels.get(i).getText().length() + 2;
        int valueStop = literal.indexOf("</" + attributLabels.get(i).getText()
            + ">", valueStart);

        if (valueStart > -1 && valueStop > -1) {
          String value = literal.substring(valueStart, valueStop);
          if (attributTextFields.get(i) instanceof Text) {
            ((Text) attributTextFields.get(i)).setText(value);
          } else {
            ((CCombo) attributTextFields.get(i)).setText(value);
          }
        }
      }
    }
  }

  /**
   * Creates the fixed value, which should be assigned.
   * 
   */
  private void updateFrom() {
    final StringBuffer stringBuffer = new StringBuffer();
    stringBuffer.append("<simpl:container ");
    for (int i = 0; i < attributLabels.size(); i++) {
      stringBuffer.append(attributLabels.get(i).getText() + "=\"");
      if (attributTextFields.get(i) instanceof Text) {
        stringBuffer.append(((Text) attributTextFields.get(i)).getText()
            + "\" ");
      } else {
        stringBuffer.append(((CCombo) attributTextFields.get(i)).getText()
            + "\" ");
      }
    }
    stringBuffer
        .append("xmlns:simpl=\"http://www.example.org/simpl\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.example.org/simpl simpl.xsd \">");
    stringBuffer.append(System.getProperty("line.separator"));
    for (int i = 0; i < elementLabels.size(); i++) {
      stringBuffer.append("<" + elementLabels.get(i).getText() + ">");
      if (elementTextFields.get(i) instanceof Text) {
        stringBuffer.append(((Text) elementTextFields.get(i)).getText());
      } else {
        stringBuffer.append(((CCombo) elementTextFields.get(i)).getText());
      }
      stringBuffer.append("</" + elementLabels.get(i).getText() + ">");
      stringBuffer.append(System.getProperty("line.separator"));
    }
    stringBuffer.append("</simpl:container>");
    execute(stringBuffer);
  }

  /**
   * Sets the belonging literal.
   * 
   * @param stringBuffer
   */
  private void execute(final StringBuffer stringBuffer) {
    Command cmd = wrapInShowContextCommand(new UpdateModelCommand(
        fVariable.getFrom(), "Modify ..!") {
      @Override
      public void doExecute() {
        From from = BPELUtil.adapt(fVariable.getFrom(), From.class);
        from.setLiteral(stringBuffer.toString());
      }
    });
    cmd.execute();
    InitializeReferencesHelper.initializeReference(getProcess(), fVariable);
  }
}
