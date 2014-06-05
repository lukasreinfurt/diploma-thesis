package org.simtech.bootware.core;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

/**
 * Interface that is implemented by the bootware.
 */
@WebService
public interface Bootware {
	@WebMethod String deploy(@WebParam(name = "context") Context context);
}
