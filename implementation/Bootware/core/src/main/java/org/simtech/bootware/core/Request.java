package org.simtech.bootware.core;

public class Request {

	private String type = "";
	private Boolean failing = false;
	private Object response;
	private RequestContext context;

	public Request(final String t) {
		type = t;
	}

	public final String getType() {
		return type;
	}

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

	public final void setRequestContext(final RequestContext context) {
		this.context = context;
	}

	public final RequestContext getRequestContext() {
		return context;
	}

}
