package org.simtech.bootware.core;

import java.util.HashMap;
import java.util.Map;

/**
 * A wrapper class that wraps an information list map.
 * This simplifies jaxb marshalling and unmarshalling.
 */
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
