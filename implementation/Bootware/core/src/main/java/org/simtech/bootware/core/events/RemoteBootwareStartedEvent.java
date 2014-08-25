package org.simtech.bootware.core.events;

/**
 * An event type that is published once the remote bootware is started.
 * <p>
 * It contains the URL of the remote bootware.
 */
public class RemoteBootwareStartedEvent extends BaseEvent {

	protected String url;

	public RemoteBootwareStartedEvent(final Severity severity, final String message) {
		super(severity, message);
	}

	public RemoteBootwareStartedEvent(final Severity severity, final String message, final String url) {
		super(severity, message);
		this.url = url;
	}

	public final void setUrl(final String url) {
		this.url = url;
	}

	public final String getUrl() {
		return url;
	}

}
