package org.simtech.bootware.core;

/**
 * Stores information about an request.
 */
public class Request {

	private String type = "";
	private Boolean failing = false;
	private Object response;
	private RequestContext context;

	/**
	 * Creates a new request of the given type.
	 *
	 * @param t Type of the request as String (e.g. deploy, undeploy)
	 */
	public Request(final String t) {
		type = t;
	}

	/**
	 * Returns the type of the request.
	 *
	 * @return Type of the request as String.
	 */
	public final String getType() {
		return type;
	}

	/**
	 * Sets the request to failing.
	 * <p>
	 * If a request is failing can be checked later with @see #isFailing
	 *
	 * @param reason The reason why the request is failed as String.
	 */
	public final void fail(final String reason) {
		failing = true;
		response = reason;
	}

	/**
	 * Returns the failing state of the request.
	 *
	 * @return True if the request is failing, false otherwise.
	 */
	public final Boolean isFailing() {
		return failing;
	}

	/**
	 * Returns the request response.
	 *
	 * @return A response object.
	 */
	public final Object getResponse() {
		return response;
	}

	/**
	 * Sets the request context.
	 *
	 * @param context The request context.
	 */
	public final void setRequestContext(final RequestContext context) {
		this.context = context;
	}

	/**
	 * Returns the request context.
	 *
	 * @return The request context.
	 */
	public final RequestContext getRequestContext() {
		return context;
	}

}
