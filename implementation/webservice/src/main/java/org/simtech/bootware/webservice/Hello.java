package org.simtech.bootware.webservice;

import javax.jws.WebService;

@WebService(serviceName  = "Hello",
            portName     = "HelloPort")
public class Hello {
	private String message = new String("Hello, ");

	public String sayHello(String name) {
		return message + name + ".";
	}
}
