package org.simtech.bootware.core.filters;

import org.simtech.bootware.core.events.BaseEvent;

import net.engio.mbassy.listener.IMessageFilter;
import net.engio.mbassy.listener.MessageHandlerMetadata;

public abstract class AbstractAcceptString implements IMessageFilter {

	protected abstract String string();

	@Override
	public final boolean accepts(final Object message, final MessageHandlerMetadata metadata) {
		final BaseEvent event = (BaseEvent) message;
		if (event.getMessage().equals(string())) {
			return true;
		}
		return false;
	}

}
