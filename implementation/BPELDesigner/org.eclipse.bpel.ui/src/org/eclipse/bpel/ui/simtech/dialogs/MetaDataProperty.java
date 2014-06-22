package org.eclipse.bpel.ui.simtech.dialogs;

public class MetaDataProperty {

	private String property;
	private String value;

	public MetaDataProperty(String property, String value) {
		this.property = property;
		this.value = value;
	}

	public String getProperty() {
		return property;
	}

	public String getValue() {
		return value;
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof MetaDataProperty) {
			MetaDataProperty mdProp = (MetaDataProperty)obj;
			if (property.equals(mdProp.getProperty()))
				return true;
		}
		return false;
	}
}
