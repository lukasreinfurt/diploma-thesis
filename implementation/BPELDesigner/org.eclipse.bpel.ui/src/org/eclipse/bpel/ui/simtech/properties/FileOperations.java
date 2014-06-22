package org.eclipse.bpel.ui.simtech.properties;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.bpel.apache.ode.deploy.model.dd.DocumentRoot;
import org.eclipse.bpel.apache.ode.deploy.model.dd.ProcessType;
import org.eclipse.bpel.apache.ode.deploy.model.dd.TDeployment;
import org.eclipse.bpel.apache.ode.deploy.model.dd.TMdProperty;
import org.eclipse.bpel.apache.ode.deploy.model.dd.TMetaData;
import org.eclipse.bpel.apache.ode.deploy.model.dd.ddFactory;
import org.eclipse.bpel.apache.ode.deploy.model.dd.util.ddResourceFactoryImpl;
import org.eclipse.bpel.apache.ode.deploy.model.dd.util.ddResourceImpl;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;

public class FileOperations {

	public enum PRETTY_PRINT {
		TRANSFORMER, XMLSERIALIZER
	}

	private static ddResourceImpl loadDD(IContainer mContainer) {
		final IFile file = mContainer.getFile(new Path("deploy.xml"));
		if (file.exists()) {
			URI fileURI = URI.createURI(file.getFullPath().toString());

			// generate Resource Factory
			ddResourceFactoryImpl fac = new ddResourceFactoryImpl();
			ddResourceImpl ddResource = (ddResourceImpl) fac
					.createResource(fileURI);
			try {
				ddResource.load(null);
				return ddResource;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	private static TMetaData getMetaDataElementFromDD(ddResourceImpl ddResource) {
		if (ddResource == null) return null;
		try {
			EObject eObj = ddResource.getContents().iterator().next();
			if (eObj instanceof DocumentRoot) {
				DocumentRoot docRoot = (DocumentRoot) eObj;
				TDeployment tDepl = docRoot.getDeploy();
				ProcessType procType = tDepl.getProcess().iterator().next();
				TMetaData tMD = procType.getMetaData();
				if (tMD == null) {
					tMD = ddFactory.eINSTANCE.createTMetaData();
					procType.setMetaData(tMD);
					ddResource.save(null);
				}
				return tMD;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	public static HashMap<String, String> loadPropertiesFromDD(IContainer mContainer) {
		HashMap<String, String> result = new HashMap<String, String>();
		ddResourceImpl ddResource = loadDD(mContainer);
		TMetaData tMD = getMetaDataElementFromDD(ddResource);
		if (tMD == null) return null;
		
		for (TMdProperty tMdProp : tMD.getMdProperty()) {
			String name = tMdProp.getName();
			String value = tMdProp.getValue();
			result.put(name, value);
		}
		
		return result;
	}

	public static void storePropertiesToDD(IContainer mContainer,
			HashMap<String, String> keyValuePairs) {
		ddResourceImpl ddResource = loadDD(mContainer);
		TMetaData tMD = getMetaDataElementFromDD(ddResource);
		if (tMD == null) return;
		
		// we first remove all properties because we do not want to run
		// into conflicts with the new properties
		tMD.getMdProperty().clear();
		
		// now we store all properties as given in the list
		Iterator<String> keyIt = keyValuePairs.keySet().iterator();
		if (keyIt.hasNext()) {
			try {
				while (keyIt.hasNext()) {
					String key = keyIt.next();
					String value = keyValuePairs.get(key);
					TMdProperty tMdProp = ddFactory.eINSTANCE
							.createTMdProperty();
					tMdProp.setName(key);
					tMdProp.setValue(value);
					tMD.getMdProperty().add(tMdProp);
				}
				ddResource.save(null);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
