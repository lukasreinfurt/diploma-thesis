package org.eclipse.bpel.ui.agora.snapshots.wizard.table;

import org.eclipse.bpel.ui.agora.ode135.client.TVariableInfo;

public class VariableInfo {
	private boolean selected = true;
	private TVariableInfo info = null;
	
	public VariableInfo(boolean load, TVariableInfo info) {
		super();
		this.selected = load;
		this.info = info;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean load) {
		this.selected = load;
	}

	public TVariableInfo getInfo() {
		return info;
	}

	public void setInfo(TVariableInfo info) {
		this.info = info;
	}
}
