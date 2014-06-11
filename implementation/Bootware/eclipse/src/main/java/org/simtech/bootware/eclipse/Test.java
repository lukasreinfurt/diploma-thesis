package org.simtech.bootware.eclipse;

import org.simtech.bootware.test.eclipse.ExtensionPointPlugin;

public class Test implements ExtensionPointPlugin {

	public Test() {}

	public final String execute() {
		return "Plugin executed!";
	}

}
