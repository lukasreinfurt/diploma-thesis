package org.eclipse.bpel.ui.wizards.simtech.table;

import java.util.Arrays;
import java.util.List;

import org.eclipse.bpel.ui.wizards.simtech.FragmentUtils.Conflict;
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
import org.eclipse.wst.wsdl.Namespace;
/**
 * LabelProvider for namespace conflict table
 * @author schrotbn
 *
 */
public class NamespaceConflictTableLabelProvider extends LabelProvider implements
ITableLabelProvider{
	// Column names
	private static final String COLUMN_FRAGMENT_PREFIX = "Fragment Prefix";
	private static final String COLUMN_MODEL_PREFIX    = "Model Prefix";
	private static final String COLUMN_CONFLICT_TYPE   = "Conflict Type";
	private static final String COLUMN_FRAGMENT_URI    = "Fragment URI";
	private static final String COLUMN_MODEL_URI 	   = "Model URI";
	
	// Set column names
	private static String[] columnNames = new String[] { 
		COLUMN_FRAGMENT_PREFIX, 
		COLUMN_FRAGMENT_URI,
		COLUMN_MODEL_PREFIX, 
		COLUMN_MODEL_URI, 
		COLUMN_CONFLICT_TYPE, 
		COLUMN_MODEL_PREFIX };

	public static List<String> getColumnNames() {
		return Arrays.asList(columnNames);
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		Conflict conflict = (Conflict)element;
		Namespace ns = (Namespace)conflict.getObject();
		Namespace modelNS = (Namespace)conflict.getAdditionalInfo();
		String result = "";
		switch (columnIndex) {
		case 0: 
			result = ns.getPrefix();
			break;
		case 1: 
			result = ns.getURI();
			break;
		case 2:
			result = modelNS.getPrefix();
			break;
		case 3:
			result = modelNS.getURI();
			break;
		case 4:
			result = conflict.getConflictType().name();
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
		gridData.horizontalSpan = 4;
		table.setLayoutData(gridData);

		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		
		// Set up 5 columns
		for (int i = 0; i < 5;i++) {
			TableColumn column = new TableColumn(table, SWT.CENTER, i);
			column.setText(columnNames[i]);
			column.setWidth(200);
		}
		
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

		// All columns have texteditors
		for (int i = 0; i < 5; i++) {
			TextCellEditor textEditor = new TextCellEditor(table);
			editors[i] = textEditor;
		}
		
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
