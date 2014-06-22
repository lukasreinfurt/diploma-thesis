package org.eclipse.bpel.ui.wizards.simtech.table;

import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;

/**
 * 
 * @author sonntamo
 */
public class DocumentTable {

	LabelProvider labelProvider;
	ICellModifier cellModifier;
	ContentProvider contentProvider;
	TableViewer tableViewer;
	

	
	public enum TableType {
		WSDL,
		XSD
	}
	
	public DocumentTable(TableType type, TableViewer tableViewer) {
		this.contentProvider = new ContentProvider();
		this.tableViewer = tableViewer;
		switch (type) {
		case WSDL:
			this.labelProvider = new WSDLLabelProvider();
			this.cellModifier = new WSDLCellModifier(this);
			break;
		case XSD:
			this.labelProvider = new XSDLabelProvider();
			this.cellModifier = new XSDCellModifier(this);
			break;
		}
	}

	public LabelProvider getLabelProvider() {
		return labelProvider;
	}
	
	public ICellModifier getCellModifier() {
		return cellModifier;
	}
	
	public ContentProvider getContentProvider() {
		return this.contentProvider;
	}
	
	public TableViewer getTableViewer() {
		return this.tableViewer;
	}
}
