package org.eclipse.bpel.ui.wizards.simtech;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import org.eclipse.bpel.ui.BPELUIPlugin;
import org.eclipse.bpel.ui.IBPELUIConstants;
import org.eclipse.bpel.ui.simtech.dialogs.MetaDataDialog;
import org.eclipse.bpel.ui.simtech.util.MetaDataUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;


/**
 * Wizard page for the specification of meta-data properties. The page is used
 * if a new BPEL process is created. Note that the specification of at least
 * one property is mandatory.
 * 
 * @author sonntamo
 */
public class NewFileWizardPageMetaData extends WizardPage {

	protected Label propsLabel;

	protected HashMap<String, String> entries;
	protected HashMap<String, ArrayList<String>> existingValues;
	protected Table propsTable;

	Button removeProp;
	Button modifyProp;
	Button newProp;

	/**
	 * Creates a new project creation wizard page.
	 * 
	 * @param pageName
	 *            the name of this page
	 * @param title
	 * 			  the title of this page
	 */
	public NewFileWizardPageMetaData(String pageName, String title) {
		super(pageName);
		setPageComplete(false);
		entries = new HashMap<String, String>();
		setTitle(title == null ? pageName : title);
		setDescription("Specify meta-data that uniquely identifies this process model version.");

		setImageDescriptor(BPELUIPlugin.INSTANCE
				.getImageDescriptor(IBPELUIConstants.ICON_WIZARD_BANNER));
		
	}

	public NewFileWizardPageMetaData(String pageName, String title,
			HashMap<String, String> properties, IFile processFile) {
		super(pageName);
		setPageComplete(false);
		entries = properties;
		setTitle(title == null ? pageName : title);
		setDescription("Specify meta-data for the new process version.");
		setImageDescriptor(BPELUIPlugin.INSTANCE
				.getImageDescriptor(IBPELUIConstants.ICON_WIZARD_BANNER));
		//@schrotbn
		initMetaDataValues(processFile);
	}
	/**
	 * Get all metadata for given process and store them in the local hashmap
	 * @param processFile 
	 * @author schrotbn
	 */
	public void initMetaDataValues(IFile processFile) {
		QName processName = MetaDataUtil.getProcessName(processFile);
		existingValues = new HashMap<String, ArrayList<String>>();
		existingValues.putAll(MetaDataUtil.getAllMetaData(processName));
	}

	/**
	 * Get the specified properties.
	 * 
	 * @return the user-defined properties
	 */
	public HashMap<String, String> getEntries() {
		return entries;
	}

