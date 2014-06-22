package org.eclipse.bpel.ui.wizards.simtech.table;

import org.eclipse.bpel.ui.wizards.simtech.documents.XSDImpl;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.swt.widgets.TableItem;

/**
 * 
 * @author sonntamo
 */
public class XSDCellModifier implements ICellModifier {

	private DocumentTable docTable;
	Boolean[] canModify = new Boolean[] {true, false, false};
	
	/**
	 * Constructor 
	 * @param TableViewerExample an instance of a TableViewerExample 
	 */
	public XSDCellModifier(DocumentTable table) {
		super();
		this.docTable = table;
	}

	/**
	 * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object, java.lang.String)
	 */
	public boolean canModify(Object element, String property) {
		int columnIndex = XSDLabelProvider.getColumnNames().indexOf(property);
		if (canModify != null && canModify.length > columnIndex)
			return canModify[columnIndex];
		return false;
	}

	/**
	 * @see org.eclipse.jface.viewers.ICellModifier#getValue(java.lang.Object, java.lang.String)
	 */
	public Object getValue(Object element, String property) {

		// Find the index of the column
		int columnIndex = XSDLabelProvider.getColumnNames().indexOf(property);

		Object result = null;
		XSDImpl xsd = (XSDImpl) element;

		switch (columnIndex) {
			case 0 : // Checkbox 
				result = new Boolean(xsd.isSelected());
				break;
			case 1 : // Filename 
				result = xsd.getFilename();
				break;
			case 2 : // Target namespace 
				result = xsd.getTargetNamespace();
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
		int columnIndex	= XSDLabelProvider.getColumnNames().indexOf(property);
			
		TableItem item = (TableItem) element;
		XSDImpl xsd = (XSDImpl) item.getData();
		String valueString;

		switch (columnIndex) {
			case 0 : // Checkbox 
				xsd.setSelected(((Boolean) value).booleanValue());
				break;
			case 1 : // Filename 
				valueString = ((String) value).trim();
				xsd.setFilename(valueString);
				break;
			case 2 : // Target namespace
				valueString = ((String) value).trim();
				xsd.setTargetNamespace(valueString);
				break;
			default :
			}
		docTable.getTableViewer().refresh();
	}
}
