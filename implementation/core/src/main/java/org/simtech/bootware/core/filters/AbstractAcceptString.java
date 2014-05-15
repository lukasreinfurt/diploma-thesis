package org.simtech.bootware.core.filters;

import net.engio.mbassy.listener.IMessageFilter;
import net.engio.mbassy.listener.MessageHandlerMetadata;

import org.simtech.bootware.core.events.BaseEvent;

public abstract class AbstractAcceptString implements IMessageFilter{

	protected abstract String string();

	@Override
	public boolean accepts(Object message, MessageHandlerMetadata metadata) {
		BaseEvent event = (BaseEvent) message;
		if (event.getMessage().equals(string())) {
			return true;
		}
		return false;
	}

}
