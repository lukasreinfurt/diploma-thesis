package org.simtech.bootware.webservice;

import javax.jws.WebService;

@WebService(endpointInterface = "org.simtech.bootware.webservice.Hello")
public class HelloImpl implements Hello {
	private String message = new String("Hello, ");

	@Override
	public String sayHello(String name) {
		return message + name + ".";
	}
}
