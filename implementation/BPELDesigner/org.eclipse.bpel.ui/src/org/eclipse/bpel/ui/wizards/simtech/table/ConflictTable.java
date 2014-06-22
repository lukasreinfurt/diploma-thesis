package org.eclipse.bpel.ui.wizards.simtech.table;

import org.eclipse.bpel.model.BPELPackage;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.wst.wsdl.WSDLPackage;

/**
 * 
 * @author hahnml
 */
public class ConflictTable {

	LabelProvider labelProvider;
	ContentProvider contentProvider;
	TableViewer tableViewer;
	

	
	public enum TableType {
		VARIABLE,
		PARTNERLINK,
		CORRELATION_SET,
		MESSAGE_EXCHANGE,
		EXTENSION,
		NAMESPACE
	}
	
	public ConflictTable(TableType type, TableViewer tableViewer) {
		this.contentProvider = new ContentProvider();
		this.tableViewer = tableViewer;
		switch (type) {
		case VARIABLE:
			this.labelProvider = new ConflictTableLabelProvider(BPELPackage.Literals.VARIABLE__NAME);
			break;
		case PARTNERLINK:
			this.labelProvider = new ConflictTableLabelProvider(BPELPackage.Literals.PARTNER_LINK__NAME);
			break;
		case CORRELATION_SET:
			this.labelProvider = new ConflictTableLabelProvider(BPELPackage.Literals.CORRELATION_SET__NAME);
			break;
		case MESSAGE_EXCHANGE:
			this.labelProvider = new ConflictTableLabelProvider(BPELPackage.Literals.MESSAGE_EXCHANGE__NAME);
			break;
		case EXTENSION:
			this.labelProvider = new ConflictTableLabelProvider(BPELPackage.Literals.EXTENSION__NAMESPACE);
			break;
		case NAMESPACE:
			this.labelProvider = new NamespaceConflictTableLabelProvider();
			break;
		}
	}

	public LabelProvider getLabelProvider() {
		return labelProvider;
	}
	
	public ContentProvider getContentProvider() {
		return this.contentProvider;
	}
	
	public TableViewer getTableViewer() {
		return this.tableViewer;
	}
}
