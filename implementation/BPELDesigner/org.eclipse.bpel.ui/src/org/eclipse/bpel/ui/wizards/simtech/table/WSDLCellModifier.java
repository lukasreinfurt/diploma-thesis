package org.eclipse.bpel.ui.wizards.simtech.table;

import org.eclipse.bpel.ui.wizards.simtech.documents.WSDLImpl;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.swt.widgets.TableItem;

/**
 * 
 * @author sonntamo
 */
public class WSDLCellModifier implements ICellModifier {

	private DocumentTable docTable;
	Boolean[] canModify = new Boolean[] {true, false, false, false};
	
	/**
	 * Constructor 
	 * @param TableViewerExample an instance of a TableViewerExample 
	 */
	public WSDLCellModifier(DocumentTable table) {
		super();
		this.docTable = table;
	}

	/**
	 * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object, java.lang.String)
	 */
	public boolean canModify(Object element, String property) {
		int columnIndex = WSDLLabelProvider.getColumnNames().indexOf(property);
		if (canModify != null && canModify.length > columnIndex)
			return canModify[columnIndex];
		return false;
	}

	/**
	 * @see org.eclipse.jface.viewers.ICellModifier#getValue(java.lang.Object, java.lang.String)
	 */
	public Object getValue(Object element, String property) {

		// Find the index of the column
		int columnIndex = WSDLLabelProvider.getColumnNames().indexOf(property);

		Object result = null;
		WSDLImpl wsdl = (WSDLImpl) element;

		switch (columnIndex) {
			case 0 : // Checkbox 
				result = new Boolean(wsdl.isSelected());
				break;
			case 1 : // Name 
				result = wsdl.getFilename();
				break;
			case 2 : // Filename 
				result = wsdl.getName();
				break;
			case 3 : // Target namespace 
				result = wsdl.getTargetNamespace();
				break;
			default :
				result = "";
		}
		return result;	
	}

	/**
	 * @see org.eclipse.jface.viewers.ICellModifier#modify(java.lang.Object, java.lang.String, java.lang.Object)
	 */
	public void modify(Object element, String property, Object value) {	

		// Find the index of the column 
		int columnIndex	= WSDLLabelProvider.getColumnNames().indexOf(property);
			
		TableItem item = (TableItem) element;
		WSDLImpl wsdl = (WSDLImpl) item.getData();
		String valueString;

		switch (columnIndex) {
			case 0 : // Checkbox 
			    wsdl.setSelected(((Boolean) value).booleanValue());
				break;
			case 1 : // Name 
				valueString = ((String) value).trim();
				wsdl.setFilename(valueString);
				break;
			case 2 : // Filename 
				valueString = ((String) value).trim();
				wsdl.setName(valueString);
				break;
			case 3 : // Target namespace
				valueString = ((String) value).trim();
				wsdl.setTargetNamespace(valueString);
				break;
			default :
		}
		docTable.getTableViewer().refresh();
	}

}
