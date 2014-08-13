package org.simtech.bootware.remote;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import org.simtech.bootware.core.Bootware;
import org.simtech.bootware.core.InformationListWrapper;
import org.simtech.bootware.core.UserContext;

/**
 * Interface that is implemented by the bootware.
 */
@WebService
public interface RemoteBootware extends Bootware {
	@WebMethod InformationListWrapper getActive(@WebParam(name = "context") UserContext context);
}
