package org.simtech.bootware.eclipse;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

public final class XMLUtil {

	private XMLUtil() {}

	public static <T> T load(final Class<T> type, final String path) throws JAXBException {
		final JAXBContext jaxbContext = JAXBContext.newInstance(type);
		final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		final File file = new File(path);
		final JAXBElement root = unmarshaller.unmarshal(new StreamSource(file), type);

		return type.cast(root.getValue());
	}
}
