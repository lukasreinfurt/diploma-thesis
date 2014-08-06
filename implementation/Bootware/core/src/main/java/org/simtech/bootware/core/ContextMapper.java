package org.simtech.bootware.core;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.simtech.bootware.core.exceptions.ContextMappingException;

public class ContextMapper {

	public ContextMapper() {}

	public final RequestContext map(final UserContext userContext) throws ContextMappingException {

		final String resource    = userContext.getResource();
		final String application = userContext.getApplication();
		final File mappingFile = new File("mapping/" + application + "/" + resource + "/context.xml");

		if (mappingFile.isFile()) {
			try {
				final JAXBContext jaxbContext = JAXBContext.newInstance(RequestContext.class);
				final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
				final JAXBElement<RequestContext> root = unmarshaller.unmarshal(new StreamSource(mappingFile), RequestContext.class);

				return root.getValue();
			}
			catch (JAXBException e) {
				throw new ContextMappingException(e);
			}
		}
		else {
			throw new ContextMappingException("Could not find mapping file " + mappingFile + ".");
		}
	}

}
