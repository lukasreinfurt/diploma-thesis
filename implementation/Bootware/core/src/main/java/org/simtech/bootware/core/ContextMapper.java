package org.simtech.bootware.core;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

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

	public ContextMapper(final String repositoryURL) {
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

		// Build path to potential mapping file from the resource and application provided.
		final String resource    = userContext.getResource();
		final String application = userContext.getApplication();
		final File mappingFile = new File("mapping/" + application + "/" + resource + "/context.xml");

		// Load mapping file if it exists
		if (mappingFile.isFile()) {
			try {
				final JAXBContext jaxbContext = JAXBContext.newInstance(RequestContext.class);
				final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
				final JAXBElement<RequestContext> root = unmarshaller.unmarshal(new StreamSource(mappingFile), RequestContext.class);

				return root.getValue();
			}
			catch (JAXBException e) {
				throw new ContextMappingException("There was an error while loading the mapping file " + mappingFile + ": " + e.getMessage());
			}
		}
		else {
			throw new ContextMappingException("Could not find mapping file " + mappingFile + ".");
		}
	}

}
