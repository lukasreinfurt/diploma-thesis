package org.eclipse.bpel.ui.wizards.simtech.table;

import java.util.Arrays;
import java.util.List;

import org.eclipse.bpel.model.simtech.WSDL;
import org.eclipse.bpel.ui.wizards.simtech.FragmentUtils;
import org.eclipse.bpel.ui.wizards.simtech.documents.WSDLImpl;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * 
 * @author sonntamo
 */
public class WSDLLabelProvider extends LabelProvider implements
		ITableLabelProvider {
	
	// Set the table column property names
	private static final String COLUMN_FILENAME = "Filename";
	private static final String COLUMN_NAME = "Name";
	private static final String COLUMN_TNS = "Target Namespace";
	private static final String COLUMN_CHECK = "";
	
	//@hahnml
	private static final String COLUMN_EXISTS = "Exists Already";
	
	// Set column names
	private static String[] columnNames = new String[] { COLUMN_CHECK, COLUMN_FILENAME,
		COLUMN_NAME, COLUMN_TNS };

	public static List<String> getColumnNames() {
		return Arrays.asList(columnNames);
	}
		
	// Names of images used to represent checkboxes
	public static final String CHECKED_IMAGE = "checked";
	public static final String UNCHECKED_IMAGE = "unchecked";

	// For the checkbox images
	private static ImageRegistry imageRegistry = new ImageRegistry();

	/**
	 * Note: An image registry owns all of the image objects registered with it,
	 * and automatically disposes of them the SWT Display is disposed.
	 */
	static {
		String iconPath = "icons/";
		imageRegistry.put(CHECKED_IMAGE, AbstractUIPlugin
				.imageDescriptorFromPlugin("org.eclipse.bpel.ui", iconPath
						+ CHECKED_IMAGE + ".gif"));
		imageRegistry.put(UNCHECKED_IMAGE, AbstractUIPlugin
				.imageDescriptorFromPlugin("org.eclipse.bpel.ui", iconPath
						+ UNCHECKED_IMAGE + ".gif"));
	}

	/**
	 * Returns the image with the given key, or <code>null</code> if not found.
	 */
	private Image getImage(boolean isSelected) {
		String key = isSelected ? CHECKED_IMAGE : UNCHECKED_IMAGE;
		return imageRegistry.get(key);
	}

	public String getColumnText(Object element, int columnIndex) {
		String result = "";
		WSDL wsdl = (WSDL) element;
		switch (columnIndex) {
		case 0: // COMPLETED_COLUMN
			break;
		case 1:
			result = wsdl.getFilename();
			break;
		case 2:
			result = wsdl.getName();
			break;
		case 3:
			result = wsdl.getTargetNamespace();
			break;
		case 4:
			//@hahnml: Check if the WSDL exists already
			result = FragmentUtils.getUtils(null).checkIfProjectContainsFile(wsdl) ? "true" : "false";
			break;
		default:
			break;
		}
		return result;
	}

	/**
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object,
	 *      int)
	 */
	public Image getColumnImage(Object element, int columnIndex) {
		return (columnIndex == 0) ? // COMPLETED_COLUMN?
		getImage(((WSDLImpl) element).isSelected())
				: null;
	}

	/**
	 * Create the Table
	 */
	public static Table createTable(Composite parent) {
		int style = SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.HIDE_SELECTION;

		Table table = new Table(parent, style);

		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 4;
		table.setLayoutData(gridData);

		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		// 1st column with image/checkboxes - NOTE: The SWT.CENTER has no
		// effect!!
		TableColumn column = new TableColumn(table, SWT.CENTER, 0);
		column.setText(COLUMN_CHECK);
		column.setWidth(20);

		// 2nd column with task Description
		column = new TableColumn(table, SWT.LEFT, 1);
		column.setText(COLUMN_FILENAME);
		column.setWidth(200);

		// 3rd column with task Owner
		column = new TableColumn(table, SWT.LEFT, 2);
		column.setText(COLUMN_NAME);
		column.setWidth(200);

		// 4th column with task PercentComplete
		column = new TableColumn(table, SWT.LEFT, 3);
		column.setText(COLUMN_TNS);
		column.setWidth(200);
		
		//@hahnml
		// 5th column with task existsAlready
		column = new TableColumn(table, SWT.LEFT, 4);
		column.setText(COLUMN_EXISTS);
		column.setWidth(60);

		return table;
	}
	
	/**
	 * Create the TableViewer
	 */
	public static TableViewer createTableViewer(Table table) {

		TableViewer tableViewer = new TableViewer(table);
		tableViewer.setUseHashlookup(true);

		tableViewer.setColumnProperties(columnNames);

		// Create the cell editors
		CellEditor[] editors = new CellEditor[columnNames.length];

		// Column 1 : Completed (Checkbox)
		editors[0] = new CheckboxCellEditor(table);

		// Column 2 : Description (Free text)
		TextCellEditor textEditor = new TextCellEditor(table);
		editors[1] = textEditor;

		// Column 3 : Owner (Combo Box)
		textEditor = new TextCellEditor(table);
		editors[2] = textEditor;

		// Column 4 : Percent complete (Text with digits only)
		textEditor = new TextCellEditor(table);
		editors[3] = textEditor;

		// Assign the cell editors to the viewer
		tableViewer.setCellEditors(editors);

		// Set the default sorter for the viewer
		return tableViewer;
	}
}
