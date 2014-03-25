package org.simtech.bootware.plugins.plugin1;

import org.simtech.bootware.core.plugins.BasePlugin;
import org.simtech.bootware.core.filters.AbstractAcceptString;
import org.simtech.bootware.core.events.SimpleEvent;

import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Filter;

public class Plugin1 extends BasePlugin {

	public static class AcceptStringA extends AbstractAcceptString {
		protected String string() { return "A"; };
	}

	@Handler(filters = {@Filter(AcceptStringA.class)})
	public void handle(SimpleEvent event) {
		System.out.println("Plugin 1: " + event.getMessage());
		Plugin1Event plugin1Event = new Plugin1Event();
		plugin1Event.setMessage("Plugin1Event");
		eventBus.publish(plugin1Event);
	}

	@Handler
	public void handle(Plugin1Event event) {
		System.out.println("Plugin 1: " + event.getMessage());
	}

}
