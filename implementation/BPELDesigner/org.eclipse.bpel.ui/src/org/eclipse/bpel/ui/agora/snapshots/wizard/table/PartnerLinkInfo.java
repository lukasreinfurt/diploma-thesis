package org.eclipse.bpel.ui.agora.snapshots.wizard.table;

import org.eclipse.bpel.ui.agora.ode135.client.TPartnerLinkInfo;

public class PartnerLinkInfo {
	private boolean selected = true;
	private TPartnerLinkInfo info = null;
	
	public PartnerLinkInfo(boolean load, TPartnerLinkInfo info) {
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

	public TPartnerLinkInfo getInfo() {
		return info;
	}

	public void setInfo(TPartnerLinkInfo info) {
		this.info = info;
	}
}
