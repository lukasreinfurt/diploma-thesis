package org.simtech.bootware.core;

import java.util.HashMap;
import java.util.Map;

public class InformationListWrapper {

	private Map<String, String> informationList = new HashMap<String, String>();

	public InformationListWrapper() {}

	public final void setInformationList(final Map<String, String> map) {
		informationList = map;
	}

	public final Map<String, String> getInformationList() {
		return informationList;
	}

}
