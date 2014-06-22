package org.eclipse.bpel.ui.wizards.simtech.table;

import java.util.Arrays;
import java.util.List;

import org.eclipse.bpel.model.BPELExtensibleElement;
import org.eclipse.bpel.ui.wizards.simtech.FragmentUtils.Conflict;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.jface.viewers.CellEditor;
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

public class ConflictTableLabelProvider extends LabelProvider implements
		ITableLabelProvider {
	
	// Set the table column property names
	private static final String COLUMN_NAME = "Name";
	private static final String COLUMN_CONFLICT_TYPE = "Conflict Type";
	private static final String COLUMN_XPATH = "Element XPath";
	
	private EStructuralFeature feature;
	
	public ConflictTableLabelProvider(EStructuralFeature feature) {
		this.feature = feature;
	}
	
	// Set column names
	private static String[] columnNames = new String[] { COLUMN_NAME, COLUMN_CONFLICT_TYPE, COLUMN_XPATH };

	public static List<String> getColumnNames() {
		return Arrays.asList(columnNames);
	}
		
	public String getColumnText(Object element, int columnIndex) {
		String result = "";
		Conflict conflict = (Conflict) element;
		switch (columnIndex) {
		case 0:
			result = conflict.getObject().eGet(this.feature).toString();
			break;
		case 1:
			result = conflict.getConflictType().name();
			break;
		case 2:
			result = ((BPELExtensibleElement)conflict.getObject()).getXPath();
			break;
		default:
			break;
		}
		return result;
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
		gridData.horizontalSpan = 3;
		table.setLayoutData(gridData);

		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		TableColumn column = new TableColumn(table, SWT.CENTER, 0);
		column.setText(COLUMN_NAME);
		column.setWidth(200);

		column = new TableColumn(table, SWT.LEFT, 1);
		column.setText(COLUMN_CONFLICT_TYPE);
		column.setWidth(200);
		
		column = new TableColumn(table, SWT.LEFT, 2);
		column.setText(COLUMN_XPATH);
		column.setWidth(200);

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

		// Column 1 : Name
		TextCellEditor textEditor = new TextCellEditor(table);
		editors[0] = textEditor;

		// Column 2 : Type
		textEditor = new TextCellEditor(table);
		editors[1] = textEditor;
		
		// Column 3 : XPath
		textEditor = new TextCellEditor(table);
		editors[2] = textEditor;

		// Assign the cell editors to the viewer
		tableViewer.setCellEditors(editors);

		// Set the default sorter for the viewer
		return tableViewer;
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}
}
