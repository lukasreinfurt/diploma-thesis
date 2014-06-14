package org.simtech.bootware.core;

import java.net.URL;
import java.util.Map;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import org.simtech.bootware.core.exceptions.DeployException;

/**
 * Interface that is implemented by the bootware.
 */
@WebService
public interface Bootware {
	@WebMethod EndpointsWrapper deploy(@WebParam(name = "context") Context context) throws DeployException;
	@WebMethod void undeploy(@WebParam(name = "endpoints") Map<String, URL> endpoints);
	@WebMethod void setCredentials(@WebParam(name = "credentialsList") Map<String, CredentialsWrapper> credentialsList);
}
