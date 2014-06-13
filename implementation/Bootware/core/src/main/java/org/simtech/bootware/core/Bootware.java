package org.simtech.bootware.core;

import java.util.Map;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

/**
 * Interface that is implemented by the bootware.
 */
@WebService
public interface Bootware {
	@WebMethod Endpoints deploy(@WebParam(name = "context") Context context);
	@WebMethod void undeploy(Endpoints endpoints);
	@WebMethod void setCredentials(Map<String, Credentials> credentials);
}
