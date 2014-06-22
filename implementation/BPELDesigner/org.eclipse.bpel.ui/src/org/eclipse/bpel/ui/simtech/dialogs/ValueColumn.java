package org.eclipse.bpel.ui.simtech.dialogs;

import org.eclipse.bpel.ui.details.providers.ColumnTableProvider;
import org.eclipse.jface.viewers.ILabelProvider;

public class ValueColumn extends ColumnTableProvider.Column implements
		ILabelProvider {
	@Override
	public String getHeaderText() {
		return "Value";
	}

	@Override
	public String getProperty() {
		return "Value";
	}

	@Override
	public int getInitialWeight() {
		return 30;
	}

	public String getText(Object element) {
		String s = ((MetaDataProperty) element).getValue();
		return (s == null) ? "" : s;
	}
}