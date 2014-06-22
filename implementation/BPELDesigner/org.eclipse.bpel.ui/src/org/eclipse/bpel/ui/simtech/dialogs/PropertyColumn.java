package org.eclipse.bpel.ui.simtech.dialogs;

import org.eclipse.bpel.ui.details.providers.ColumnTableProvider;
import org.eclipse.jface.viewers.ILabelProvider;

public class PropertyColumn extends ColumnTableProvider.Column implements
		ILabelProvider {

	@Override
	public String getHeaderText() {
		return "Property";
	}

	@Override
	public String getProperty() {
		return "Property";
	}

	@Override
	public int getInitialWeight() {
		return 30;
	}

	public String getText(Object element) {
		String s = ((MetaDataProperty) element).getProperty();
		return (s == null) ? "" : s;
	}
}
