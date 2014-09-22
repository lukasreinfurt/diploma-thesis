package org.simtech.bootware.core;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.simtech.bootware.core.events.CoreEvent;
import org.simtech.bootware.core.events.Severity;
import org.simtech.bootware.core.exceptions.ContextMappingException;

/**
 * Handles the mapping of an user context to an request context.
 * <p>
 * User context objects supplied by deploy and undeploy requests have to be
 * mapped to request context objects, which contain the actual information that
 * is required to fulfill these requests.
 */
public class ContextMapper {

	private String repositoryURL;
	private EventBus eventBus;

	public ContextMapper(final EventBus eventBus, final String repositoryURL) {
		this.eventBus = eventBus;
		this.repositoryURL = repositoryURL;
	}

	/**
	 * Maps a user context object to a request context object.
	 *
	 * @param userContext The user context that should be mapped to a request context.
	 *
	 * @return The request context that maps to the supplied user context.
	 *
	 * @throws ContextMappingException If the user context could not be mapped to a request context.
	 */
	public final RequestContext map(final UserContext userContext) throws ContextMappingException {

		final String application = userContext.getApplication();
		final String resource = userContext.getResource();

		eventBus.publish(new CoreEvent(Severity.INFO, "Mapping context with repository at " + repositoryURL + "/context?application=" + application + "&resource=" + resource));

		// Create client.
		final Client client = ClientBuilder.newBuilder().register(RequestContext.class).build();

		// Send POST to repository with payload attached.
		try {
			final RequestContext requestContext = client
					.target(repositoryURL)
					.path("/context")
					.queryParam("application", application)
					.queryParam("resource", resource)
					.request()
					.get(RequestContext.class);

			if (requestContext == null) {
				throw new ContextMappingException("The response send by the repository was empty.");
			}

			return requestContext;
		}
		catch (WebApplicationException e) {
			throw new ContextMappingException(e);
		}
		catch (ProcessingException e) {
			throw new ContextMappingException(e);
		}

	}

}
