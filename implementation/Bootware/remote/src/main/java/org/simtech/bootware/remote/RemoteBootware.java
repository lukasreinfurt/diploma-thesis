package org.simtech.bootware.remote;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import org.simtech.bootware.core.Bootware;
import org.simtech.bootware.core.InformationListWrapper;
import org.simtech.bootware.core.UserContext;

/**
 * Interface that is implemented by the remote bootware.
 * <p>
 * It adds the getActive operation to the default operations defined in
 * @see org.simtech.bootware.core.Bootware
 */
@WebService
public interface RemoteBootware extends Bootware {

	/**
	 * Get information about an active application if it was deployed by the remote bootware.
	 *
	 * @param context The user context that descries the application.
	 *
	 * @return A wrapper object that contains a map of string with information about the active application.
	 */
	@WebMethod InformationListWrapper getActive(@WebParam(name = "context") UserContext context);
}
