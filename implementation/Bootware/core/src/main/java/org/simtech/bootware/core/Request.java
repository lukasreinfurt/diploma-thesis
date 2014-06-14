package org.simtech.bootware.core;

public class Request {

	private Boolean failing = false;
	private Object response;

	public Request() {}

	public final void fail(final String reason) {
		failing = true;
		response = reason;
	}

	public final Boolean isFailing() {
		return failing;
	}

	public final Object getResponse() {
		return response;
	}

}
