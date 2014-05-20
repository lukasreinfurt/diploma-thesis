package org.simtech.bootware.core;

import javax.jws.WebService;
import javax.jws.WebMethod;

/**
 * Interface that is implemented by the bootware.
 */
@WebService
public interface Bootware {
	@WebMethod String request(String name);
}
