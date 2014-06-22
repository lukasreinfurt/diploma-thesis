package org.eclipse.bpel.ui.simtech.properties;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import org.eclipse.bpel.common.ui.details.IDetailsAreaConstants;
import org.eclipse.bpel.common.ui.flatui.FlatFormAttachment;
import org.eclipse.bpel.common.ui.flatui.FlatFormData;
import org.eclipse.bpel.model.Import;
import org.eclipse.bpel.model.Process;
import org.eclipse.bpel.ui.IHelpContextIds;
import org.eclipse.bpel.ui.details.providers.AbstractContentProvider;
import org.eclipse.bpel.ui.details.providers.ColumnTableProvider;
import org.eclipse.bpel.ui.properties.BPELPropertySection;
import org.eclipse.bpel.ui.simtech.dialogs.MetaDataDialog;
import org.eclipse.bpel.ui.simtech.dialogs.MetaDataProperty;
import org.eclipse.bpel.ui.simtech.dialogs.PropertyColumn;
import org.eclipse.bpel.ui.simtech.dialogs.ValueColumn;
import org.eclipse.bpel.ui.simtech.util.MetaDataUtil;
import org.eclipse.bpel.ui.util.BPELUtil;
import org.eclipse.bpel.ui.util.MultiObjectAdapter;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.PlatformUI;

/**
 * Extends the properties view for BPEL process models. Here, the workflow
 * modeler can specify meta-data properties for BPEL process models. That 
 * way different process model versions can be uniquely addressed with 
 * natural language instead of version identifies.
 * 
 * @author sonntamo
 */
public class MetaDataSection extends BPELPropertySection {

	protected Composite parentComposite;

	protected Label propsLabel;

	protected Table propsTable;

	protected TableViewer propsViewer;

	protected ColumnTableProvider tableProvider;
	
	

	/**
	 * The map of meta-data properties
	 */
	protected HashMap<String, String> propsMap = new HashMap<String, String>();

	/**
	 * Make this section use all the vertical space it can get.
	 * 
	 */
	@Override
	public boolean shouldUseExtraSpace() {
		return true;
	}

	/**
	 * Bug 290085 - Override the super-class because the input is Process not
	 * Import If use super-class's directly, when change the import attributes
	 * the properties section do not change. Grid Qian
	 */
	@Override
	protected void addAllAdapters() {
		super.addAllAdapters();
		if (fAdapters.length > 0) {
			if (getModel() != null) {
				EObject obj = getModel();
				if (obj instanceof Process) {
					List<Import> list = ((Process) obj).getImports();
					for (int i = 0; i < list.size(); i++) {
						fAdapters[0].addToObject((Import) list.get(i));
					}
				}
			}
		}
	}

	@Override
	protected MultiObjectAdapter[] createAdapters() {
		return new MultiObjectAdapter[] { new MultiObjectAdapter() {
			@Override
			public void notify(Notification n) {
				propsViewer.setInput(getInput());
			}
		}, };
	}

