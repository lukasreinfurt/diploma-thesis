package org.eclipse.bpel.ui.simtech.dialogs;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Rewrite of MetaDataDialog as subclass of TitleAreaDialog to provide
 * user-feedback in dialog
 * 
 * @author schrotbn
 * 
 */
public class MetaDataDialog extends TitleAreaDialog {

	private String property;

	private String value;

	private String oldProperty = null;

	private String oldValue = null;

	private HashMap<String, String> allProperties;

	private HashMap<String, ArrayList<String>> processProperties;

	private Text propNameText;

	private Text valueText;

	public MetaDataDialog(Shell parent, HashMap<String, String> properties) {
		super(parent);
		this.allProperties = properties;
		processProperties = new HashMap<String, ArrayList<String>>();
	}

	public MetaDataDialog(Shell parent, HashMap<String, String> properties,
			HashMap<String, ArrayList<String>> processProps) {
		this(parent, properties);
		if (processProps != null) {
			processProperties.putAll(processProps);
		}
	}

	public MetaDataDialog(Shell parent, String propertyToChange,
			String oldValue, HashMap<String, String> properties) {
		super(parent);
		this.oldProperty = propertyToChange;
		this.oldValue = oldValue;
		this.allProperties = properties;
		processProperties = new HashMap<String, ArrayList<String>>();
	}

	public MetaDataDialog(Shell parent, String propertyToChange,
			String oldValue, HashMap<String, String> properties,
			HashMap<String, ArrayList<String>> processProps) {
		this(parent, propertyToChange, oldValue, properties);
		if (processProps != null) {
			processProperties.putAll(processProps);
		}
	}

	public String getProperty() {
		return property;
	}

	public String getValue() {
		return value;
	}

	public String getPropertyToChange() {
		return oldProperty;
	}

	@Override
	public void create() {
		setHelpAvailable(false);
		super.create();
		this.getShell().setText("Edit Metadata");
		setTitle("Specify new property for the process model");
		setMessage("Please specify the new property name and value for the process model.");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		container.setLayout(new GridLayout(2, true));
		Label propNameLabel = new Label(container, SWT.NULL);
		propNameLabel.setText("Please enter a unique property name:");
		propNameText = new Text(container, SWT.SINGLE | SWT.BORDER);
		propNameText
				.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		propNameText.setText(oldProperty == null ? "" : oldProperty);

		Label valueLabel = new Label(container, SWT.NULL);
		valueLabel.setText("Please enter a value for that property:");
		valueText = new Text(container, SWT.SINGLE | SWT.BORDER);
		valueText.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		valueText.setText(oldValue == null ? "" : oldValue);

		Listener listener = new Listener() {
			public void handleEvent(Event event) {
				property = propNameText.getText();
				value = valueText.getText();

				if (!"".equals(property) && !"".equals(value)) {
					if ((allProperties.containsKey(property) && !property
							.equals(oldProperty))) {
						setErrorMessage("The property \"" + property
								+ "\" already exists.");
					}
					if (processProperties.containsKey(property)) { // Propertyname
																	// already
																	// defined
						if (processProperties.get(property).contains(value)) { // Property
																				// already
																				// defined
							setErrorMessage("Property \"" + property
									+ "\" with value \"" + value
									+ "\" is already defined!");
							getButton(IDialogConstants.OK_ID).setEnabled(false);
						} else { // New value for property is accepted
							setErrorMessage(null);
							setMessage("Press OK to save the specified property");
							getButton(IDialogConstants.OK_ID).setEnabled(true);
						}
					} else { // New property is always accepted
						setErrorMessage(null);
						setMessage("Press OK to save the specified property");
						getButton(IDialogConstants.OK_ID).setEnabled(true);
					}
				} else {
					setErrorMessage("No name or value given!");
					getButton(IDialogConstants.OK_ID).setEnabled(false);
				}
			}
		};

		propNameText.addListener(SWT.Modify, listener);
		valueText.addListener(SWT.Modify, listener);

		return container;
	}

	@Override
	protected void okPressed() {
		this.property = propNameText.getText();
		this.value = valueText.getText();
		super.okPressed();
	}
}