	private void createProjectGroup(Composite parent) {
		Composite fields = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		fields.setLayout(layout);
		fields.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		newProp = new Button(fields, SWT.PUSH);
		newProp.setText("New Property");
		modifyProp = new Button(fields, SWT.PUSH);
		modifyProp.setText("Modify Property");
		removeProp = new Button(fields, SWT.PUSH);
		removeProp.setText("Remove Property");
		modifyProp.setEnabled(false);
		removeProp.setEnabled(false);

		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		data.widthHint = 120;
		data.horizontalAlignment = SWT.LEFT;

		newProp.setLayoutData(data);

		data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		data.widthHint = 120;
		data.horizontalAlignment = SWT.LEFT;
		modifyProp.setLayoutData(data);

		data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		data.widthHint = 120;
		data.horizontalAlignment = SWT.LEFT;

		removeProp.setLayoutData(data);

		propsLabel = new Label(fields, SWT.NONE);
		propsLabel.setText("Properties:");
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 3;
		data.verticalIndent = 20;

		propsLabel.setLayoutData(data);

		// create table
		propsTable = new Table(fields, SWT.FULL_SELECTION | SWT.V_SCROLL
				| SWT.READ_ONLY);
		data = new GridData();

		data.horizontalSpan = 3;
		data.horizontalAlignment = SWT.FILL;
		data.heightHint = 200;
		propsTable.setLayoutData(data);

		// set up table
		propsTable.setLinesVisible(true);
		propsTable.setHeaderVisible(true);

		TableColumn propCol = new TableColumn(propsTable, SWT.NONE);
		propCol.setText("Property");
		propCol.setWidth(240);
		TableColumn valCol = new TableColumn(propsTable, SWT.NONE);
		valCol.setText("Value");
		valCol.setWidth(240);

		for (Entry<String, String> entry : entries.entrySet()) {
			TableItem item = new TableItem(propsTable, SWT.NONE);
			item.setText(new String[] { entry.getKey(), entry.getValue() });
		}

		propsTable.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				modifyProp.setEnabled(e.item != null);
				removeProp.setEnabled(e.item != null);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				System.out.println(e);
			}
		});

		newProp.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				insertNewProp();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		modifyProp.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				modifyProp();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		removeProp.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				removeProp();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

	}

	/**
	 * Method declared on IDialogPage.
	 * 
	 * @param parent
	 *            the parent composite that we must attach ourselves to
	 */
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setFont(parent.getFont());

		initializeDialogUnits(parent);

		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		createProjectGroup(composite);

		setPageComplete(validatePage());
		setControl(composite);
	}

	/**
	 * Returns whether this page's controls currently all contain valid values.
	 * 
	 * @return <code>true</code> if all controls are valid, and
	 *         <code>false</code> if at least one is invalid
	 */
	protected boolean validatePage() {
		setErrorMessage(null);
		setMessage(null);

		if (entries.size() == 0) {
			setErrorMessage("There must be at least one non-empty property-value pair.");
			return false;
		}

		for (String key : entries.keySet()) {
			if (existingValues != null && existingValues.containsKey(key)) {
				if (!existingValues.get(key).contains(entries.get(key))) {
					return true;
				}
			} else {
				return true;
			}
		}
		setErrorMessage("There must be at least one unique property-value pair!");
		return false;
		
	}

	/**
	 * see @DialogPage.setVisible(boolean)
	 * 
	 * @param visible
	 *            whether should be visible or not
	 * 
	 */

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			newProp.setFocus();
		}
	}

	/**
	 * Removes a property from the table and list.
	 */
	void removeProp() {
		if (propsTable.getSelectionCount() == 0)
			return;

		TableItem selection = propsTable.getSelection()[0];

		String oldProp = selection.getText(0);
		entries.remove(oldProp);

		TableItem[] items = propsTable.getItems();
		int index = -1;
		for (int i = 0; i < items.length; i++) {
			TableItem item = items[i];
			if (item.equals(selection)) {
				index = i;
				break;
			}
		}
		if (index != -1) {
			propsTable.remove(index);
		}
		removeProp.setEnabled(false);
		modifyProp.setEnabled(false);
		setPageComplete(validatePage());
	}

	/**
	 * Inserts a new property to the table and list.
	 */
	void insertNewProp() {

		MetaDataDialog dialog = new MetaDataDialog(propsLabel.getShell(),
				entries, existingValues);
		if (dialog.open() == Window.OK) {
			String prop = dialog.getProperty();
			String val = dialog.getValue();

			entries.put(prop, val);
			TableItem item = new TableItem(propsTable, SWT.NONE);
			item.setText(new String[] { prop, val });
		}
		setPageComplete(validatePage());
	}

	/**
	 * Modifies a property in the table and list.
	 */
	void modifyProp() {
		if (propsTable.getSelectionCount() == 0)
			return;

		TableItem selection = propsTable.getSelection()[0];

		String prop = selection.getText(0);
		String val = selection.getText(1);

		MetaDataDialog dialog = new MetaDataDialog(propsLabel.getShell(), prop,
				val, entries, existingValues);
		if (dialog.open() == Window.OK) {
			String newProp = dialog.getProperty();
			String newVal = dialog.getValue();
			String oldProp = dialog.getPropertyToChange();

			entries.remove(oldProp);
			entries.put(newProp, newVal);
			TableItem[] items = propsTable.getItems();
			int index = -1;
			for (int i = 0; i < items.length; i++) {
				TableItem item = items[i];
				if (item.equals(selection)) {
					index = i;
					break;
				}
			}
			if (index != -1) {
				propsTable.remove(index);
				TableItem newItem = new TableItem(propsTable, SWT.NONE, index);
				newItem.setText(new String[] { newProp, newVal });
			}
			removeProp.setEnabled(false);
			modifyProp.setEnabled(false);
			setPageComplete(validatePage());
		}
	}
}