	protected void createMetaDataWidgets(Composite parent) {

		FlatFormData data;

		Button newProp = fWidgetFactory.createButton(parent, "New Property",
				SWT.PUSH);
		final Button modifyProp = fWidgetFactory.createButton(parent,
				"Modify Property", SWT.PUSH);
		final Button removeProp = fWidgetFactory.createButton(parent,
				"Remove Property", SWT.PUSH);
		modifyProp.setEnabled(false);
		removeProp.setEnabled(false);

		removeProp.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				removeProp();
			}
		});

		newProp.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				insertNewProp();
			}
		});

		modifyProp.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				modifyProp();
			}

		});

		data = new FlatFormData();
		data.left = new FlatFormAttachment(0,
				IDetailsAreaConstants.HSPACE);
		data.top = new FlatFormAttachment(0, IDetailsAreaConstants.VSPACE);
		newProp.setLayoutData(data);

		data = new FlatFormData();
		data.left = new FlatFormAttachment(newProp,
				IDetailsAreaConstants.HSPACE);
		data.top = new FlatFormAttachment(0, IDetailsAreaConstants.VSPACE);
		modifyProp.setLayoutData(data);
		
		data = new FlatFormData();
		data.left = new FlatFormAttachment(modifyProp,
				IDetailsAreaConstants.HSPACE);
		data.top = new FlatFormAttachment(0, IDetailsAreaConstants.VSPACE);
		removeProp.setLayoutData(data);

		propsLabel = fWidgetFactory.createLabel(parent, "Properties:");
		data = new FlatFormData();
		data.left = new FlatFormAttachment(0, IDetailsAreaConstants.HSPACE);
		data.top = new FlatFormAttachment(newProp, IDetailsAreaConstants.VSPACE);
		propsLabel.setLayoutData(data);

		// create table
		propsTable = fWidgetFactory.createTable(parent, SWT.FULL_SELECTION
				| SWT.V_SCROLL | SWT.READ_ONLY);

		data = new FlatFormData();
		data.left = new FlatFormAttachment(0, IDetailsAreaConstants.HSPACE);
		data.right = new FlatFormAttachment(100, 0);
		data.top = new FlatFormAttachment(propsLabel,
				IDetailsAreaConstants.VSPACE);
		data.bottom = new FlatFormAttachment(100, -IDetailsAreaConstants.HSPACE);
		propsTable.setLayoutData(data);

		// set up table
		propsTable.setLinesVisible(true);
		propsTable.setHeaderVisible(true);

		tableProvider = new ColumnTableProvider();
		tableProvider.add(new PropertyColumn());
		tableProvider.add(new ValueColumn());

		propsViewer = new TableViewer(propsTable);
		tableProvider.createTableLayout(propsTable);
		propsViewer.setLabelProvider(tableProvider);
		propsViewer.setCellModifier(tableProvider);
		propsViewer.setContentProvider(new PropsContentProvider());
		propsViewer.setColumnProperties(tableProvider.getColumnProperties());
		propsViewer.setCellEditors(tableProvider.createCellEditors(propsTable));

		propsViewer
				.addPostSelectionChangedListener(new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent event) {
						removeProp.setEnabled(!event.getSelection().isEmpty());
						modifyProp.setEnabled(!event.getSelection().isEmpty());
					}
				});
	}

	public class PropsContentProvider extends AbstractContentProvider {

		/**
		 * @see org.eclipse.bpel.ui.details.providers.AbstractContentProvider#collectElements(java.lang.Object,
		 *      java.util.List)
		 */
		@Override
		public void collectElements(Object input, List<Object> list) {
			if (input instanceof Process) {
				
				/* 
				 * We load the current meta-data properties from the DD
				 * and insert them into the table of this section. 
				 */
				Process process = (Process) input;
				IFile bpelFile = BPELUtil.getBPELFile(process);
				propsMap = FileOperations.loadPropertiesFromDD(bpelFile.getParent());
				for (Entry<String, String> prop : propsMap.entrySet()) {
					list.add(new MetaDataProperty(prop.getKey(), prop.getValue()));
				}
			}
		}
	}

	@Override
	protected void basicSetInput(EObject newInput) {
		super.basicSetInput(newInput);

		if (getInput() != null) {
			propsViewer.setInput(getInput());
		}
	}

	@Override
	protected void createClient(Composite parent) {
		Composite composite = parentComposite = createFlatFormComposite(parent);

		createMetaDataWidgets(composite);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parentComposite,
				IHelpContextIds.PROPERTY_PAGE_IMPORTS);
	}

	@Override
	public Object getUserContext() {
		return ((StructuredSelection) propsViewer.getSelection())
				.getFirstElement();
	}

	@Override
	public void restoreUserContext(Object userContext) {
		propsTable.setFocus();
		if (userContext != null) {
			propsViewer.setSelection(new StructuredSelection(userContext));
		}
	}

	/**
	 * Deletes a meta-data property for the opened process. 
	 */
	void removeProp() {

		// get the selected element
		ISelection selection = propsViewer.getSelection();
		if (selection.isEmpty()) {
			return;
		}
		IStructuredSelection ssel = null;
		if ((selection instanceof IStructuredSelection) == false) {
			return;
		}

		ssel = (IStructuredSelection) selection;
		Object obj = ssel.getFirstElement();

		// if it is a meta-data property, remove it from the map and 
		// the deployment descriptor
		if (obj instanceof MetaDataProperty) {
			MetaDataProperty mdProp = (MetaDataProperty) obj;
			if (propsMap.containsKey(mdProp.getProperty())) {
				propsMap.remove(mdProp.getProperty());
				FileOperations.storePropertiesToDD(getBPELFile().getParent(), propsMap);
			}
		}
		
		// refresh the property section
		propsViewer.refresh();
	}

	void insertNewProp() {
		QName processName = MetaDataUtil.getProcessName(getBPELFile());
		
		// open the dialog to enter the new meta-data property
		MetaDataDialog dialog = new MetaDataDialog(propsLabel.getShell(), 
				propsMap, MetaDataUtil.getAllMetaData(processName));
		dialog.create();
		if (dialog.open() == Window.OK) {
			String prop = dialog.getProperty();
			String val = dialog.getValue();
			
			// put the new meta-data property to the map and store it
			// in the deployment descriptor
			propsMap.put(prop, val);
			FileOperations.storePropertiesToDD(getBPELFile().getParent(), 
					propsMap);
			
			// refresh the property section
			propsViewer.refresh();
		}
	}

	void modifyProp() {
		
		// get the selected element
		ISelection selection = propsViewer.getSelection();
		if (selection.isEmpty()) {
			return;
		}
		IStructuredSelection ssel = null;
		if ((selection instanceof IStructuredSelection) == false) {
			return;
		}

		ssel = (IStructuredSelection) selection;
		Object obj = ssel.getFirstElement();

		// if it is a meta-data property, modify it
		if (obj instanceof MetaDataProperty) {

			MetaDataProperty mdProp = (MetaDataProperty) obj;
			QName processName = MetaDataUtil.getProcessName(getBPELFile());
			// open the dialog to change a given meta-data property
			MetaDataDialog dialog = new MetaDataDialog(propsLabel
					.getShell(), mdProp.getProperty(), mdProp.getValue(), propsMap, MetaDataUtil.getAllMetaData(processName));
			if (dialog.open() == Window.OK) {
				String prop = dialog.getProperty();
				String val = dialog.getValue();
				
				/*
				 *  Remove the property to modify from the map. We do this
				 *  because the key might be modified and not removing the
				 *  old property would be like inserting a new property.
				 */
				propsMap.remove(mdProp.getProperty());
				
				// put the modified property to the map
				propsMap.put(prop, val);
				
				// store the properties to the DD
				FileOperations.storePropertiesToDD(getBPELFile().getParent(), propsMap);
				
				// refresh the property section
				propsViewer.refresh();
			}
		}
	}

	@Override
	public void gotoMarker(IMarker marker) {
		// TODO Auto-generated method stub
		super.gotoMarker(marker);
	}

	/**
	 * 
	 */
	@Override
	public boolean isValidMarker(IMarker marker) {
		return super.isValidMarker(marker);
	}

}
