package org.simtech.bootware.webservice;

import javax.jws.WebService;
import javax.jws.WebMethod;

@WebService
public interface Hello {
	@WebMethod String sayHello(String name);
}
